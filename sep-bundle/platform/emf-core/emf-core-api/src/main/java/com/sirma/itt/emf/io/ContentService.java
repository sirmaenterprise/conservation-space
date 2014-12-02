package com.sirma.itt.emf.io;

import java.io.File;
import java.io.OutputStream;

import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * The Interface ContentService provides means for working/accessing with documents content.
 *
 * @author BBonev
 */
public interface ContentService {

	/**
	 * Fetches the content for the given dms descriptor and store it in a temporary file
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
	 * Retrieve the content of document by directly streaming to the output param
	 *
	 * @param location
	 *            the document descriptor
	 * @param output
	 *            the stream to use.
	 * @return the number of bytes directly copied. If {@link java.io.IOException} or any other -1L
	 *         is returned
	 */
	long getContent(FileDescriptor location, OutputStream output);

	/**
	 * Extracts the content for the given instance from the given file descriptor.
	 * 
	 * @param instance
	 *            the instance
	 * @param descriptor
	 *            the descriptor
	 * @return the extracted content or <code>null</code> if not applicable or no content found.
	 */
	String extractContent(Instance instance, FileDescriptor descriptor);

}
