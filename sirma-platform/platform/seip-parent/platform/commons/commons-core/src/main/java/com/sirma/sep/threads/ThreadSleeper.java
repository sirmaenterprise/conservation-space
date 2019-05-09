package com.sirma.sep.threads;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

/**
 * Provides means to pause the execution of process. It basically warps {@link Thread#sleep(long)} functionality through
 * {@link TimeUnit#sleep(long)}, which allows more convenient way to manage the delay.
 *
 * @author A. Kunchev
 * @see TimeUnit#sleep(long)
 * @see Thread#sleep(long)
 */
@Singleton
@SuppressWarnings("static-method")
public class ThreadSleeper {

	/**
	 * Pauses the execution of the current {@link Thread} for the specific time period.
	 *
	 * @param amount that should be waited before resuming execution
	 * @param unit represents time duration for the given amount
	 * @throws InterruptedException when the current {@link Thread} is interrupted while sleeping
	 */
	public void sleepFor(long amount, TimeUnit unit) throws InterruptedException {
		Objects.requireNonNull(unit, "Time unit is required.");
		unit.sleep(amount);
	}

	/**
	 * Pauses the execution of the current {@link Thread} for the specific amount of seconds.
	 *
	 * @param seconds amount of seconds that should be waited before resuming execution
	 * @throws InterruptedException when the current {@link Thread} is interrupted while sleeping
	 */
	public void sleepFor(long seconds) throws InterruptedException {
		sleepFor(seconds, TimeUnit.SECONDS);
	}
}