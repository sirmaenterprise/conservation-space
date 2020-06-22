package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import static com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService.isProcess;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil.getStartModelElementInstance;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.getActivityId;
import static com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowParser.getSequenceFlowModel;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModel;
import com.sirmaenterprise.sep.bpm.exception.BPMException;

/**
 * Action implementation that can execute the {@link BPMStartRequest#START_OPERATION} action.<br>
 * The provided {@link BPMStartRequest} should contain a workflow instance that should be used as source value
 * 
 * @author bbanchev
 */
@Extension(target = Action.TARGET_NAME, order = 601)
public class BPMStartAction extends BPMTransitionAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public String getName() {
		return BPMStartRequest.START_OPERATION;
	}

	/**
	 * Handles {@link BPMStartRequest} and start a new workflow based on the provided workflow instance.
	 * 
	 * @param request
	 *            is the event wrapper that is used to provide additional details
	 * @return the list of newly generated instances part of the started process
	 * @throws CamundaIntegrationException
	 *             on an error in underlying processing
	 */
	@Override
	protected Object executeBPMAction(BPMTransitionRequest request) throws BPMException {
		LOGGER.debug("Going to start workflow based on request '{}'", request);
		Instance process = request.getTargetReference().toInstance();
		if (isProcess(process)) {
			String operation = request.getUserOperation();
			// get the model instance to find the start transition element + transitions and their checkpoints
			BpmnModelInstance processModel = camundaBPMNService.getBpmnModelInstance(process);
			StartEvent startEvent = getStartModelElementInstance(processModel);
			if (startEvent == null) {
				LOGGER.warn("There is no valid start element for process: {}", process);
			}
			SequenceFlowModel sequenceFlowModel = getSequenceFlowModel(startEvent);
			// process the request and split the properties for
			Map<String, Serializable> properties = collectActivityProperties(request);
			// add all requested properties the the process as well
			process.addAllProperties(properties);
			// collect the transition data
			Map<String, Serializable> collectTransitionProperties = new HashMap<>();
			sequenceFlowModel.getTransitions().forEach(
					transition -> collectTransitionProperties.putAll(collectTransitionProperties(request, transition)));

			Instance updated = processService.startProcess(process, collectTransitionProperties);
			if (updated != null) {
				// if started execute completion steps
				completeProcessCreation(operation, updated);
				return listGeneratedActivities(request, getActivityId(process));
			}
		}
		return Collections.emptyList();
	}

	private void completeProcessCreation(String operation, Instance process) {
		// transfer the initial workflow properties back to workflow
		loadAndTransferProcessProperties(camundaBPMNService.getProcessInstance(process), process);
		domainInstanceService.save(InstanceSaveContext.create(process, new Operation(operation)));
	}

}
