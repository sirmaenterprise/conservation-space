package com.sirma.sep.content.rendition;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Plugin for providing thumbnail for instances. The provider should operation on 2 steps. The first step is to create
 * an thumbnail end point from a source instance that will be used in the second step to try to retrieve the actual
 * thumbnail. The end point should be in text format without size limit. The requesting a thumbnail only the end point
 * will be provided!
 *
 * @author BBonev
 */
public interface ThumbnailProvider extends Plugin, Named {
	String TARGET_NAME = "ThumbnailProvider";

	/**
	 * Creates a thumbnail end point from the given source {@link Instance}. If the method returns <code>null</code> the
	 * instance will be considered for not supported by the provided or not applicable for thumbnail retrieval and will
	 * not be registered at all.
	 *
	 * @param source
	 *            the source instance to use for end point creation.
	 * @return returns a non <code>null</code> end point for valid instance or <code>null</code> to ignore the instance.
	 */
	String createThumbnailEndPoint(Instance source);

	/**
	 * Retrieve a thumbnail for the given non <code>null</code> end point. If no thumbnail is available or there is an
	 * error while retrieving <code>null</code> should be returned to reschedule retrieval
	 *
	 * @param endPoint
	 *            the end point to use to retrieve the thumbnail.
	 * @return the thumbnail or <code>null</code>
	 */
	String getThumbnail(String endPoint);

	/**
	 * Returns the provider identifier. The name should not exceed 100 characters.
	 *
	 * @return the provider identifier.
	 */
	@Override
	String getName();
}
