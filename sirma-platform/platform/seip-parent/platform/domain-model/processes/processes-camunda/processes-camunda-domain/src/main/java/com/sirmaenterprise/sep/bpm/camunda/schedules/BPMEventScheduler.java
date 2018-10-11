package com.sirmaenterprise.sep.bpm.camunda.schedules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.save.SaveRequest;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.AfterOperationExecutedEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntry;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirmaenterprise.sep.bpm.bpmn.ProcessService;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.event.MultiEngineBusinessProcessEvent;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.schedule.BPMScheduleWrapperEvent;
import com.sirmaenterprise.sep.bpm.camunda.service.SecureProcessEngine;
import com.sirmaenterprise.sep.bpm.camunda.util.BPMInstanceUtil;

/**
 * Camunda event scheduler that handle the execution of scheduled events by the {@link SchedulerService}.
 *
 * @author hlungov
 */
@Singleton
@Named(BPMEventScheduler.BEAN_ID)
public class BPMEventScheduler extends SchedulerActionAdapter {

	public static final String BEAN_ID = "BPMEventScheduler";

	/**
	 * Event extensions - properties keys.
	 */
	public static final String TARGET_DEF_ID = "targetDefId";
	public static final String USER_ACTION = "userAction";
	public static final String SERVER_ACTION = "serverAction";
	public static final String RELATION_ID = "relationId";

	/**
	 * Camunda system properties.
	 */
	protected static final String WORKFLOW_ID = "camundaProcessId";
	protected static final String EVENT_NAME = "camundaEventName";
	protected static final String EXECUTION_ID = "camundaExecutionId";

	@Inject
	private InstanceTypeResolver instanceResolver;
	@Inject
	private ProcessService processService;
	@Inject
	private LinkService linkService;

