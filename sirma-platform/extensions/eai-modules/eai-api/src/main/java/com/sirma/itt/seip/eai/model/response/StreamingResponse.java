package com.sirma.itt.seip.eai.model.response;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.sirma.itt.seip.eai.model.ServiceResponse;

/**
 * A streaming response containing the stream data and its length, encoding and mimetype.
 * 
 * @author bbanchev
 */
public class StreamingResponse implements ServiceResponse, AutoCloseable {

	private InputStream stream;
	private String contentType;
	private String contentEncoding;
	private long contentLength;

	/**
	 * Instantiates a new streaming response.
	 *
	 * @param stream
	 *            the stream
	 * @param contentType
	 *            the content type
	 * @param contentEncoding
	 *            the content encoding
	 * @param contentLength
	 *            the content length
	 */
	public StreamingResponse(InputStream stream, String contentType, String contentEncoding, long contentLength) {
		this.stream = stream;
		this.contentType = contentType;
		this.contentEncoding = contentEncoding;
		this.contentLength = contentLength;
	}

	/**
	 * Gets the wrapped stream.
	 *
	 * @return the stream
	 */
	public InputStream getStream() {
		return stream;
	}

	/**
	 * Gets the content type.
	 *
	 * @return the content type or null if not set
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Gets the content length.
	 *
	 * @return the content length
	 */
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * Gets the content encoding.
	 *
	 * @return the content encoding
	 */
	public String getContentEncoding() {
		return contentEncoding;
	}

	@Override
	public void close() throws IOException {
		IOUtils.closeQuietly(stream);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StreamingResponse [stream=");
		builder.append(stream);
		builder.append(", contentType=");
		builder.append(contentType);
		builder.append(", contentEncoding=");
		builder.append(contentEncoding);
		builder.append(", contentLength=");
		builder.append(contentLength);
		builder.append("]");
		return builder.toString();
	}
}
