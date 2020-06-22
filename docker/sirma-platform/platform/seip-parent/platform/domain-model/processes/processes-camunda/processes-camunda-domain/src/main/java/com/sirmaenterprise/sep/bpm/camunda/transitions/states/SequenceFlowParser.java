package com.sirmaenterprise.sep.bpm.camunda.transitions.states;

import static com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil.isSkipped;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

import org.camunda.bpm.engine.delegate.BpmnModelExecutionContext;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.EventBasedGateway;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.InclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SequenceFlowParser} is helper method that builds a {@link SequenceFlowModel} based on the current
 * execution.
 *
 * @author bbanchev
 */
public class SequenceFlowParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private SequenceFlowParser() {
		// utility class
	}

	/**
	 * Gets the outgoing transitions as a wrapper class {@link SequenceFlowModel} .
	 *
	 * @param executionModel
	 *            the execution to get transitions for
	 * @return the outgoing transitions - possibly empty, not null
	 */
	public static SequenceFlowModel getSequenceFlowModel(ModelElementInstance executionModel) {
		SequenceFlowModel model = new SequenceFlowModel();
		if (executionModel instanceof FlowNode) {
			FlowNode bpmnModelElementInstance = (FlowNode) executionModel;
			// characteristics should be set at the beginning
			if (isMultiInstance(bpmnModelElementInstance)) {
				model.loopCharacteristics = true;
			}
			Collection<SequenceFlow> outgoing = bpmnModelElementInstance.getOutgoing();
			for (SequenceFlow sequenceFlow : outgoing) {
				processEntry(model, sequenceFlow, sequenceFlow);
			}

		}
		return model;
	}

	/**
	 * Builds {@link SequenceFlowModel} based on the the current execution status. {@link SequenceFlowModel} would be
	 * filled with all possible transitions and all activities for each of those transitions.
	 * 
	 * @param execution
	 *            as source data
	 * @return the built {@link SequenceFlowModel} - might have no transitions, never null
	 */
	public static SequenceFlowModel getSequenceFlowModel(BpmnModelExecutionContext execution) {
		return getSequenceFlowModel(execution.getBpmnModelElementInstance());
	}

	private static void extractParallel(SequenceFlowModel model, SequenceFlow source, SequenceFlow current) {
		FlowNode target = current.getTarget();
		Collection<SequenceFlow> outgoing = target.getOutgoing();
		if (outgoing.isEmpty()) {
			return;
		}
		for (SequenceFlow flow : outgoing) {
			if (!isNonBlocking(flow)) {
				// add to the chain
				addToModel(model, source, flow.getTarget());
			} else {
				extractParallel(model, source, flow);
			}
		}
	}

	private static void processEntry(SequenceFlowModel model, SequenceFlow transition, SequenceFlow appendTo) {
		FlowNode target = transition.getTarget();
		LOGGER.debug("Flow with id {}, target: {} and condition: {}", transition.getId(), target,
				transition.getConditionExpression() != null ? transition.getConditionExpression().getRawTextContent()
						: null);
		if (isParallel(target)) {
			extractParallel(model, transition, transition);
		} else if (isNonBlocking(transition)) {
			extractSequences(model, transition, appendTo);
		} else {
			addToModel(model, appendTo, target);
		}
	}

	private static void addToModel(SequenceFlowModel model, SequenceFlow transition, FlowNode activity) {
		LOGGER.trace("SequenceFlowModel.add {} to {} ", activity.getId(), transition.getId());
		if (activity instanceof EndEvent) {
			model.add(transition, null);
		} else {
			model.add(transition, activity.getId());
		}
	}

	private static boolean isMultiInstance(FlowNode activity) {
		if (!(activity instanceof Activity)) {
			return false;
		}
		return ((Activity) activity).getLoopCharacteristics() instanceof MultiInstanceLoopCharacteristics;
	}

	private static void extractSequences(SequenceFlowModel model, SequenceFlow transition, SequenceFlow appendTo) {
		FlowNode target = transition.getTarget();
		for (SequenceFlow outgoing : target.getOutgoing()) {
			SequenceFlow nextAppendTo = isSkipped(outgoing.getId()) ? appendTo : outgoing;
			processEntry(model, outgoing, nextAppendTo);
		}
	}

	private static boolean isNonBlocking(SequenceFlow sequenceFlow) {
		FlowNode target = sequenceFlow.getTarget();
		return checkNonBlockingGateway(target) || checkNonBlockingEvent(target) || checkNonBlockingTask(target);
	}

	private static boolean isParallel(FlowNode target) {
		return target instanceof ParallelGateway || target instanceof InclusiveGateway;
	}

	private static boolean checkNonBlockingTask(FlowNode target) {
		// may be include multitinstance tasks
		// also check service/mail and so on tasks
		return target == null;
	}

	private static boolean checkNonBlockingGateway(FlowNode target) {
		// although EventBasedGateway is not an immediate operation it is not blocked by required user interaction
		return target instanceof ExclusiveGateway || target instanceof EventBasedGateway
				|| target instanceof ParallelGateway || target instanceof InclusiveGateway;
	}

	private static boolean checkNonBlockingEvent(FlowNode target) {
		return target instanceof Event && !(target instanceof EndEvent);
	}

}
