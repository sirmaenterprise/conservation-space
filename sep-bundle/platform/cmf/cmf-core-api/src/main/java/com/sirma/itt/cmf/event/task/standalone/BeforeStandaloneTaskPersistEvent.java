package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.HandledEvent;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before {@link StandaloneTaskInstance} is created/persisted for the first time.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before {@link StandaloneTaskInstance} is beening created/perssited for the first time.")
public class BeforeStandaloneTaskPersistEvent extends
		BeforeInstancePersistEvent<StandaloneTaskInstance, AfterStandaloneTaskPersistEvent>
		implements HandledEvent {

	/** The handled. */
	private boolean handled;

	/**
	 * Instantiates a new before standalone task persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeStandaloneTaskPersistEvent(StandaloneTaskInstance instance) {
		super(instance);
	}

	@Override
	protected AfterStandaloneTaskPersistEvent createNextEvent() {
		return new AfterStandaloneTaskPersistEvent(getInstance());
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
