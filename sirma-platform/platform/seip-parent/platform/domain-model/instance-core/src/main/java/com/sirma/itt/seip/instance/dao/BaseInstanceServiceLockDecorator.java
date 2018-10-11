package com.sirma.itt.seip.instance.dao;

import java.util.Collection;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Delegate that verifies the lock status of an instance before.
 * <p>
 * <b> Note that this decorator does not work properly while the intercepted service is a EJB bean. Because the security
 * interceptor is not called for the EJB beans while there is a decorator defined.</b>
 *
 * @author BBonev
 */
public abstract class BaseInstanceServiceLockDecorator implements InstanceService {

	@Inject
	private LockService lockService;

	@Override
	public Instance save(Instance instance, Operation operation) {
		verifyAllowedToModify(instance.toReference());
		return getDelegate().save(instance, operation);
	}

	@Override
	public Collection<String> delete(Instance instance, Operation operation, boolean permanent) {
		verifyAllowedToModify(instance.toReference());
		return getDelegate().delete(instance, operation, permanent);
	}

	@Override
	public Instance publish(Instance instance, Operation operation) {
		verifyAllowedToModify(instance.toReference());
		return getDelegate().publish(instance, operation);
	}

	/**
	 * Verify that current user is allowed to modify the instance represented by the given instance reference
	 *
	 * @param reference
	 *            an {@link InstanceReference} object that will be checked.
	 */
	protected void verifyAllowedToModify(InstanceReference reference) {
		if (!lockService.isAllowedToModify(reference)) {
			throw createLockException(lockService.lockStatus(reference));
		}
	}

	protected InstanceService getDelegate() {
		// the method is not abstract because the api that resolve decorator beans interpret it as a service bean and
		// the implementations does not see it
		throw new UnsupportedOperationException("Implement the method BaseInstanceServiceLockDecorator.getDelegate()");
	}

	private static LockException createLockException(LockInfo lockStatus) {
		return new LockException(lockStatus, "Cannot modify instance already locked by " + lockStatus.getLockedBy());
	}

}
