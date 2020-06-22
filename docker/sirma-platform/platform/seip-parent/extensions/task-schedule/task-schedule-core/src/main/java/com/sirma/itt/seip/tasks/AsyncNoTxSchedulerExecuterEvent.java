package com.sirma.itt.seip.tasks;

import java.util.function.Supplier;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.concurrent.CompletableOperation;
import com.sirma.itt.seip.concurrent.event.BaseCallableEvent;
import com.sirma.itt.seip.security.util.SecureEventAllTenants;

/**
 * System event that is fired to trigger asynchronous action execution in no transactional context.
 *
 * @author BBonev
 */
@Documentation("System event that is fired to trigger asynchronous action execution in no transactional context.")
public class AsyncNoTxSchedulerExecuterEvent extends BaseCallableEvent implements SecureEventAllTenants {

	/**
	 * Instantiates a new async scheduler executer event.
	 *
	 * @param callable
	 *            the callable
	 * @param completableOperation
	 *            the completable operation
	 */
	@SuppressWarnings("unchecked")
	public <E> AsyncNoTxSchedulerExecuterEvent(Supplier<E> callable, CompletableOperation<E> completableOperation) {
		super(callable, (CompletableOperation<Object>) completableOperation);
	}
}