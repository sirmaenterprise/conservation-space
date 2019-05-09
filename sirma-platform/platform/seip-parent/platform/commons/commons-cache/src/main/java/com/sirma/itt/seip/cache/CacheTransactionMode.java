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

	public static CacheTransactionMode parse(String mode) {
		if (mode == null) {
			return null;
		}
		switch (mode.toUpperCase()) {
			case "FULL_XA": return FULL_XA;
			case "NON_XA": return NON_XA;
			case "NON_DURABLE_XA": return NON_DURABLE_XA;
			case "NONE": return NONE;
			default: return null;
		}
	}
}
