/**
 *
 */
package com.sirma.itt.seip.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link UncaughtExceptionHandler} that prints them to a logger.
 *
 * @author BBonev
 */
public class LoggingExceptionHandler implements UncaughtExceptionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger("UncaughtExceptions");

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOGGER.error("Uncought exception ", e);
	}

}
