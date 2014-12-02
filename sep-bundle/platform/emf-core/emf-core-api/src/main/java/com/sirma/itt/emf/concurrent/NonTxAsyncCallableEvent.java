package com.sirma.itt.emf.concurrent;

import java.util.concurrent.Callable;

import com.sirma.itt.emf.security.context.SecurityContext;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event that can be used for asynchronous task execution on the EJB thread pool without transaction
 * support. The event could not be used to write to any persistent layer that require transaction
 * environment to operate. For transaction support use the {@link TxAsyncCallableEvent}.
 * 
 * @author BBonev
 */
@Documentation("Event that can be used for asynchronous task execution on the EJB thread pool without transaction"
		+ " support. The event could not be used to write to any persistent layer that require transaction"
		+ " environment to operate. For transaction support use the {@link TxAsyncCallableEvent}.")
public class NonTxAsyncCallableEvent extends BaseCallableEvent {

	/**
	 * Instantiates a new non transaction async callable event.
	 * 
	 * @param callable
	 *            the callable
	 */
	public NonTxAsyncCallableEvent(Callable<?> callable) {
		this(callable, null);
	}

	/**
	 * Instantiates a new non transaction async callable event.
	 * 
	 * @param callable
	 *            the callable
	 * @param securityContext
	 *            the security context
	 */
	public NonTxAsyncCallableEvent(Callable<?> callable, SecurityContext securityContext) {
		super(callable, securityContext);
	}
}
