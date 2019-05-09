package com.sirma.itt.seip.instance.actions.move;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.actions.InstanceOperations;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Instance move action executor.
 *
 * @author nvelkov
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 100)
public class InstanceMoveAction implements Action<MoveActionRequest> {

	@Inject
	private InstanceOperations operationInvoker;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	public String getName() {
		return MoveActionRequest.OPERATION_NAME;
	}

	@Override
	public Instance perform(MoveActionRequest request) {
		Serializable newParentId = request.getDestinationId();
		Instance destination = newParentId != null ? domainInstanceService.loadInstance(newParentId.toString()) : null;
		Instance target = request.getTargetReference().toInstance();
		operationInvoker.invokeMove(destination, new Operation(request.getUserOperation(), true), target);
		return target;
	}
}
