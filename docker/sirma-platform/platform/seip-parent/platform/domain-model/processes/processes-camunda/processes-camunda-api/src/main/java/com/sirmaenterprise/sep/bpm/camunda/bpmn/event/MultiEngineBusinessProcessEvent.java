package com.sirmaenterprise.sep.bpm.camunda.bpmn.event;

import org.camunda.bpm.engine.cdi.BusinessProcessEventType;
import org.camunda.bpm.engine.cdi.impl.event.CdiBusinessProcessEvent;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.bpmn.behavior.GatewayActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ParallelMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SequentialMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirmaenterprise.sep.bpm.camunda.util.ProcessEngineUtil;

/**
 * The {@link MultiEngineBusinessProcessEvent} is a wrapper for event occurred at arbitrary process engine. It contains
 * additional business logic based on the received event.
 *
 * @author bbanchev
 */
public class MultiEngineBusinessProcessEvent extends CdiBusinessProcessEvent {

	/**
	 * Enum for currently supported activity types.
	 *
	 * @author bbanchev
	 */
	public enum ActivityType {
		/** Any BPMN 2.0 task. */
		TASK,
		/** Any BPMN 2.0 gateway. */
		GATEWAY,
		/** Any BPMN 2.0 subprocess. */
		SUBPROCESS,
		/** Any BPMN 2.0 event (catching or throwing). */
		EVENT;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(MultiEngineBusinessProcessEvent.class);
	protected String engineName;
	protected ActivityType activityType;
	protected String processBusinessKey;
	protected DelegateExecution execution;

	/**
	 * Instantiates a new multi engine business process event for {@link DelegateExecution}.
	 *
	 * @param execution
	 * 		the execution event
	 * @param processDefinition
	 * 		the process definition of the event
	 * @param type
	 * 		the business type
	 */
	public MultiEngineBusinessProcessEvent(DelegateExecution execution, ProcessDefinitionEntity processDefinition,
			BusinessProcessEventType type) {
		super(execution.getCurrentActivityId(), execution.getCurrentTransitionId(), processDefinition, execution, type,
			  ClockUtil.getCurrentTime());
		this.execution = execution;
		setActivityType();

		this.engineName = ProcessEngineUtil.getProcessEngineName(execution);
		processBusinessKey = execution.getProcessBusinessKey();
	}

	/**
	 * Instantiates a new multi engine business process event for {@link DelegateTask}.
	 *
	 * @param delegateTask
	 * 		the task event
	 * @param processDefinition
	 * 		the process definition of the event
	 * @param type
	 * 		the business type
	 */
	public MultiEngineBusinessProcessEvent(DelegateTask delegateTask, ProcessDefinitionEntity processDefinition,
			BusinessProcessEventType type) {
		super(delegateTask, processDefinition, type, ClockUtil.getCurrentTime());
		execution = delegateTask.getExecution();
		activityType = ActivityType.TASK;
		this.engineName = ProcessEngineUtil.getProcessEngineName(execution);
		processBusinessKey = execution.getProcessBusinessKey();
	}

	private void setActivityType() {

		ActivityBehavior activityBehavior = getActivityBehavior();
		if (activityBehavior == null) {
			LOGGER.warn("DelegateExecution is unsupported type {}", execution);
			return;
		}
		if (activityBehavior instanceof TaskActivityBehavior) {
			activityType = ActivityType.TASK;
		} else if (activityBehavior instanceof ParallelMultiInstanceActivityBehavior
				|| activityBehavior instanceof SequentialMultiInstanceActivityBehavior) {
			activityType = ActivityType.TASK;
		} else if (activityBehavior instanceof NoneStartEventActivityBehavior
				|| activityBehavior instanceof NoneEndEventActivityBehavior
				|| activityBehavior instanceof IntermediateCatchEventActivityBehavior) {
			activityType = ActivityType.EVENT;
		} else if (activityBehavior instanceof SubProcessActivityBehavior) {
			activityType = ActivityType.SUBPROCESS;
		} else if (activityBehavior instanceof GatewayActivityBehavior) {
			activityType = ActivityType.GATEWAY;
		} else {
			LOGGER.warn("Unimplemented ActivityType mapping for : {}", activityBehavior);
		}
	}

	/**
	 * Gets the activity behavior for the current execution. Might return null if the {@link #getExecution()} is not a
	 * {@link ActivityExecution}
	 *
	 * @return the activity behavior
	 */
	public ActivityBehavior getActivityBehavior() {
		PvmActivity activity = getActivity();
		if (activity != null) {
			return activity.getActivityBehavior();
		}
		return null;
	}

	/**
	 * Gets the source event activity.
	 *
	 * @return the underlined activity or null if not found
	 */
	public PvmActivity getActivity() {
		if (execution instanceof ActivityExecution) {
			return ((ActivityExecution) execution).getActivity();
		}
		return null;
	}

	/**
	 * Gets the current most inner scope - might be event the execution
	 *
	 * @return the variable scope. Might return null on invalid scope
	 */
	public AbstractVariableScope getLocalVariableScope() {
		DelegateTask task = getTask();
		if (task instanceof AbstractVariableScope) {
			return (AbstractVariableScope) task;
		}
		if (execution instanceof AbstractVariableScope) {
			return (AbstractVariableScope) execution;
		}
		return null;
	}

	/**
	 * Gets the engine name.
	 *
	 * @return the engine name
	 */
	public String getEngineName() {
		return engineName;
	}

	/**
	 * Gets the activity type.
	 *
	 * @return the activity type
	 */
	public ActivityType getActivityType() {
		return activityType;
	}

	/**
	 * Gets the process business key.
	 *
	 * @return the process business key
	 */
	public String getProcessBusinessKey() {
		return processBusinessKey;
	}

	/**
	 * Gets the execution associated with the event.
	 *
	 * @return the execution
	 */
	public DelegateExecution getExecution() {
		return execution;
	}

	@Override
	public String toString() {
		return "Event '" + processDefinition.getKey() + "' ['" + type + "', '" + activityType + "', " + (
				type == BusinessProcessEventType.TAKE ? transitionName : activityId) + "]";
	}
}
