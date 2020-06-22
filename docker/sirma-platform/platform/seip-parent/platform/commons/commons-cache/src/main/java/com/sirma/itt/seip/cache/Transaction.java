package com.sirma.itt.seip.cache;

import com.sirma.itt.seip.annotation.Documentation;

/**
 * The cache transaction configuration
 *
 * @author BBonev
 */
public @interface Transaction {

	/**
	 * This attribute configures the transaction mode, setting the cache transaction mode to one of NONE, NON_XA,
	 * NON_DURABLE_XA, FULL_XA.
	 *
	 * @return the cache participation mode in the transactions
	 */
	@Documentation("This attribute configures the transaction mode, setting the cache transaction mode to one of "
			+ "NONE, NON_XA, NON_DURABLE_XA, FULL_XA.")
	CacheTransactionMode mode() default CacheTransactionMode.NONE;

	/**
	 * This attribute configures the locking mode for this cache, one of OPTIMISTIC or PESSIMISTIC.
	 * <br>
	 * http://infinispan.org/docs/stable/user_guide/user_guide.html#tx:locking
	 * 
	 * @return the transaction locking mode
	 */
	@Documentation("This attribute configures the locking mode for this cache, one of OPTIMISTIC or PESSIMISTIC.")
	TransactionLocking locking() default TransactionLocking.OPTIMISTIC;

	/**
	 * If there are any ongoing transactions when a cache is stopped, Infinispan waits for ongoing remote and local
	 * transactions to finish. The amount of time to wait for is defined by the cache stop timeout.
	 * 
	 * @return stop timeout in milliseconds
	 */
	@Documentation("If there are any ongoing transactions when a cache is stopped, Infinispan waits for ongoing "
			+ "remote and local transactions to finish. The amount of time to wait for is defined by the cache stop timeout.")
	int stopTimeout() default 30000;
}
