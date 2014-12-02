package com.sirma.itt.objects.event;

import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Event fired after first persist of a {@link ObjectInstance}
 * 
 * @author BBonev
 */
@Documentation("Event fired after first persist of a {@link ObjectInstance}")
public class AfterObjectPersistEvent extends
		AfterInstancePersistEvent<ObjectInstance, TwoPhaseEvent> {

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
