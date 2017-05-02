package com.sirma.itt.objects.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.event.BeforeInstancePersistEvent;

/**
 * Event fired before first persist of a {@link ObjectInstance}
 *
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before first persist of a {@link ObjectInstance}")
public class BeforeObjectPersistEvent extends BeforeInstancePersistEvent<ObjectInstance, AfterObjectPersistEvent> {

	/**
	 * Instantiates a new before object persist event.
	 *
	 * @param instance
	 *            the instance
	 */
	public BeforeObjectPersistEvent(ObjectInstance instance) {
		super(instance);
	}

	@Override
	protected AfterObjectPersistEvent createNextEvent() {
		return new AfterObjectPersistEvent(getInstance());
	}

}
