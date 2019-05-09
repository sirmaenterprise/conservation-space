package com.sirma.sep.threads;

/**
 * Provides a way to interrupt the current thread. It basically warps {@link Thread#interrupt()}  functionality
 *
 * @author Radoslav Dimitrov
 */
public class ThreadInterrupter {

	/**
	 * Interrupts the current thread.
	 */
	public void interruptCurrentThread() {
		Thread.currentThread().interrupt();
	}
}
