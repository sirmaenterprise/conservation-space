package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before deleting a {@link StandaloneTaskInstance} in Activiti.
 *
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before deleting a {@link StandaloneTaskInstance} in Activiti.")
public class BeforeStandaloneTaskDeleteEvent extends
		BeforeInstanceDeleteEvent<StandaloneTaskInstance, AfterStandaloneTaskDeleteEvent> {

	/**
	 * Instantiates a new standalone task cancel event.
	 *
	 * @param instance
	 *            the instance
	 */
	public BeforeStandaloneTaskDeleteEvent(StandaloneTaskInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterStandaloneTaskDeleteEvent createNextEvent() {
		return new AfterStandaloneTaskDeleteEvent(getInstance());
	}

}
