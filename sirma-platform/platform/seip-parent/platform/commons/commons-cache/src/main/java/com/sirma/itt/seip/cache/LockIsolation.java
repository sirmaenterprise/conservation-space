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
}
