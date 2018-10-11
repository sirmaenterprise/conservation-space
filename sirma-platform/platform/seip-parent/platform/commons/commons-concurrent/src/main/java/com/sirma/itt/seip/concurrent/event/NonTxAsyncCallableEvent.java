package com.sirma.itt.seip.concurrent.event;

import java.util.function.Supplier;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.concurrent.CompletableOperation;

/**
 * Event that can be used for asynchronous task execution on the EJB thread pool without transaction support. The event
 * could not be used to write to any persistent layer that require transaction environment to operate. For transaction
 * support use the {@link TxAsyncCallableEvent}.
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
	public NonTxAsyncCallableEvent(Supplier<?> callable) {
		super(callable);
	}

	/**
	 * Instantiates a new non tx async callable event.
	 *
	 * @param callable
	 *            the callable
	 * @param completableOperaton
	 *            the completable operaton
	 */
	public NonTxAsyncCallableEvent(Supplier<?> callable, CompletableOperation<Object> completableOperaton) {
		super(callable, completableOperaton);
	}
}
