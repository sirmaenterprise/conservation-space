package com.sirma.itt.cmf.alfresco4.remote;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.slf4j.Logger;

/**
 * Part source that works directly with passed {@link InputStream}
 * 
 * @author BBonev
 */
public class StreamPartSource implements PartSource {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StreamPartSource.class);

	/** The filename. */
	private final String filename;

	/** The source. */
	private final InputStream source;

	/**
	 * Instantiates a new stream part source.
	 * 
	 * @param filename
	 *            the filename
	 * @param source
	 *            the source
	 */
	public StreamPartSource(String filename, InputStream source) {
		this.filename = filename;
		this.source = source;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLength() {
		try {
			if (source != null) {
				return source.available();
			}
		} catch (Exception e) {
			LOGGER.warn("Could not get source stream evailable size", e);
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFileName() {
		return filename;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream createInputStream() throws IOException {
		if (this.source != null) {
			return source;
		}
		return new ByteArrayInputStream(new byte[] {});
	}

}
