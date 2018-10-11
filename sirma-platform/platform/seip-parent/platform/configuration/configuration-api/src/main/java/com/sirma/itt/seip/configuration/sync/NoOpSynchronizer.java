package com.sirma.itt.seip.configuration.sync;

/**
 * Synchronizer that does not synchronize anything
 *
 * @author BBonev
 */
public class NoOpSynchronizer implements Synchronizer {

	public static final Synchronizer INSTANCE = new NoOpSynchronizer();

	/**
	 * Begin read.
	 */
	@Override
	public void beginRead() {
		// nothing to do
	}

	/**
	 * End read.
	 */
	@Override
	public void endRead() {
		// nothing to do
	}

	/**
	 * Begin write.
	 */
	@Override
	public void beginWrite() {
		// nothing to do
	}

	/**
	 * End write.
	 */
	@Override
	public void endWrite() {
		// nothing to do
	}

}
