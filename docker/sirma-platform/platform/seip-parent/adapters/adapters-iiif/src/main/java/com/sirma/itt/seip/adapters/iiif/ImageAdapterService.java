package com.sirma.itt.seip.adapters.iiif;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Adapter service for uploading images to a specific server.
 *
 * @author Nikolay Ch
 */
public interface ImageAdapterService {
	/**
	 * Uploads the image to the image server.
	 *
	 * @param instance
	 *            the instance
	 * @param fileDescriptor
	 *            the file descriptor
	 * @return the property descriptor of the uploaded image or null if the
	 *         instance is not image
	 * @throws DMSException
	 *             if an error occurs
	 */
	FileAndPropertiesDescriptor upload(Instance instance, FileDescriptor fileDescriptor) throws DMSException;

	/**
	 * Returns an uri of the uploaded instance.
	 *
	 * @param instance
	 *            the uploaded instance
	 * @return null if the instance is not uploaded or it's not image
	 */
	String getContentUrl(Instance instance);
}
