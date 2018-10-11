package com.sirma.itt.seip.instance.actions.revert;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.revert.RevertContext;
import com.sirma.itt.seip.plugin.Extension;

/**
 * {@link Action} implementation that executes revert operation for specific version. This action will replace most of
 * the data of the current instance with the data from the version. Current instance data is saved as new version. If
 * the execution fails, all changes are rollbacked.
 *
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 170)
public class RevertVersionAction implements Action<RevertVersionRequest> {

	@Inject
	private InstanceVersionService instanceVersionService;

	@Override
	public String getName() {
		return InstanceVersionService.REVERT_VERSION_SERVER_OPERATION;
	}

	@Override
	public Object perform(RevertVersionRequest request) {
		RevertContext revertContext = RevertContext.create(request.getTargetId()).setOperation(request.toOperation());
		return instanceVersionService.revertVersion(revertContext);
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(RevertVersionRequest request) {
		// the action works with internal locking
		return false;
	}
}
