package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.isActivity;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.bpm.camunda.properties.BPMTaskProperties;
import com.sirmaenterprise.sep.bpm.exception.BPMException;

/**
 * Action implementation that can execute the {@link BPMClaimRequest.CLAIM_OPERATION} action.<br>
 * The provided {@link BPMClaimRequest} should contain a task instance that should be used as source value.<br>
 * Will claim camunda task and will add task assignee.
 * 
 * @author Hristo Lungov
 */
@Extension(target = Action.TARGET_NAME, order = 602)
public class BPMClaimAction extends BPMTransitionAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public String getName() {
		return BPMClaimRequest.CLAIM_OPERATION;
	}

	@Override
	protected Object executeBPMAction(BPMTransitionRequest request) throws BPMException {
		LOGGER.debug("Going to claim task based on request '{}'", request);
		Instance taskInstance = request.getTargetReference().toInstance();
		if (isActivity(taskInstance)) {
			Serializable currentUser = securityContext.getAuthenticated().getSystemId();
			camundaBPMNService.claimTask(taskInstance, currentUser.toString());
			taskInstance.add(BPMTaskProperties.TASK_ASSIGNEE, currentUser);
			return Arrays.asList(
					domainInstanceService.save(InstanceSaveContext.create(taskInstance, request.toOperation())));

		}
		return Collections.emptyList();
	}

}
