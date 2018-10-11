package com.sirma.itt.seip.instance.lock.action;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.lock.LockInfo;
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

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public String getName() {
		return UnlockRequest.UNLOCK;
	}

	@Override
	public Object perform(UnlockRequest request) {
		Serializable targetId = request.getTargetId();
		return instanceTypeResolver
				.resolveReference(targetId)
					.map(lockService::unlock)
					.orElse(new LockInfo());
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(UnlockRequest request) {
		return false;
	}
}
