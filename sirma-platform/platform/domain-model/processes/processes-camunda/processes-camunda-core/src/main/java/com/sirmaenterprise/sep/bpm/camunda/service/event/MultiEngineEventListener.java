package com.sirmaenterprise.sep.bpm.camunda.service.event;

import static com.sirmaenterprise.sep.bpm.camunda.util.ProcessEngineUtil.getProcessEngineName;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.cdi.BusinessProcessEvent;
import org.camunda.bpm.engine.cdi.BusinessProcessEventType;
import org.camunda.bpm.engine.cdi.impl.event.CdiEventListener;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;

import com.sirmaenterprise.sep.bpm.camunda.bpmn.event.MultiEngineBusinessProcessEvent;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMSecurityService;

/**
 * Event generator of CDI events based on {@link DelegateExecution} and {@link DelegateTask} events triggered by the
 * process engine. Generator fires a {@link MultiEngineBusinessProcessEvent} in the required security context and with
 * all available data filled in the event.
 * 
 * @author bbanchev
 */
@Singleton
public class MultiEngineEventListener extends CdiEventListener {
	private static final long serialVersionUID = -3168365694809648019L;

	@Inject
	private BPMSecurityService bpmSecurityService;

	@Override
	public void notify(DelegateExecution execution) throws Exception {
		bpmSecurityService.executeEngineOperation(getProcessEngineName(execution), () -> {
			super.notify(execution);
			return null;
		});
	}

	@Override
	public void notify(DelegateTask task) {
		try {
			bpmSecurityService.executeEngineOperation(getProcessEngineName(task), () -> {
				super.notify(task);
				return null;
			});
		} catch (Exception e) {
			throw new CamundaIntegrationRuntimeException(e);
		}
	}

	@Override
	protected BusinessProcessEvent createEvent(DelegateExecution execution) {
		ProcessDefinitionEntity processDefinition = Context
				.getBpmnExecutionContext()
					.getExecution()
					.getProcessDefinition();
		return new MultiEngineBusinessProcessEvent(execution, processDefinition,
				provideExecutionType(execution.getEventName()));
	}

	private static BusinessProcessEventType provideExecutionType(String eventName) {
		BusinessProcessEventType type;
		if (ExecutionListener.EVENTNAME_START.equals(eventName)) {
			type = BusinessProcessEventType.START_ACTIVITY;
		} else if (ExecutionListener.EVENTNAME_END.equals(eventName)) {
			type = BusinessProcessEventType.END_ACTIVITY;
		} else if (ExecutionListener.EVENTNAME_TAKE.equals(eventName)) {
			type = BusinessProcessEventType.TAKE;
		} else {
			throw new CamundaIntegrationRuntimeException("Unsupported event type: " + eventName);
		}
		return type;
	}

	@Override
	protected BusinessProcessEvent createEvent(DelegateTask task) {
		ProcessDefinitionEntity processDefinition = Context
				.getBpmnExecutionContext()
					.getExecution()
					.getProcessDefinition();

		return new MultiEngineBusinessProcessEvent(task, processDefinition, provideTaskType(task.getEventName()));
	}

	private static BusinessProcessEventType provideTaskType(String eventName) {
		BusinessProcessEventType type;
		if (TaskListener.EVENTNAME_CREATE.equals(eventName)) {
			type = BusinessProcessEventType.CREATE_TASK;
		} else if (TaskListener.EVENTNAME_COMPLETE.equals(eventName)) {
			type = BusinessProcessEventType.COMPLETE_TASK;
		} else if (TaskListener.EVENTNAME_ASSIGNMENT.equals(eventName)) {
			type = BusinessProcessEventType.ASSIGN_TASK;
		} else if (TaskListener.EVENTNAME_DELETE.equals(eventName)) {
			type = BusinessProcessEventType.DELETE_TASK;
		} else {
			throw new CamundaIntegrationRuntimeException("Unsupported event type: " + eventName);
		}
		return type;
	}

}
