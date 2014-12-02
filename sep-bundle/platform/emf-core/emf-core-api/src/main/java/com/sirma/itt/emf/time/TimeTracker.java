package com.sirma.itt.emf.time;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Keeps track of time intervals.
 *
 * @author BBonev
 */
public class TimeTracker implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -475056806106476623L;
	/** The times. */
	private Deque<Long> times = new LinkedList<Long>();

	/**
	 * Begins the time tracking.
	 * 
	 * @return the time tracker
	 */
	public TimeTracker begin() {
		times.push(System.currentTimeMillis());
		return this;
	}

	/**
	 * Stops the time tracking. You can call the {@link #stop()} method the same
	 * number of times the {@link #begin()} method was called.
	 *
	 * @return the time between the begin and stop calls in milliseconds
	 */
	public long stop() {
		if (times.isEmpty()) {
			return -1;
		}
		return System.currentTimeMillis() - times.pop();
	}

	/**
	 * Stop the time tracking and return the result in seconds.
	 *
	 * @return the seconds between the last {@link #begin()} call
	 */
	public double stopInSeconds() {
		return stop() / 1000.0;
	}

	/**
	 * Returns the currently elapsed time in milliseconds if started or -1 if not
	 * 
	 * @return the time between the begin and current call in milliseconds
	 */
	public long elapsedTime() {
		if (times.isEmpty()) {
			return -1;
		}
		return System.currentTimeMillis() - times.peek();
	}

	/**
	 * Returns the currently elapsed time in seconds if started or -1 if not
	 * 
	 * @return the time between the begin and current call in seconds
	 */
	public double elapsedTimeInSeconds() {
		return elapsedTime() / 1000.0;
	}

	/**
	 * Creates new {@link TimeTracker} instance and starts the counting
	 * 
	 * @return the time tracker
	 */
	public static TimeTracker createAndStart() {
		return new TimeTracker().begin();
	}

}
