package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before transition in a {@link StandaloneTaskInstance} in Activiti.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before transition in a {@link StandaloneTaskInstance} in Activiti.")
public class BeforeStandaloneTaskTransitionEvent extends
		AbstractInstanceTwoPhaseEvent<StandaloneTaskInstance, AfterStandaloneTaskTransitionEvent> {

	/** The transition definition. */
	private final TransitionDefinition transitionDefinition;

	/**
	 * Instantiates a new standalone task cancel event.
	 * 
	 * @param instance
	 *            the instance
	 * @param transitionDefinition
	 *            the transition definition
	 */
	public BeforeStandaloneTaskTransitionEvent(StandaloneTaskInstance instance,
			TransitionDefinition transitionDefinition) {
		super(instance);
		this.transitionDefinition = transitionDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterStandaloneTaskTransitionEvent createNextEvent() {
		return new AfterStandaloneTaskTransitionEvent(getInstance(), getTransitionDefinition());
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
