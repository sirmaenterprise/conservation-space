package com.sirma.itt.seip.instance.lock.action;

import java.io.Serializable;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.models.ErrorData;

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

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public String getName() {
		return LockRequest.LOCK;
	}

	@Override
	public Object perform(LockRequest request) {
		Serializable id = request.getTargetId();
		InstanceReference reference = instanceTypeResolver
				.resolveReference(id)
					.orElseThrow(() -> new ResourceException(Status.NOT_FOUND,
							new ErrorData("Could not load instance with id: " + id), null));

		return lockService.lock(reference, request.getLockType());
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(LockRequest request) {
		return false;
	}
}
