package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after {@link TaskInstance} is created/persisted for the first time.
 * 
 * @author BBonev
 */
@Documentation("Event fired after {@link TaskInstance} is beening created/perssited for the first time.")
public class AfterTaskPersistEvent extends AfterInstancePersistEvent<TaskInstance, TwoPhaseEvent> {

	private final TaskInstance previousTask;

	/**
	 * Instantiates a new before standalone task persist event.
	 * 
	 * @param instance
	 *            the instance
	 * @param previousTask
	 *            the previous task
	 */
	public AfterTaskPersistEvent(TaskInstance instance, TaskInstance previousTask) {
		super(instance);
		this.previousTask = previousTask;
	}

	/**
	 * Gets the previous task.
	 * 
	 * @return the previous task
	 */
	public TaskInstance getPreviousTask() {
		return previousTask;
	}

}
