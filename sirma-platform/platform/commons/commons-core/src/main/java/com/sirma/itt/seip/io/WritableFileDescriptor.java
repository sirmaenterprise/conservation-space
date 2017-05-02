package com.sirma.itt.seip.io;

import java.io.InputStream;

/**
 * File descriptor that allows modification of the content identified by the current descriptor.
 *
 * @author BBonev
 */
public interface WritableFileDescriptor extends FileDescriptor {

	/**
	 * Writes the given input stream to current descriptor. The method will close the stream when finished writhing.<br>
	 * NOTE: This does not mean it will write to the remote system if points to remove host but that when calling the
	 * {@link #getInputStream()} method will return the written content. Where the content is written depends on the
	 * implementation.
	 *
	 * @param inputStream
	 *            the input stream
	 */
	void write(InputStream inputStream);
}
