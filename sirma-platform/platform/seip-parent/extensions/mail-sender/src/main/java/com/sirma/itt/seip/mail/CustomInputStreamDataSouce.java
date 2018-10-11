package com.sirma.itt.seip.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import javax.activation.DataSource;
import javax.mail.internet.SharedInputStream;

/**
 * Custom implementation of data source and shared input stream used, when building mail attachments parts. The
 * implementation depends on the provided supplier to create new streams for each call of the get method so we can
 * manipulate/close the stream without any problems.
 *
 * @author A. Kunchev
 */
public class CustomInputStreamDataSouce implements DataSource, SharedInputStream {

	private Supplier<InputStream> inStream;

	private String type;

	/**
	 * Constructor.
	 *
	 * @param inputStream
	 *            the input stream
	 * @param contentType
	 *            the type of the content
	 */
	public CustomInputStreamDataSouce(Supplier<InputStream> inputStream, String contentType) {
		inStream = inputStream;
		type = contentType;
	}

	/**
	 * Setter for the type.
	 *
	 * @param type
	 *            the type
	 */
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getContentType() {
		if (type == null) {
			return "application/octet-stream";
		}
		return type;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return inStream.get();
	}

	@Override
	public String getName() {
		return "InputStreamDataSource";
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new IOException("Not Supported");
	}

	@Override
	public long getPosition() {
		return 0;
	}

	@Override
	public InputStream newStream(long arg0, long arg1) {
		return inStream.get();
	}
}
