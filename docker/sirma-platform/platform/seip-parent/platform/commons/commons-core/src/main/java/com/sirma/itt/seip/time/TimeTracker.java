package com.sirma.itt.seip.time;

import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Keeps track of time intervals.
 *
 * @author BBonev
 */
public class TimeTracker implements Serializable {

	private static final long serialVersionUID = -475056806106476623L;
	private Long first = null;
	private Deque<Long> times = null;

	/**
	 * Begins the time tracking.
	 *
	 * @return the time tracker
	 */
	public TimeTracker begin() {
		if (first == null) {
			first = System.currentTimeMillis();
		} else if (times == null) {
			times = new LinkedList<>();
			times.push(first);
			times.push(System.currentTimeMillis());
		} else {
			times.push(System.currentTimeMillis());
		}
		return this;
	}

	/**
	 * Stops the time tracking. You can call the {@link #stop()} method the same number of times the {@link #begin()}
	 * method was called.
	 *
	 * @return the time between the begin and stop calls in milliseconds
	 */
	public long stop() {
		if (times == null) {
			if (first == null) {
				return -1;
			}
			long local = first;
			first = null;
			return System.currentTimeMillis() - local;
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
		if (times == null) {
			if (first == null) {
				return -1;
			}
			return System.currentTimeMillis() - first;
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
	 * Checks if current tracker instance has been started.
	 *
	 * @return true, if is started
	 */
	public boolean isStarted() {
		return first != null || times != null;
	}

	/**
	 * Checks if is ended.
	 *
	 * @return true, if is ended
	 */
	public boolean isEnded() {
		return first == null;
	}

	/**
	 * Restarts the current tracker and returns the current progress. The method will return the current elapsed time
	 * and will start counting again.
	 *
	 * @return the elapsed time from the last calling of {@link #begin()}
	 */
	public double restart() {
		long elapsed = stop();
		begin();
		return elapsed;
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
