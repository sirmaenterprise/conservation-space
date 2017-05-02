package com.sirma.itt.seip.instance.dao;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

/**
 * Delegate that verifies the lock status of an instance before write operations.
 * <p>
 * <b> Note that this decorator does not work properly while the intercepted service is a EJB bean. Because the security
 * interceptor is not called for the EJB beans while there is a decorator defined.</b>
 *
 * @author BBonev
 */
@Decorator
@Priority(Interceptor.Priority.APPLICATION)
public abstract class InstanceServiceLockDecorator extends BaseInstanceServiceLockDecorator {

	@Inject
	@Delegate
	private InstanceService delegate;

	@Override
	protected InstanceService getDelegate() {
		return delegate;
	}
}
