package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import static com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService.isProcess;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.isActivity;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;
import com.sirmaenterprise.sep.bpm.exception.BPMException;

/**
 *
 * Action implementation that can execute the {@link BPMStopRequest#STOP_OPERATION} action.<br>
 * The provided {@link BPMStopRequest} should contain a workflow instance that should be used as source value
 *
 * @author hlungov
 */
@Extension(target = Action.TARGET_NAME, order = 604)
public class BPMStopAction extends BPMTransitionAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public String getName() {
		return BPMStopRequest.STOP_OPERATION;
	}

	/**
	 * Handles {@link BPMStopRequest} and stop a workflow based on the provided workflow instance.
	 *
	 * @param request
	 *            is the event wrapper that is used to provide additional details
	 * @return the list of stopped workflow instance
	 * @throws CamundaIntegrationException
	 *             on an error in underlying processing
	 */
	@Override
	protected Object executeBPMAction(BPMTransitionRequest request) throws BPMException {
		LOGGER.debug("Going to start workflow based on request '{}'", request);
		Instance process = request.getTargetReference().toInstance();
		if (isProcess(process) && isActivity(process)) {
			Map<String, Serializable> properties = collectActivityProperties(request);
			// add all requested properties the the process as well
			process.addAllProperties(properties);
			processService.cancelProcess(process);
		}
		return Arrays.asList(process);
	}
}
