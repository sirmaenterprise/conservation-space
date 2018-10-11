package com.sirmaenterprise.sep.bpm.camunda.bpmn.parse;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;

import org.camunda.bpm.engine.delegate.BpmnModelExecutionContext;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.CatchEvent;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.LoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.ThrowEvent;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to facilitate work with raw {@link ModelElementInstance}
 *
 * @author bbanchev
 */
public class CamundaModelElementInstanceUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private CamundaModelElementInstanceUtil() {
		// utility class
	}

	/**
	 * Gets the {@link CamundaFormData} for given {@link ExtensionElements}
	 *
	 * @param extensionElements
	 *            the extension elements as source data
	 * @return the {@link CamundaFormData} or null if not found or extensionElements is null
	 */
	public static CamundaFormData getCamundaFormData(ExtensionElements extensionElements) {
		if (extensionElements == null) {
			return null;
		}
		Collection<ModelElementInstance> elements = extensionElements.getElements();
		for (ModelElementInstance modelElementInstance : elements) {
			if (modelElementInstance instanceof CamundaFormData) {
				return (CamundaFormData) modelElementInstance;
			}
		}
		return null;
	}

	/**
	 * Gets the {@link ExtensionElements} for given {@link DelegateExecution}
	 *
	 * @param context
	 *            the execution as source data
	 * @return the {@link ExtensionElements} or null if not found or extensionElements is null
	 */
	public static ExtensionElements getExtensionElements(Object context) {

		BaseElement extendedItem = null;
		if (context instanceof BpmnModelExecutionContext) {
			extendedItem = ((BpmnModelExecutionContext) context).getBpmnModelElementInstance();
			// check for loop execution elements that are not fully supported by #getBpmnModelElementInstance
			if (extendedItem == null && context instanceof ActivityExecution) {
				ActivityExecution activityExecution = (ActivityExecution) context;
				String executionId = getModelElementId(activityExecution.getCurrentActivityId());
				// supports null as param
				extendedItem = activityExecution.getBpmnModelInstance().getModelElementById(executionId);
			}
		} else if (context instanceof FlowElement) {
			extendedItem = (FlowElement) context;
		}

		if (extendedItem == null) {
			return null;
		}
		return extendedItem.getExtensionElements();
	}

	/**
	 * Gets the {@link CamundaProperties} for given {@link ExtensionElements}
	 *
	 * @param extensionElements
	 *            the extension elements as source data
	 * @return the {@link CamundaProperties} or null if not found or extensionElements is null
	 */
	public static CamundaProperties getCamundaProperties(ExtensionElements extensionElements) {
		if (extensionElements == null || extensionElements.getElements() == null) {
			return null;
		}
		for (ModelElementInstance element : extensionElements.getElements()) {
			if (element instanceof CamundaProperties) {
				return (CamundaProperties) element;
			}
		}
		return null;
	}

	/**
	 * Get event name from {@link EventDefinition}.
	 *
	 * @param eventDefinition the camunda event definition element - {@link EventDefinition}
	 * @return the event name
	 */
	public static String getEventName(EventDefinition eventDefinition) {
		if (eventDefinition instanceof SignalEventDefinition) {
			Signal signal = ((SignalEventDefinition) eventDefinition).getSignal();
			return signal.getName();
		} else if (eventDefinition instanceof MessageEventDefinition) {
			Message message = ((MessageEventDefinition) eventDefinition).getMessage();
			return message.getName();
		} else if (eventDefinition instanceof TimerEventDefinition) {
			LOGGER.warn("Not implemented yet parsing for: " + TimerEventDefinition.class.getName());
		}
		return null;
	}

	/**
	 * Get Camunda event definition from current execution {@link DelegateExecution}.
	 * @param execution the current delegate execution
	 * @return the {@link EventDefinition} or null if not found
	 */
	public static EventDefinition getCamundaEventDefinition(DelegateExecution execution) {
		FlowElement element = execution.getBpmnModelElementInstance();
		Collection<EventDefinition> eventDefinitions = Collections.emptyList();
		if (element instanceof CatchEvent) {
			eventDefinitions = ((CatchEvent) element).getEventDefinitions();
		} else if (element instanceof ThrowEvent) {
			eventDefinitions = ((ThrowEvent) element).getEventDefinitions();
		}
		if (eventDefinitions.size() == 1) {
			return eventDefinitions.iterator().next();
		}
		return null;
	}

	/**
	 * Gets the {@link CamundaInputOutput} for given {@link ExtensionElements}
	 *
	 * @param extensionElements
	 *            the extension elements as source data
	 * @return the {@link CamundaInputOutput} or null if not found or extensionElements is null
	 */
	public static CamundaInputOutput getCamundaInputOutput(ExtensionElements extensionElements) {
		if (extensionElements == null || extensionElements.getElements() == null) {
			return null;
		}
		for (ModelElementInstance element : extensionElements.getElements()) {
			if (element instanceof CamundaInputOutput) {
				return (CamundaInputOutput) element;
			}
		}
		return null;
	}

	/**
	 * Gets the {@link LoopCharacteristics} for given {@link ExtensionElements}
	 *
	 * @param extensionElements
	 *            the extension elements as source data
	 * @return the {@link LoopCharacteristics} or null if not found or extensionElements is null
	 */
	public static LoopCharacteristics getLoopCharacteristics(ExtensionElements extensionElements) {
		if (extensionElements == null || extensionElements.getElements() == null) {
			return null;
		}
		for (ModelElementInstance element : extensionElements.getElements()) {
			if (element instanceof LoopCharacteristics) {
				return (LoopCharacteristics) element;
			}
		}
		return null;
	}

	/**
	 * Gets the {@link StartEvent} for given {@link BpmnModelInstance}
	 *
	 * @param processModel
	 *            the process model instance
	 * @return the {@link StartEvent} or null if not found or provided model is null
	 */
	public static StartEvent getStartModelElementInstance(BpmnModelInstance processModel) {
		if (processModel == null) {
			return null;
		}
		Collection<StartEvent> startElements = processModel.getModelElementsByType(StartEvent.class);
		if (startElements.isEmpty()) {
			return null;
		}
		return startElements.iterator().next();
	}

	/**
	 * Gets the actual id of activity. If the activity is simple the returned id is the same as the provided, if the
	 * activity is part of loop execution is returned the model id as it could be found the bpmn model.
	 * 
	 * @param activityId
	 *            is the id - i.e. TASK000 or TASK000#multiInstanceBody
	 * @return the task id as it is the bpmn model - i.e. TASK000
	 */
	public static String getModelElementId(String activityId) {
		if (activityId == null) {
			return null;
		}
		// if part of multiinstance execution extract the actual id
		if (activityId.indexOf('#') > 0) {// NOSONAR
			return activityId.substring(0, activityId.indexOf('#'));
		}
		return activityId;
	}
}
