/**
 *
 */
package com.sirma.itt.seip.cache;

import com.sirma.itt.seip.annotation.Documentation;

/**
 * Defines cache locking control.
 *
 * @author BBonev
 */
public @interface Locking {

	/**
	 * This attribute the cache locking isolation level. Allowable values are NONE, SERIALIZABLE, REPEATABLE_READ,
	 * READ_COMMITTED, READ_UNCOMMITTED.
	 *
	 * @return the lock isolation mode
	 */
	@Documentation("This attribute the cache locking isolation level. Allowable values are NONE, SERIALIZABLE, REPEATABLE_READ, READ_COMMITTED, READ_UNCOMMITTED.")
	LockIsolation isolation() default LockIsolation.NONE;

	/**
	 * If true, a pool of shared locks is maintained for all entries that need to be locked. Otherwise, a lock is
	 * created per entry in the cache. Lock striping helps control memory footprint but may reduce concurrency in the
	 * system.
	 * <br>
	 * http://infinispan.org/docs/stable/user_guide/user_guide.html#lock_striping
	 *
	 * @return if lock striping is enabled
	 */
	@Documentation("If true, a pool of shared locks is maintained for all entries that need to be locked. Otherwise, a lock is created per entry in the cache. Lock striping helps control memory footprint but may reduce concurrency in the system.")
	boolean striping() default false;

	/**
	 * This attribute configures the maximum time to attempt a particular lock acquisition.
	 *
	 * @return the lock acquisition time
	 */
	@Documentation("This attribute configures the maximum time to attempt a particular lock acquisition.")
	int acquireTimeout() default 5000;

	/**
	 * This attribute is used to configure the concurrency level. Adjust this value according to the number of
	 * concurrent threads interacting with Infinispan.
	 *
	 * @return the concurrency level
	 */
	@Documentation("This attribute is used to configure the concurrency level. Adjust this value according to the number of concurrent threads interacting with Infinispan.")
	int concurrencyLevel() default 32;
}
