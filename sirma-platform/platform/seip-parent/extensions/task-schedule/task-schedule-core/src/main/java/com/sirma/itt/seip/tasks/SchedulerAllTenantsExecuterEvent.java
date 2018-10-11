package com.sirma.itt.seip.tasks;

import java.util.function.Supplier;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.concurrent.CompletableOperation;
import com.sirma.itt.seip.concurrent.event.BaseCallableEvent;
import com.sirma.itt.seip.security.util.SecureEventAllTenants;

/**
 * System event that is fired to trigger synchronous action execution in the context of all tenants.
 *
 * @author nvelkov
 */
@Documentation("System event that is fired to trigger synchronous action execution in the context of all tenants")
public class SchedulerAllTenantsExecuterEvent extends BaseCallableEvent implements SecureEventAllTenants {

	/**
	 * Instantiates a new scheduler all tenants executer event.
	 *
	 * @param callable
	 *            the callable
	 * @param completableOperation
	 *            the completable operation
	 */
	public SchedulerAllTenantsExecuterEvent(Supplier<?> callable, CompletableOperation<Object> completableOperation) {
		super(callable, completableOperation);
	}
}