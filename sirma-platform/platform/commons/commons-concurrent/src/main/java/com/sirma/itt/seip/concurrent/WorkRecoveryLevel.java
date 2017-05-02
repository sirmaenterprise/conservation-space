package com.sirma.itt.seip.concurrent;

/**
 * When executing a collection of units of work the outcome of execution could be revert all units or only the failed
 * units. This enum is to determine the recovery level required.
 *
 * @author BBonev
 */
public enum WorkRecoveryLevel {

	/** Revert all units of work. */
	ALL, /** Revert only the failed units. */
	UNIT;
}
