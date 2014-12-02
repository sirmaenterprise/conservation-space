package com.sirma.itt.emf.concurrent;

import java.util.concurrent.Callable;

import com.sirma.itt.emf.security.context.SecurityContext;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event that can be used for asynchronous task execution on the EJB thread pool in transaction
 * environment. The payload of the event will be executed in full transactional environment. Note
 * that this use of this event will create new transaction for every call. If transaction are not
 * needed for executed operation consider using {@link NonTxAsyncCallableEvent}.
 * 
 * @author BBonev
 */
@Documentation("Event that can be used for asynchronous task execution on the EJB thread pool in transaction"
		+ " environment. The payload of the event will be executed in full transactional environment.")
public class TxAsyncCallableEvent extends BaseCallableEvent {

	/**
	 * Instantiates a new transaction async callable event.
	 * 
	 * @param callable
	 *            the callable
	 */
	public TxAsyncCallableEvent(Callable<?> callable) {
		this(callable, null);
	}

	/**
	 * Instantiates a new transaction async callable event.
	 * 
	 * @param callable
	 *            the callable
	 * @param securityContext
	 *            the security context
	 */
	public TxAsyncCallableEvent(Callable<?> callable, SecurityContext securityContext) {
		super(callable, securityContext);
	}
}
