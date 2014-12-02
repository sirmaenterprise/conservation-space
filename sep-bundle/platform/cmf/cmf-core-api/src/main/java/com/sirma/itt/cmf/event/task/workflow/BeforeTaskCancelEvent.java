package com.sirma.itt.cmf.event.task.workflow;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.event.HandledEvent;
import com.sirma.itt.emf.event.instance.BeforeInstanceCancelEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before {@link TaskInstance} is canceled. The task cannot be canceled directly but
 * only via the workflow that is in.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before {@link TaskInstance} is canceled. The task cannot be canceled directly but only via the workflow that is in.")
public class BeforeTaskCancelEvent extends
		BeforeInstanceCancelEvent<TaskInstance, AfterTaskCancelEvent> implements HandledEvent {

	/** The handled. */
	private boolean handled;

	/**
	 * Instantiates a new before standalone task persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeTaskCancelEvent(TaskInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterTaskCancelEvent createNextEvent() {
		return new AfterTaskCancelEvent(getInstance());
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

}
