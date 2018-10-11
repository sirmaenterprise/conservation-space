package com.sirma.itt.seip.util;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.sirma.itt.seip.time.TimeTracker;

/**
 * The Class TimeTrackerTest.
 *
 * @author BBonev
 */
@Test
public class TimeTrackerTest {

	/**
	 * Test begin stop.
	 */
	public void testBeginStop() {
		TimeTracker tracker = new TimeTracker();
		tracker.begin();
		assertTrue(tracker.stop() >= 0);
	}

	/**
	 * Test multiple iterations.
	 */
	public void testMultipleIterations() {
		TimeTracker tracker = new TimeTracker();
		tracker.begin();
		tracker.begin();
		tracker.begin();

		assertTrue(tracker.stop() >= 0);
		assertTrue(tracker.stop() >= 0);
		assertTrue(tracker.stop() >= 0);
	}

	/**
	 * Test multiple iterations in seconds.
	 */
	public void testMultipleIterationsInSeconds() {
		TimeTracker tracker = new TimeTracker();
		tracker.begin();
		tracker.begin();
		tracker.begin();

		assertTrue(tracker.stopInSeconds() >= 0.0);
		assertTrue(tracker.stop() >= 0);
		assertTrue(tracker.stopInSeconds() >= 0.0);
	}

	/**
	 * Test elapse time.
	 */
	public void testElapseTime() {
		TimeTracker tracker = new TimeTracker();
		tracker.begin();

		assertTrue(tracker.elapsedTime() >= 0);
		assertTrue(tracker.elapsedTime() >= 0);
		assertTrue(tracker.elapsedTime() >= 0);
		assertTrue(tracker.stop() >= 0);
	}

	/**
	 * Test multi start elapse time.
	 */
	public void testMultiStartElapseTime() {
		TimeTracker tracker = new TimeTracker();
		tracker.begin();
		tracker.begin();

		assertTrue(tracker.elapsedTime() >= 0);
		assertTrue(tracker.stop() >= 0);

		tracker.begin();
		assertTrue(tracker.elapsedTime() >= 0);
		assertTrue(tracker.stop() >= 0);
		assertTrue(tracker.elapsedTime() >= 0);
		assertTrue(tracker.stop() >= 0);
	}

}
