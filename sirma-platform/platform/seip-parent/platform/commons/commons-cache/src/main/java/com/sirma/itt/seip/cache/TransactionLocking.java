package com.sirma.itt.seip.cache;

/**
 * Defines the possible transaction locking modes.
 *
 * http://infinispan.org/docs/stable/user_guide/user_guide.html#what_do_i_need_pessimistic_or_optimistic_transactions
 *
 * @author BBonev
 */
public enum TransactionLocking {

	/** The optimistic. */
	OPTIMISTIC, /** The pessimistic. */
	PESSIMISTIC;
}
