package com.sirma.itt.seip.instance.actions.move;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
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
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public String getName() {
		return MoveActionRequest.OPERATION_NAME;
	}

	@Override
	public Instance perform(MoveActionRequest request) {
		Instance target = getInstance(request.getTargetId());
		Serializable newParentId = request.getDestinationId();
		Instance destination = newParentId != null ? getInstance(newParentId) : null;
		Operation operation = new Operation(request.getUserOperation(), true);
		operationInvoker.invokeMove(destination, operation, target);
		return target;
	}

	private Instance getInstance(Serializable id) {
		return instanceTypeResolver.resolveReference(id).map(InstanceReference::toInstance).orElseThrow(
				() -> new InstanceNotFoundException(id));
	}
}
