package com.sirma.sep.content.preview.util;

import org.springframework.util.StopWatch;

/**
 * Utility for tracking time.
 *
 * @author Mihail Radkov
 */
public class TimeTracker extends StopWatch {

	private TimeTracker() {
		// Hidden utility constructor
	}

	/**
	 * Constructs and starts a {@link TimeTracker}.
	 *
	 * @return started {@link TimeTracker}
	 */
	public static TimeTracker create() {
		TimeTracker tracker = new TimeTracker();
		tracker.start();
		return tracker;
	}

	/**
	 * Stops the {@link TimeTracker} and returns the elapsed time from the {@link TimeTracker#start()} in milliseconds.
	 *
	 * @return elapsed milliseconds
	 */
	public long stopInMs() {
		stop();
		return getTotalTimeMillis();
	}
}
