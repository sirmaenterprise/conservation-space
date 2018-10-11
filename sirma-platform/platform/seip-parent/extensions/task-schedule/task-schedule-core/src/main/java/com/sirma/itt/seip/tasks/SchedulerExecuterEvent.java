package com.sirma.itt.seip.tasks;

import java.util.function.Supplier;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.concurrent.CompletableOperation;
import com.sirma.itt.seip.concurrent.event.BaseCallableEvent;

/**
 * System event that is fired to trigger synchronous action execution in specific security context.
 *
 * @author BBonev
 */
@Documentation("System event that is fired to trigger synchronous action execution in specific security context")
public class SchedulerExecuterEvent extends BaseCallableEvent{

	/**
	 * Instantiates a new scheduler executer event.
	 *
	 * @param callable
	 *            the callable
	 * @param completableOperation
	 *            the completable operation
	 */
	public SchedulerExecuterEvent(Supplier<?> callable, CompletableOperation<Object> completableOperation) {
		super(callable, completableOperation);
	}
}