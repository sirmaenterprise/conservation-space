package com.sirma.itt.seip.instance.actions.transition;

import static com.sirma.itt.seip.instance.InstanceSaveContext.create;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Implements {@link Action}. Used to execute transition operations. The actual operation, which is executed is passed
 * as userOperation id, which comes with the request. Extracts the target instance form the passed id in the request and
 * then executes save with the builded {@link Operation}.
 *
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 10)
public class InstanceTransitionAction implements Action<TransitionActionRequest> {

	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	public String getName() {
		return TransitionActionRequest.OPERATION_NAME;
	}

	@Override
	public Instance perform(TransitionActionRequest request) {
		String userOperation = request.getUserOperation();
		if (StringUtils.isBlank(userOperation)) {
			throw new EmfRuntimeException("Failed to execute immediate operation: " + request.getUserOperation());
		}

		try {
			InstanceSaveContext context = create(request.getTargetInstance(), request.toOperation());
			return domainInstanceService.save(context);
		} catch (RuntimeException e) {
			throw new EmfRuntimeException("Problem occurred, while saving instance relations.", e);
		}
	}

}
