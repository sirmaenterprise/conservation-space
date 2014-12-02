package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.HandledEvent;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before {@link TaskInstance} is created/persisted for the first time.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before {@link TaskInstance} is beening created/perssited for the first time.")
public class BeforeTaskPersistEvent extends
		BeforeInstancePersistEvent<TaskInstance, AfterTaskPersistEvent> implements HandledEvent {

	private final TaskInstance previousTask;
	private boolean handled;

	/**
	 * Instantiates a new before standalone task persist event.
	 * 
	 * @param instance
	 *            the instance
	 * @param previousTask
	 *            the previous task
	 */
	public BeforeTaskPersistEvent(TaskInstance instance, TaskInstance previousTask) {
		super(instance);
		this.previousTask = previousTask;
	}

	@Override
	protected AfterTaskPersistEvent createNextEvent() {
		return new AfterTaskPersistEvent(getInstance(), getPreviousTask());
	}

	/**
	 * Getter method for previousTask.
	 * 
	 * @return the previousTask
	 */
	public TaskInstance getPreviousTask() {
		return previousTask;
	}

	@Override
	public boolean isHandled() {
		return handled;
	}

	@Override
	public void setHandled(boolean handled) {
		this.handled = handled;
	}

}
