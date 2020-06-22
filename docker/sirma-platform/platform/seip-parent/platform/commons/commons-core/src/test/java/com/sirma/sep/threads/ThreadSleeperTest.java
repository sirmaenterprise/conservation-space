package com.sirma.sep.threads;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * Test for {@link ThreadSleeper}.
 *
 * @author A. Kunchev
 */
public class ThreadSleeperTest {

	private ThreadSleeper sleeper = new ThreadSleeper();

	private TestObject object = spy(new TestObject());

	@Test
	public void sleepFor_200_miliseconds() throws Exception {
		callInNewThread(200, TimeUnit.MILLISECONDS);
		// 100+ millis in order to allow the other thread to start
		verify(object, after(300)).call();
	}

	private void callInNewThread(long after, TimeUnit unit) {
		new Thread(() -> {
			try {
				if (unit == null) {
					sleeper.sleepFor(after);
				} else {
					sleeper.sleepFor(after, unit);
				}
				object.call();
			} catch (Exception e) {
				fail("There was an error while the test was executed. Error: " + e.getMessage());
			}
		}).start();
	}

	@Test
	public void sleepFor_1_second() throws Exception {
		callInNewThread(1, null);
		// 100+ millis in order to allow the other thread to start
		verify(object, after(1100)).call();
	}

	private class TestObject {
		TestObject() {}

		void call() {
			// What's up
		}
	}
}