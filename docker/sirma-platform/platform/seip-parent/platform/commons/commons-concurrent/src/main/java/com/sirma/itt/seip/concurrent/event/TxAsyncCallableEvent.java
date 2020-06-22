package com.sirma.itt.seip.concurrent.event;

import java.util.function.Supplier;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.concurrent.CompletableOperation;

/**
 * Event that can be used for asynchronous task execution on the EJB thread pool in transaction environment. The payload
 * of the event will be executed in full transactional environment. Note that this use of this event will create new
 * transaction for every call. If transaction are not needed for executed operation consider using
 * {@link NonTxAsyncCallableEvent}.
 *
 * @author BBonev
 */
@Documentation("Event that can be used for asynchronous task execution on the EJB thread pool in transaction"
		+ " environment. The payload of the event will be executed in full transactional environment.")
public class TxAsyncCallableEvent extends BaseCallableEvent {

	/**
	 * Instantiates a new transactional asynchronous callable event.
	 *
	 * @param callable
	 *            the callable
	 */
	public TxAsyncCallableEvent(Supplier<?> callable) {
		super(callable);
	}

	/**
	 * Instantiates a new tx async callable event.
	 *
	 * @param callable
	 *            the callable
	 * @param completableOperation
	 *            the completable operation
	 */
	public TxAsyncCallableEvent(Supplier<?> callable, CompletableOperation<Object> completableOperation) {
		super(callable, completableOperation);
	}
}
