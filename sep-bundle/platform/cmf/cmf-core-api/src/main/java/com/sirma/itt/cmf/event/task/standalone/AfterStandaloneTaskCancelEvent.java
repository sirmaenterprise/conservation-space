package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceCancelEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before cancellation of a {@link StandaloneTaskInstance} in Activiti.
 * 
 * @author BBonev
 */
@Documentation("Event fired before cancellation of a {@link StandaloneTaskInstance} in Activiti.")
public class AfterStandaloneTaskCancelEvent extends
		AfterInstanceCancelEvent<StandaloneTaskInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new standalone task cancel event.
	 *
	 * @param instance
	 *            the instance
	 */
	public AfterStandaloneTaskCancelEvent(StandaloneTaskInstance instance) {
		super(instance);
	}

}
