package com.sirma.itt.seip.instance.actions.save;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Executes instance create or instance update. The request parameter contains all the information that is needed to
 * execute successfully the operation.
 *
 * @author A. Kunchev
 * @see Actions
 * @see DomainInstanceService#save(InstanceSaveContext)
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 90)
public class SaveAction implements Action<SaveRequest> {

	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	public String getName() {
		return SaveRequest.OPERATION_NAME;
	}

	@Override
	public Instance perform(SaveRequest request) {
		return domainInstanceService.save(request.toSaveContext());
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(SaveRequest request) {
		// instance is already locked or no need locking
		return false;
	}
}