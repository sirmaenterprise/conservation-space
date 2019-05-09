package com.sirma.sep.model.management;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.domain.exceptions.TimeoutException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;

/**
 * Model operation synchronization controller. Provides means to synchronize asynchronous operations (like apply or
 * deploy changes). <br>
 * The instance can track different requests registered by {@link #acquire()}. The requests should be marked for
 * completed  by calling the method {@link #release(String)}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/10/2018
 */
@Singleton
public class ModelOpSynchronization {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final RandomStringGenerator REQUEST_ID_GENERATOR = new RandomStringGenerator.Builder().withinRange(
			'a', 'z').build();

	@Inject
	private ContextualConcurrentMap<String, CountDownLatch> waitingRequests;

	/**
	 * Generate a random request id that can be used for calling the rest of the instance methods.
	 *
	 * @return a random generated id
	 */
	private static String generateRequestId() {
		return REQUEST_ID_GENERATOR.generate(10);
	}

	/**
	 * Acquire an operation token for tracking.
	 *
	 * @return a token that can be used to track an operation progress.
	 */
	public String acquire() {
		String requestId = generateRequestId();
		waitingRequests.put(requestId, new CountDownLatch(1));
		return requestId;
	}

	/**
	 * Blocking operation! Waits for operation represented by the given request to become available. If the operation
	 * has already been completed the method returns immediately. If the request is not processed within the specified
	 * time interval then the method will fail with {@link TimeoutException}
	 *
	 * @param requestId the request id to check for
	 * @param timeout the time to wait
	 * @param unit the time unit of the specified timeout
	 */
	public void waitForRequest(String requestId, long timeout, TimeUnit unit) {
		try {
			CountDownLatch latch = waitingRequests.get(requestId);
			if (latch == null) {
				// probably already updated, we can throw specific exception if needed
				return;
			}
			LOGGER.debug("Waiting for operation with id {} to complete", requestId);
			if (!(latch.await(timeout, unit))) {
				throw new TimeoutException("Model op did not finish in time for request=" + requestId);
			}
			LOGGER.debug("Operation with id {} completed", requestId);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RollbackedRuntimeException(e);
		}
	}

	/**
	 * Mark an operation represented by the given request if for completed. An operation cannot be completed more than
	 * once. If operation is attempted to be completed twice a {@link IllegalStateException} will be thrown the second
	 * time and any other time.<br>
	 * The method also notifies all waiting calls on {@link #waitForRequest(String, long, TimeUnit)} that the specified
	 * operation has been completed.
	 *
	 * @param requestId to mark as completed.
	 */
	public void release(String requestId) {
		CountDownLatch latch = waitingRequests.remove(requestId);
		if (latch == null) {
			LOGGER.warn("Unknown request id {}", requestId);
		} else {
			LOGGER.debug("Releasing waiting requests for request id {}", requestId);
			latch.countDown();
		}
	}
}