	private static final List<Pair<String, Class<?>>> ARGUMENTS_VALIDATION = Arrays.asList(
			new Pair<>(WORKFLOW_ID, String.class));

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	protected List<Pair<String, Class<?>>> validateInput() {
		return ARGUMENTS_VALIDATION;
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public void execute(SchedulerContext context) throws Exception {
		String executionId = context.getIfSameType(EXECUTION_ID, String.class);
		String eventName = context.getIfSameType(EVENT_NAME, String.class);
		String relationId = context.getIfSameType(RELATION_ID, String.class);
		String targetDefId = context.getIfSameType(TARGET_DEF_ID, String.class);
		String workflowId = context.getIfSameType(WORKFLOW_ID, String.class);
		SchedulerEntry schedulerEntry = context.getIfSameType(SchedulerContext.SCHEDULER_ENTRY, SchedulerEntry.class);
		// notify camunda signal event if workflow id and relation id exists
		if (StringUtils.isNotBlank(workflowId) && StringUtils.isNotBlank(relationId)) {
			InstanceReference instanceReference = BPMInstanceUtil.resolveReference(workflowId, instanceResolver);
			List<LinkReference> links = linkService.getLinks(instanceReference, relationId);
			for (LinkReference linkReference : links) {
				Instance instance = linkReference.getTo().toInstance();
				// notify only if related instance def id is the same as def id passed from camunda extension
				if (targetDefId.equalsIgnoreCase(instance.getIdentifier())) {
					notifyCamundaCatchEvent(eventName, executionId, schedulerEntry);
					break;
				}
			}
		}
	}

	private void notifyCamundaCatchEvent(String eventName, String executionId, SchedulerEntry schedulerEntry) {
		processService.notify(eventName, executionId, null);
		schedulerEntry.getConfiguration().setRemoveOnSuccess(true);
	}

	/**
	 * Creates the executor context.
	 *
	 * @param event
	 * 		the {@link MultiEngineBusinessProcessEvent} from which to take camunda extension properties and add them to new
	 * 		{@link SchedulerContext}
	 * @return the {@link SchedulerContext} filled with all the mandatory data for schedule
	 */
	public static SchedulerContext createExecutorContext(MultiEngineBusinessProcessEvent event) {
		DelegateExecution eventExecution = event.getExecution();
		EventDefinition camundaEventDefinition = CamundaModelElementInstanceUtil.getCamundaEventDefinition(
				eventExecution);
		String eventName = CamundaModelElementInstanceUtil.getEventName(camundaEventDefinition);
		SchedulerContext context = new SchedulerContext();
		if (StringUtils.isNotBlank(eventName)) {
			ExtensionElements extensionElements = CamundaModelElementInstanceUtil.getExtensionElements(eventExecution);
			CamundaProperties camundaProperties = CamundaModelElementInstanceUtil.getCamundaProperties(
					extensionElements);
			if (camundaProperties == null || camundaProperties.getCamundaProperties() == null) {
				return context;
			}
			for (CamundaProperty camundaProperty : camundaProperties.getCamundaProperties()) {
				CollectionUtils.addNonNullValue(context, camundaProperty.getCamundaName(),
												camundaProperty.getCamundaValue());
			}
			context.put(WORKFLOW_ID, event.getProcessBusinessKey());
			context.put(EXECUTION_ID, eventExecution.getId());
			context.put(EVENT_NAME, eventName);
			return context;
		}
		return context;
	}

	/**
	 * Creates the {@link EmfEvent} that will be scheduled by {@link SchedulerService}.
	 *
	 * @param context
	 * 		the {@link SchedulerContext} with {@link BPMEventScheduler#USER_ACTION} and {@link
	 * 		BPMEventScheduler#SERVER_ACTION}
	 * @param contextInstance
	 * 		the context instance to observe for
	 * @return returns the created {@link EmfEvent} that can be scheduled by {@link SchedulerService}
	 */
	public static EmfEvent createConfigurationEvent(SchedulerContext context, Instance contextInstance) {
		String userAction = context.getIfSameType(USER_ACTION, String.class);
		String serverAction = context.getIfSameType(SERVER_ACTION, String.class);
		if (SaveRequest.OPERATION_NAME.equalsIgnoreCase(serverAction)
				&& ActionTypeConstants.CREATE.equalsIgnoreCase(userAction)) {
			return new BPMScheduleWrapperEvent(new Operation(serverAction, userAction, true), contextInstance);
		}

		return new AfterOperationExecutedEvent(new Operation(serverAction, userAction, true), contextInstance);
	}

	/**
	 * Lookup for context instance by {@link LinkService} which to be used in creation of {@link SchedulerContext}.
	 *
	 * @param context
	 * 		the current {@link SchedulerContext} with properties filled from {@link MultiEngineBusinessProcessEvent}
	 * @param event
	 * 		the {@link MultiEngineBusinessProcessEvent}
	 * @return the found instances or throws {@link CamundaIntegrationRuntimeException} if none found
	 */
	public Collection<Instance> findContextInstances(SchedulerContext context, MultiEngineBusinessProcessEvent event) {
		String relationId = context.getIfSameType(RELATION_ID, String.class);
		String targetDefId = context.getIfSameType(TARGET_DEF_ID, String.class);
		String userAction = context.getIfSameType(USER_ACTION, String.class);
		String serverAction = context.getIfSameType(SERVER_ACTION, String.class);
		String processBusinessKey = event.getProcessBusinessKey();
		if (StringUtils.isNotBlank(targetDefId) && StringUtils.isNotBlank(relationId)) {
			InstanceReference workflowReference = BPMInstanceUtil.resolveReference(processBusinessKey,
																				   instanceResolver);
			if (SaveRequest.OPERATION_NAME.equalsIgnoreCase(serverAction)
					&& ActionTypeConstants.CREATE.equalsIgnoreCase(userAction)) {
				return Arrays.asList(workflowReference.toInstance());
			}
			List<LinkReference> linksFrom = linkService.getLinks(workflowReference, relationId);
			return linksFrom.stream()
					.map(LinkReference::getTo)
					.map(InstanceReference::toInstance)
					.filter(instance -> targetDefId.equalsIgnoreCase(instance.getIdentifier()))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}
