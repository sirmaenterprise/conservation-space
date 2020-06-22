package com.sirma.itt.seip.testutil.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} that will throw {@link IOException} for all read methods
 * 
 * @author BBonev
 */
public class FailingInputStream extends InputStream {

	@Override
	public int read() throws IOException {
		throw new IOException();
	}

	@Override
	public int read(byte[] b) throws IOException {
		throw new IOException();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		throw new IOException();
	}

}
