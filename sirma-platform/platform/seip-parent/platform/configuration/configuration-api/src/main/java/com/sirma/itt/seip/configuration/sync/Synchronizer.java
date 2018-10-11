package com.sirma.itt.seip.configuration.sync;

/**
 * Provides means to implement synchronization for configuration update. This should be used only by the configuration
 * and not by the configuration client.
 *
 * @author BBonev
 */
public interface Synchronizer {

	/**
	 * Begin read.
	 */
	void beginRead();

	/**
	 * End read.
	 */
	void endRead();

	/**
	 * Begin write.
	 */
	void beginWrite();

	/**
	 * End write.
	 */
	void endWrite();
}
