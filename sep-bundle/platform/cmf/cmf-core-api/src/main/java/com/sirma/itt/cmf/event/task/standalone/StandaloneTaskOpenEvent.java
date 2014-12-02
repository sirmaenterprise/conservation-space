package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.HandledEvent;
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when a task of instance {@link StandaloneTaskInstance} is going to be opened.
 * 
 * @author BBonev
 */
@Documentation("Event fired when a task of instance {@link StandaloneTaskInstance} is going to be opened.")
public class StandaloneTaskOpenEvent extends InstanceOpenEvent<StandaloneTaskInstance> implements
		HandledEvent {

	/** The handled. */
	private boolean handled;

	/**
	 * Instantiates a new standalone task open event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public StandaloneTaskOpenEvent(StandaloneTaskInstance instance) {
		super(instance);
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
