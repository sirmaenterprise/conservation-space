package com.sirma.sep.content.type;

import java.io.File;
import java.io.InputStream;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Defines methods for mime type resolving. Used to define extensions, which can be called consecutively to resolve
 * given input mime type.
 *
 * @author A. Kunchev
 */
public interface MimeTypeResolver extends Plugin {

	String TARGET_NAME = "mimeTypeResolver";

	/**
	 * Resolves the mime type of the given input stream. Note that if the passed input stream must support
	 * {@link InputStream#mark(int)} and {@link InputStream#reset()} methods. The method does not close the stream.
	 *
	 * @param stream
	 *         the stream, which mime type is searched
	 * @param fileName
	 *         - the filename of document from which <code>stream</code> is taken.
	 * @return the mime type of the passed stream or <b>null</b> if the mime type can not be resolved
	 */
	String getMimeType(InputStream stream, String fileName);

	/**
	 * Resolves the mime type of the given file.
	 *
	 * @param file
	 *            the file, which mime type is searched
	 * @return the mime type of the passed file or <b>null</b> if the mime type can not be resolved
	 */
	String getMimeType(File file);

	/**
	 * Resolves the mime type of the given byte array.
	 *
	 * @param bytes
	 *            the bytes, which mime type is searched
	 * @param fileName
	 *         - the filename of document from which <code>bytes</code> are taken.
	 * @return the mime type of the passed bytes array or <b>null</b> if the mime type can not be resolved
	 */
	String getMimeType(byte[] bytes, String fileName);

	/**
	 * Resolve mimetype by file name only
	 *
	 * @param fileName
	 *            the file name to test for
	 * @return the mime type that corresponds to the given file name or <code>null</code> if the name is
	 *         <code>null</code> or empty or does not have an extension.
	 */
	String resolveFromName(String fileName);
}
