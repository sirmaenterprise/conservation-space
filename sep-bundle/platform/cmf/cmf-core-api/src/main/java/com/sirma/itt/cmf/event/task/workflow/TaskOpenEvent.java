package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.HandledEvent;
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired on task open by user.
 * 
 * @author BBonev
 */
@Documentation("Event fired on task open by user.")
public class TaskOpenEvent extends InstanceOpenEvent<TaskInstance> implements
		HandledEvent {

	/** The handled. */
	private boolean handled = false;

	/** The context. */
	private final WorkflowInstanceContext context;

	/**
	 * Instantiates a new task open event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public TaskOpenEvent(TaskInstance instance) {
		this(instance, instance.getContext());
	}

	/**
	 * Instantiates a new task open event.
	 * 
	 * @param instance
	 *            the instance
	 * @param context
	 *            the context
	 */
	public TaskOpenEvent(TaskInstance instance, WorkflowInstanceContext context) {
		super(instance);
		this.context = context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isHandled() {
		return handled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	/**
	 * Getter method for context.
	 * 
	 * @return the context
	 */
	public WorkflowInstanceContext getContext() {
		return context;
	}

}
