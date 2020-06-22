package com.sirma.itt.objects.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;

/**
 * Event fired after first persist of a {@link ObjectInstance}
 *
 * @author BBonev
 */
@Documentation("Event fired after first persist of a {@link ObjectInstance}")
public class AfterObjectPersistEvent extends AfterInstancePersistEvent<ObjectInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new before object persist event.
	 *
	 * @param instance
	 *            the instance
	 */
	public AfterObjectPersistEvent(ObjectInstance instance) {
		super(instance);
	}

}
