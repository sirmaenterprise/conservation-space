package com.sirma.sep.content.rendition;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * The service provides logic for working with thumbnails for any instance.<br>
 * All thumbnails returned are in Base64 format encoded using {@link org.apache.commons.codec.binary.Base64}.
 *
 * @author bbanchev
 */
public interface RenditionService {

	String BASE64_IMAGE_PREFIX = "data:image/png;base64,";

	/** The default purpose. */
	String DEFAULT_PURPOSE = "default";

	/**
	 * Gets the primary thumbnail for instance. If there is association for primary image it is retrieved its thumbnail
	 * from alfresco
	 *
	 * @param instance
	 *            the instance to get thumbnail for
	 * @return the primary thumbnail or the default as {@link org.apache.commons.codec.binary.Base64} encoded
	 */
	String getPrimaryThumbnail(Instance instance);

	/**
	 * Gets the thumbnail rendered in dms or the default if there is no thumbnail for that type.
	 *
	 * @param instance
	 *            the instance to get thumbnail for
	 * @return the thumbnail or the default as {@link org.apache.commons.codec.binary.Base64} encoded
	 */
	String getDefaultThumbnail(Instance instance);

	/**
	 * Gets the default thumbnail for an instance with given id.
	 *
	 * @param id
	 *            the id
	 * @return the thumbnail or <code>null</code> if not found
	 */
	String getThumbnail(Serializable id);

	/**
	 * Gets the thumbnail for the given id and specified purpose
	 *
	 * @param id
	 *            the id
	 * @param purpose
	 *            the purpose
	 * @return the thumbnail or <code>null</code> if not found
	 */
	String getThumbnail(Serializable id, String purpose);

	/**
	 * Load thumbnail for the given instance if that have any associated to them at all.
	 *
	 * @param <I>
	 *            the generic type
	 * @param instance
	 *            the instance
	 * @return the same instance
	 */
	<I extends Instance> I loadThumbnail(I instance);

	/**
	 * Load thumbnails for the given instances if that have any associated to them at all. This method load the
	 * thumbnails or schedules a check for the one that are missing.
	 * <p>
	 * Note that this method is called for any batch loaded instances and could cause DMS overload with requesting
	 * thumbnails over and over. So when there is a need of batch instance loading that does not require tumbnails (some
	 * batch internal instance processing) this method could be disabled with
	 * {@link Options#DISABLE_POST_INSTANCE_LOAD_DECORATION}. If disabled the method does nothing and returns
	 * immediately.
	 *
	 * @param <I>
	 *            the generic type
	 * @param instances
	 *            the instances
	 */
	<I extends Instance> void loadThumbnails(Collection<I> instances);

	/**
	 * Gets the thumbnails for the given collection of ids.
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @return a mapping for each instance id that has a thumbnail.
	 */
	<S extends Serializable> Map<S, String> getThumbnails(Collection<S> ids);
}
