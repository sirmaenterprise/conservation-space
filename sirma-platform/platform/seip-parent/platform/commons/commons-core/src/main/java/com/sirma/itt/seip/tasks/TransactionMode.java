package com.sirma.itt.seip.tasks;

/**
 * The supported transaction modes when running scheduler task.
 *
 * @author BBonev
 */
public enum TransactionMode {

	/** The required. */
	REQUIRED, /** The requires new. */
	REQUIRES_NEW, /** The not supported. */
	NOT_SUPPORTED;
}
