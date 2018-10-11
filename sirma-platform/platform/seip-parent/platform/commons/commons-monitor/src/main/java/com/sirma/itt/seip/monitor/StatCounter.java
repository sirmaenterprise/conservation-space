package com.sirma.itt.seip.monitor;

/**
 * Statistics counter to determine the currently active items
 *
 * @author BBonev
 */
public interface StatCounter {

	/**
	 * Increment.
	 */
	void increment();

	/**
	 * Decrement.
	 */
	void decrement();

	/**
	 * Increment.
	 *
	 * @param amount
	 *            the amount
	 */
	void increment(int amount);

	/**
	 * Decrement.
	 *
	 * @param amount
	 *            the amount
	 */
	void decrement(int amount);
}
