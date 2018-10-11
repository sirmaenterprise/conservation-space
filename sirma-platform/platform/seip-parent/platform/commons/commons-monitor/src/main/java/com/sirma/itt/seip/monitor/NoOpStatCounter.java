package com.sirma.itt.seip.monitor;

/**
 * No operation implementation for {@link StatCounter}.
 *
 * @author BBonev
 */
public class NoOpStatCounter implements StatCounter {

	@Override
	public void increment() {
		// nothing to do
	}

	@Override
	public void decrement() {
		// nothing to do
	}

	@Override
	public void increment(int amount) {
		// nothing to do
	}

	@Override
	public void decrement(int amount) {
		// nothing to do
	}

}
