package com.sirma.itt.seip.content;

import java.io.File;
import java.io.OutputStream;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * The Interface ContentService provides means for working/accessing with
 * documents content.
 *
 * @author BBonev
 */
public interface ContentService {

	/**
	 * Fetches the content for the given dms descriptor and store it in a
	 * temporary file
	 *
	 * @param location
	 *            the location to fetch
	 * @return the path to the local file
	 */
	File getContent(FileDescriptor location);

	/**
	 * Fetches the location content and store it in a temporary file.
	 *
	 * @param location
	 *            the location to fetch
	 * @param fileName
	 *            the file name (last segment) to store the file as.
	 * @return the path to the local file
	 */
	File getContent(FileDescriptor location, String fileName);

	/**
	 * Retrieve the content of document by directly streaming to the output
	 * param
	 *
	 * @param location
	 *            the document descriptor
	 * @param output
	 *            the stream to use.
	 * @return the number of bytes directly copied. If
	 *         {@link java.io.IOException} or any other -1L is returned
	 */
	long getContent(FileDescriptor location, OutputStream output);

	/**
	 * Load text content for the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the string
	 */
	String loadTextContent(Instance instance);

	/**
	 * Load text content.
	 *
	 * @param reference
	 *            the reference
	 * @return the string
	 */
	String loadTextContent(InstanceReference reference);

	/**
	 * Extracts the content for the given instance from the given file descriptor.
	 *
	 * @param mimetype
	 *            the mimetype of the content represented by the given descriptor
	 * @param descriptor
	 *            the descriptor that provides access to a content that need his content extracted
	 * @return the extracted content or <code>null</code> if not applicable or no content found.
	 */
	String extractContent(String mimetype, FileDescriptor descriptor);

	/**
	 * Sets the given content string to an instance (if a suitable
	 * {@link ContentSetter} is found).
	 *
	 * @param instance
	 *            Instance to which the content will be set.
	 * @param content
	 *            Content to set.
	 */
	void setContent(Instance instance, String content);

	/**
	 * Uploads the given instance
	 *
	 * @param instance
	 *            the instance to be uploaded
	 * @param descriptor
	 *            the descriptor of the instance
	 * @return the properties of the uploaded instance
	 */
	FileAndPropertiesDescriptor uploadContent(Instance instance, FileDescriptor descriptor);

	/**
	 * Returns the uri of the uploaded instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the uri of the instance
	 */
	String getContentURI(Instance instance);

	/**
	 * Returns a file descriptor for the given instance.
	 *
	 * @param instance
	 *            the instance
	 *
	 * @return the descriptor
	 */
	FileDescriptor getDescriptor(Instance instance);
}
