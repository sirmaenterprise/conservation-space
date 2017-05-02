package com.sirma.itt.seip.adapters.remote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File part that allows files with name that is encoded in a custom charset. The default charset is UTF-8.
 *
 * @author BBonev
 */
public class UnicodeFilePart extends FilePart {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final byte[] FILE_NAME_BYTES = EncodingUtil.getAsciiBytes(FILE_NAME);
	private String fileNameCharset = "UTF-8";

	/**
	 * Creates a file part with specified name and source
	 *
	 * @param name
	 *            is the name of the part
	 * @param partSource
	 *            is the source to use
	 */
	public UnicodeFilePart(String name, PartSource partSource) {
		super(name, partSource);
	}

	/**
	 * Creates a part from with a name and using the given file
	 *
	 * @param name
	 *            is the name of the part
	 * @param file
	 *            is the file to use as source
	 * @throws FileNotFoundException
	 */
	public UnicodeFilePart(String name, File file) throws FileNotFoundException {
		super(name, file);
	}

	/**
	 * Creates a file part with a name and file with custom name
	 *
	 * @param name
	 *            is the name of the part
	 * @param fileName
	 *            is the name of the destination file
	 * @param file
	 *            is the name of the source file
	 * @throws FileNotFoundException
	 */
	public UnicodeFilePart(String name, String fileName, File file) throws FileNotFoundException {
		super(name, fileName, file);
	}

	/**
	 * Creates a file part with a name and given part source with specified content type and charset
	 *
	 * @param name
	 *            is the name of the part
	 * @param partSource
	 *            is the source to use
	 * @param contentType
	 *            is the content type of the source
	 * @param charset
	 *            is the charset of the source
	 */
	public UnicodeFilePart(String name, PartSource partSource, String contentType, String charset) {
		super(name, partSource, contentType, charset);
	}

	/**
	 * Creates a file part with a name and given file with specified content type and charset
	 *
	 * @param name
	 *            is the name of the part
	 * @param file
	 *            is the source file to use
	 * @param contentType
	 *            is the content type of the file
	 * @param charset
	 *            is the charset of the file
	 * @throws FileNotFoundException
	 */
	public UnicodeFilePart(String name, File file, String contentType, String charset) throws FileNotFoundException {
		super(name, file, contentType, charset);
	}

	/**
	 * Creates a file part with a name and given file with custom name and specified content type and charset
	 *
	 * @param name
	 *            is the name of the part
	 * @param fileName
	 *            is the custom name of the file
	 * @param file
	 *            is the source file to use
	 * @param contentType
	 *            is the content type of the file
	 * @param charset
	 *            is the charset of the file
	 * @throws FileNotFoundException
	 */
	public UnicodeFilePart(String name, String fileName, File file, String contentType, String charset)
			throws FileNotFoundException {
		super(name, fileName, file, contentType, charset);
	}

	/**
	 * Creates the header for the file using the {@link #getFileNameCharset()}<br>
	 * {@inheritDoc}
	 */
	@Override
	protected void sendDispositionHeader(OutputStream out) throws IOException {
		LOGGER.trace("enter sendDispositionHeader(OutputStream out)");
		super.sendDispositionHeader(out);
		String filename = getSource().getFileName();
		if (filename != null) {
			out.write(FILE_NAME_BYTES);
			out.write(QUOTE_BYTES);
			out.write(EncodingUtil.getBytes(filename, fileNameCharset));
			out.write(QUOTE_BYTES);
		}
	}

	/**
	 * Returns the charset that will used for encoding the name of the initialized file
	 *
	 * @return the fileNameCharset
	 */
	public String getFileNameCharset() {
		return fileNameCharset;
	}

	/**
	 * Sets the charset to be used when decoding the file name when sending it to the remote site
	 *
	 * @param fileNameCharset
	 *            the fileNameCharset to set
	 */
	public void setFileNameCharset(String fileNameCharset) {
		this.fileNameCharset = fileNameCharset;
	}

}
