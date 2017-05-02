package com.sirma.itt.seip.concurrent.event;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.concurrent.CompletableOperation;
import com.sirma.itt.seip.security.util.AbstractSecureEvent;

/**
 * Base event that can be used for asynchronous task execution on the EJB thread pool. The benefit the possible use of
 * full transaction support for the executed operation if needed. For concrete use see sub classes of this.
 *
 * @author BBonev
 */
@Documentation("Base event that can be used for asynchronous task execution on the EJB thread pool. The benefit"
		+ " the possible use of full transaction support for the executed operation if needed. For concrete use see sub classes of this.")
public abstract class BaseCallableEvent extends AbstractSecureEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final Supplier<?> callable;
	private final CompletableOperation<Object> completableOperaton;

	/**
	 * Instantiates a new base callable event.
	 *
	 * @param callable
	 *            the callable
	 */
	public BaseCallableEvent(Supplier<?> callable) {
		this.callable = callable;
		completableOperaton = null;
	}

	/**
	 * Instantiates a new base callable event.
	 *
	 * @param callable
	 *            the callable
	 * @param completableOperation
	 *            the completable operation
	 */
	public BaseCallableEvent(Supplier<?> callable, CompletableOperation<Object> completableOperation) {
		this.callable = callable;
		completableOperaton = completableOperation;
	}

	/**
	 * Execute the event payload in the security context initialized at event creation and update the passed callback if
	 * any.
	 */
	public void call() {
		try {
			Object result;
			// if there is active context use it
			if (getSecureExecutor() == null) {
				LOGGER.warn("Executing secure event of type {} but no secure executor is present.",
						getClass().getSimpleName());
				result = callable.get();
			} else {
				result = getSecureExecutor().execute(callable);
			}
			if (completableOperaton != null) {
				completableOperaton.completed(result);
			}
		} catch (RuntimeException e) {
			if (completableOperaton != null) {
				completableOperaton.failed(e);
			}
			LOGGER.error("Async operation failed due to : {}", e.getMessage(), e);
			throw e;
		}
	}
}
