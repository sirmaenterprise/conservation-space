package com.sirma.itt.seip.cache;

/**
 * The cache transaction modes.
 *
 * @author BBonev
 */
public enum CacheTransactionMode {

	/** The full transactional support. */
	FULL_XA, /** The non xa. */
	NON_XA, /** The non durable xa. */
	NON_DURABLE_XA, /** No transaction support. */
	NONE;
}
