package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.isActivity;
import static com.sirmaenterprise.sep.bpm.camunda.util.BPMActivityUtil.isActivityInProgress;

import java.lang.invoke.MethodHandles;
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
 * Action implementation that can execute the {@link BPMReleaseRequest.RELEASE_OPERATION} action.<br>
 * The provided {@link BPMReleaseRequest} should contain a task instance that should be used as source value.<br>
 * Will release camunda task and will remove task assignee.
 *
 * @author Hristo Lungov
 */
@Extension(target = Action.TARGET_NAME, order = 603)
public class BPMReleaseAction extends BPMTransitionAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public String getName() {
		return BPMReleaseRequest.RELEASE_OPERATION;
	}

	@Override
	protected Object executeBPMAction(BPMTransitionRequest request) throws BPMException {
		LOGGER.debug("Going to release task based on request '{}'", request);
		Instance taskInstance = request.getTargetReference().toInstance();
		if (isActivity(taskInstance) && isActivityInProgress(taskInstance)) {
			camundaBPMNService.releaseTask(taskInstance);
			taskInstance.remove(BPMTaskProperties.TASK_ASSIGNEE);
			domainInstanceService.save(InstanceSaveContext.create(taskInstance, request.toOperation()));
		}
		return Collections.emptyList();
	}
}
