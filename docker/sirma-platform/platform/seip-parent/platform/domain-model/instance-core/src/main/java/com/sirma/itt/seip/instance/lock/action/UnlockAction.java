package com.sirma.itt.seip.instance.lock.action;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Performs unlock operation for the instance.
 *
 * @see LockService#unlock
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 45)
public class UnlockAction implements Action<UnlockRequest> {

	@Inject
	private LockService lockService;

	@Override
	public String getName() {
		return UnlockRequest.UNLOCK;
	}

	@Override
	public Object perform(UnlockRequest request) {
		return lockService.unlock(request.getTargetReference());
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(UnlockRequest request) {
		return false;
	}
}
