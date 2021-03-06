package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.container.items.AbstractDriveItem;
import com.bhyoo.onedrive.container.items.DriveItem;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.network.ErrorResponse;
import com.bhyoo.onedrive.utils.ByteBufStream;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class DriveItemHandler extends SimpleChannelInboundHandler<HttpObject> {
	private final DefaultDriveItemPromise promise;
	private final ObjectMapper mapper;
	private final ByteBufStream stream;
	private HttpResponse response;
	private ErrorResponse errorResponse;
	private DriveItem driveItem;
	private Thread workerThread;
	@Nullable private Exception workerException;

	public DriveItemHandler(DefaultDriveItemPromise promise, ObjectMapper mapper) {
		this.promise = promise;
		this.mapper = mapper;
		this.stream = new ByteBufStream();
	}

	@Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		promise.setFailure(cause);
		if (!stream.isClosed()) stream.close();
		ctx.close();
	}

	@Override protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpResponse) {
			this.response = (HttpResponse) msg;

			if (response.status().code() == HTTP_OK) {
				workerThread = new Thread() {
					@Override public void run() {
						try {
							driveItem = mapper.readValue(stream, AbstractDriveItem.class);
						}
						catch (IOException e) {
							// FIXME: custom exception
							workerException = new RuntimeException("DEV: Unrecognizable json response.", e);
						}
						catch (Exception e) {
							workerException = e;
						}
					}
				};
			}
			else {
				workerThread = new Thread() {
					@Override public void run() {
						try {
							errorResponse = mapper
									.readerFor(ErrorResponse.class)
									.with(DeserializationFeature.UNWRAP_ROOT_VALUE)
									.readValue(stream);
						}
						catch (IOException e) {
							// FIXME: custom exception
							workerException = new RuntimeException("DEV: Unrecognizable json response.", e);
						}
						catch (Exception e) {
							workerException = e;
						}
					}
				};
			}
			workerThread.start();
		}
		else if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;

			if (content instanceof LastHttpContent) {
				ctx.close();
				stream.setNoMoreBuf();
				workerThread.join();

				if (workerException != null) {
					promise.setFailure(workerException);
				}
				else if (response.status().code() == HTTP_OK) {
					promise.setSuccess(driveItem);
				}
				else {
					promise.setFailure(
							new ErrorResponseException(HTTP_OK, response.status().code(),
									errorResponse.getCode(), errorResponse.getMessage()));
				}
			}
			else {
				content.content().retain();
				stream.writeByteBuf(content.content());
			}
		}
	}
}
