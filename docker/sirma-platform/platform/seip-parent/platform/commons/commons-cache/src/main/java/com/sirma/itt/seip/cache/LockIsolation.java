/**
 *
 */
package com.sirma.itt.seip.cache;

/**
 * Defines the possible cache locking isolation
 *
 * @author BBonev
 */
public enum LockIsolation {
	NONE, SERIALIZABLE, REPEATABLE_READ, READ_COMMITTED, READ_UNCOMMITTED;

	public static LockIsolation parse(String isolationMode) {
		if (isolationMode == null) {
			return null;
		}
		switch (isolationMode.toUpperCase()) {
			case "NONE": return NONE;
			case "SERIALIZABLE": return SERIALIZABLE;
			case "REPEATABLE_READ": return REPEATABLE_READ;
			case "READ_COMMITTED": return READ_COMMITTED;
			case "READ_UNCOMMITTED": return READ_UNCOMMITTED;
			default: return null;
		}
	}
}
