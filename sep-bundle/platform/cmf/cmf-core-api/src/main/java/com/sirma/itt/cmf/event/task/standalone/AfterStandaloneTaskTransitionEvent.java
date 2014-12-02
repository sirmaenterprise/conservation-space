package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before task transition of type {@link StandaloneTaskInstance} in Activiti.
 * 
 * @author BBonev
 */
@Documentation("Event fired before task transition of type {@link StandaloneTaskInstance} in Activiti.")
public class AfterStandaloneTaskTransitionEvent extends
		AbstractInstanceTwoPhaseEvent<StandaloneTaskInstance, TwoPhaseEvent> {

	/** The transition definition. */
	private final TransitionDefinition transitionDefinition;

	/**
	 * Instantiates a new after standalone task transition event.
	 * 
	 * @param instance
	 *            the instance
	 * @param transitionDefinition
	 *            the transition definition
	 */
	public AfterStandaloneTaskTransitionEvent(StandaloneTaskInstance instance,
			TransitionDefinition transitionDefinition) {
		super(instance);
		this.transitionDefinition = transitionDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}

	/**
	 * Getter method for transitionDefinition.
	 * 
	 * @return the transitionDefinition
	 */
	public TransitionDefinition getTransitionDefinition() {
		return transitionDefinition;
	}

}
