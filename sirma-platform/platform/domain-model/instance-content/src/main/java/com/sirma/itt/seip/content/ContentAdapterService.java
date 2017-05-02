package com.sirma.itt.seip.content;

import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * The Interface ContentAdapterService is responsible to fetch the dms content.
 */
public interface ContentAdapterService {

	/**
	 * Gets the content descriptor.
	 *
	 * @param instance
	 *            the instance
	 * @return the content descriptor
	 */
	FileDescriptor getContentDescriptor(DMSInstance instance);

	/**
	 * Gets the content descriptor.
	 *
	 * @param dmsId
	 *            the dms id
	 * @return the content descriptor
	 */
	FileDescriptor getContentDescriptor(String dmsId);

	/**
	 * Gets the content descriptor.
	 *
	 * @param dmsId
	 *            the dms id
	 * @param containerId
	 *            the container id
	 * @return the content descriptor
	 */
	FileDescriptor getContentDescriptor(String dmsId, String containerId);

}
