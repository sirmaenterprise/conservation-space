package com.sirma.itt.seip.io;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} proxy that can close other resources when the stream is closed.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 18/05/2017
 */
public class ChainClosingInputStream extends FilterInputStream {

	private final Closeable closeHandle;

	/**
	 * Creates a <code>FilterInputStream</code> by assigning the argument <code>in</code> to the field
	 * <code>this.in</code> so as to remember it for later use.
	 *
	 * @param in
	 *            the underlying input stream, or <code>null</code> if this instance is to be created without an
	 *            underlying stream
	 * @param closeHandle
	 *            the other resource to close
	 */
	public ChainClosingInputStream(InputStream in, Closeable closeHandle) {
		super(in);
		this.closeHandle = closeHandle;
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			if (closeHandle != null) {
				closeHandle.close();
			}
		}
	}

}
