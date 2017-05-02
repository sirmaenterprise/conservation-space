package com.sirma.itt.seip.instance.actions.save;

import java.util.Date;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Executes instance create or instance update. The request parameter contains all the information that is needed to
 * execute successfully the operation.
 *
 * @author A. Kunchev
 * @see Actions
 * @see DomainInstanceService#save(Instance, Operation, Date)
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 90)
public class CreateOrUpdateAction implements Action<CreateOrUpdateRequest> {

	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	public String getName() {
		return CreateOrUpdateRequest.OPERATION_NAME;
	}

	@Override
	public Instance perform(CreateOrUpdateRequest request) {
		// we are building new operation, because request#getOperation returns dummy operation id
		Operation operation = new Operation(request.getUserOperation(), true);
		return domainInstanceService
				.save(InstanceSaveContext.create(request.getTarget(), operation, request.getVersionCreatedOn()));
	}

}
