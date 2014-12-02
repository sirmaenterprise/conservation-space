package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.instance.BeforeInstanceCancelEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before cancellation of a {@link StandaloneTaskInstance} in Activiti.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before cancellation of a {@link StandaloneTaskInstance} in Activiti.")
public class BeforeStandaloneTaskCancelEvent extends
		BeforeInstanceCancelEvent<StandaloneTaskInstance, AfterStandaloneTaskCancelEvent> {

	/**
	 * Instantiates a new standalone task cancel event.
	 *
	 * @param instance
	 *            the instance
	 */
	public BeforeStandaloneTaskCancelEvent(StandaloneTaskInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterStandaloneTaskCancelEvent createNextEvent() {
		return new AfterStandaloneTaskCancelEvent(getInstance());
	}

}
