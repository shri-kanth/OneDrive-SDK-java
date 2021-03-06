package com.bhyoo.onedrive.container.facet;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/location_facet.htm">https://dev.onedrive.com/facets/location_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class LocationFacet {
	@Getter @Setter(PRIVATE) protected @Nullable Double altitude;
	@Getter @Setter(PRIVATE) protected @Nullable Double latitude;
	@Getter @Setter(PRIVATE) protected @Nullable Double longitude;
}
