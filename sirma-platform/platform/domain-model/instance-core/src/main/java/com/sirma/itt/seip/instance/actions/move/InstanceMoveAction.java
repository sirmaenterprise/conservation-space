package com.sirma.itt.seip.instance.actions.move;

import java.io.Serializable;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.actions.InstanceOperations;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.models.ErrorData;

/**
 * Instance move action executor.
 *
 * @author nvelkov
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 100)
public class InstanceMoveAction implements Action<MoveActionRequest> {

	@Inject
	private InstanceOperations operationInvoker;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private InstanceContextInitializer contextInitializer;

	@Override
	public String getName() {
		return MoveActionRequest.OPERATION_NAME;
	}

	@Override
	public Instance perform(MoveActionRequest request) {
		if (request == null || request.getTargetId() == null || request.getDestinationId() == null) {
			throw new EmfRuntimeException("The request object is null.");
		}

		Instance targetInstance = getInstance(request.getTargetId());
		Instance destination = getInstance(request.getDestinationId());
		String userOperation = request.getUserOperation();
		Operation operation = new Operation(userOperation, true);
		operationInvoker.invokeMove(destination, operation, targetInstance);
		return targetInstance;
	}

	private Instance getInstance(Serializable id) {
		Instance instance = instanceTypeResolver.resolveReference(id).map(InstanceReference::toInstance).orElseThrow(
				() -> new ResourceException(Status.NOT_FOUND, new ErrorData("Instance with id " + id + " not found"),
						null));
		contextInitializer.restoreHierarchy(instance);
		return instance;
	}
}
