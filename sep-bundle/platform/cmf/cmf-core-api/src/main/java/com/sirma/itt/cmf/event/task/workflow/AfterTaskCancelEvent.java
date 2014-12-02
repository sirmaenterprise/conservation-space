package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceCancelEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after {@link TaskInstance} is canceled during the workflow cancellation.
 * 
 * @author BBonev
 */
@Documentation("Event fired after {@link TaskInstance} is canceled during the workflow cancellation.")
public class AfterTaskCancelEvent extends AfterInstanceCancelEvent<TaskInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after task cancel event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterTaskCancelEvent(TaskInstance instance) {
		super(instance);
	}

}
