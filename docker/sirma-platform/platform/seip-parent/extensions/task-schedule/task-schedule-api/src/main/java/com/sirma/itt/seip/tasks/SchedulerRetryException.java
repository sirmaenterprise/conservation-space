package com.sirma.itt.seip.tasks;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception thrown from {@link SchedulerAction}s to indicate that the action conditions are not met and should be
 * retried. This exception will not trigger complete logging in comparison with all other exceptions that can be thrown
 * from an action.
 *
 * @author BBonev
 */
public class SchedulerRetryException extends EmfRuntimeException {

	private static final long serialVersionUID = 6850126686068537165L;

	/**
	 * Instantiate an instance with the given message to log
	 *
	 * @param message
	 * 		the message to log
	 */
	public SchedulerRetryException(String message) {
		super(message);
	}

	/**
	 * Instantiate an instance with the given message to log
	 *
	 * @param message
	 * 		the message to log
	 * @param e
	 * 		the cause
	 */
	public SchedulerRetryException(String message, Throwable e) {
		super(message, e);
	}

}
