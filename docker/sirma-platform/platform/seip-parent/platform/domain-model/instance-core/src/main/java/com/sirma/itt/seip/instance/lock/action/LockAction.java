package com.sirma.itt.seip.instance.lock.action;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Performs lock operation for the instance.
 *
 * @see LockService#lock
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 40)
public class LockAction implements Action<LockRequest> {

	@Inject
	private LockService lockService;

	@Override
	public String getName() {
		return LockRequest.LOCK;
	}

	@Override
	public Object perform(LockRequest request) {
		return lockService.lock(request.getTargetReference(), request.getLockType());
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(LockRequest request) {
		return false;
	}
}
