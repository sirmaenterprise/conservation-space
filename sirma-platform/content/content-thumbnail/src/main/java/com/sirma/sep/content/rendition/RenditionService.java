package com.sirma.sep.content.rendition;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * The service provides logic for working with thumbnails for any instance.<br>
 * All thumbnails returned are in Base64 format encoded using {@link org.apache.commons.codec.binary.Base64}.
 *
 * @author bbanchev
 */
public interface RenditionService {

	String BASE64_IMAGE_PREFIX = "data:image/png;base64,";

	/**
	 * The default purpose. This is no longer used constant and will be removed in future versions.
	 *
	 * @deprecated Replaced by {@link ThumbnailType}
	 */
	@Deprecated
	String DEFAULT_PURPOSE = "default";

	/**
	 * Gets the thumbnail for the given instance that has highest priority if more then one.
	 *
	 * @param id the id of the instance to fetch it's thumbnail
	 * @return the thumbnail or <code>null</code> if not found
	 */
	String getThumbnail(Serializable id);

	/**
	 * Gets the thumbnail for the given id and specified purpose.<br>
	 * Note that both arguments are required.
	 *
	 * @param id the id of the instance to fetch it's thumbnail
	 * @param type the thumbnail type to load.
	 * @return the found thumbnail or <code>null</code> if not found
	 */
	String getThumbnail(Serializable id, ThumbnailType type);

	/**
	 * Gets the thumbnails for the given collection of ids. The returned thumbnails will be the one with highest
	 * priority for each instance. This method should work similarly as {@link #getThumbnail(Serializable)} but for
	 * multiple instance. If any of the instances does not have thumbnail then the result map will not contain an entry
	 * for that instance.
	 *
	 * @param <S> the instance id type
	 * @param ids the collection of instance identifiers to load their thumbnails.
	 * @return a mapping for each instance id and it's thumbnail with highest priority.
	 */
	<S extends Serializable> Map<S, String> getThumbnails(Collection<S> ids);
}
