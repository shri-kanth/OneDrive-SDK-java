package com.bhyoo.onedrive.network.async;

import com.bhyoo.onedrive.client.RequestTool;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.AsciiString;
import io.netty.util.concurrent.Future;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderNames.HOST;
import static io.netty.handler.codec.http.HttpHeaderValues.GZIP;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public abstract class AbstractClient {
	@NotNull @Getter protected final URI uri;
	@NotNull @Getter protected final HttpMethod method;
	@Nullable @Getter protected final byte[] content;
	@NotNull protected final DefaultFullHttpRequest request;


	public AbstractClient(@NotNull HttpMethod method, @NotNull URI uri, @Nullable byte[] content) {
		this.uri = uri;
		this.method = method;
		this.content = content;

		if (!RequestTool.SCHEME.equalsIgnoreCase(uri.getScheme())) {
			throw new IllegalArgumentException("Wrong network scheme : \"" + uri.getScheme() + "\".");
		}

		if (content != null) {
			this.request = new DefaultFullHttpRequest(
					HTTP_1_1,
					method,
					uri.toASCIIString(),
					Unpooled.wrappedBuffer(content));
		}
		else {
			this.request = new DefaultFullHttpRequest(HTTP_1_1, method, uri.toASCIIString());
		}

		this.request.headers()
				.set(HOST, uri.getHost())
				.set(ACCEPT_ENCODING, GZIP);
	}

	@NotNull public AbstractClient setHeader(AsciiString header, CharSequence value) {
		request.headers().set(header, value);
		return this;
	}

	@NotNull public AbstractClient setHeader(String header, String value) {
		request.headers().set(header, value);
		return this;
	}

	public abstract Future<?> execute();
}
