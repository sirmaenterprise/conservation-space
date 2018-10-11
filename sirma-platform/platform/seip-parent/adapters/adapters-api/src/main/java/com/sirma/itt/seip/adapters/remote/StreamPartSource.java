package com.sirma.itt.seip.adapters.remote;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.methods.multipart.PartSource;

/**
 * Part source that works directly with passed {@link InputStream}
 *
 * @author BBonev
 */
public class StreamPartSource implements PartSource {

	private final String filename;
	private final InputStream source;
	private long length;

	/**
	 * Instantiates a new stream part source.
	 *
	 * @param filename
	 *            the filename
	 * @param length
	 *            the length
	 * @param source
	 *            the source
	 */
	public StreamPartSource(String filename, long length, InputStream source) {
		this.filename = filename;
		this.length = length;
		this.source = source;
	}

	@Override
	public long getLength() {
		return length;
	}

	@Override
	public String getFileName() {
		return filename;
	}

	@Override
	public InputStream createInputStream() throws IOException {
		if (source != null) {
			return source;
		}
		return new ByteArrayInputStream(new byte[] {});
	}

}
