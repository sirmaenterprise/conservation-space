package com.sirma.itt.emf.event.instance;

import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when an instance is going to be deleted, but before the actual deletion. The event
 * carry the instance that going to be deleted.
 * 
 * @param <I>
 *            the Instance type
 * @param <E>
 *            the next phase event type
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired when an instance is going to be deleted, but before the actual deletion. The event carry the instance that going to be deleted.")
public class BeforeInstanceDeleteEvent<I extends Instance, E extends AfterInstanceDeleteEvent<I, TwoPhaseEvent>>
		extends AbstractInstanceTwoPhaseEvent<I, E> {

	/**
	 * Instantiates a new before instance delete event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeInstanceDeleteEvent(I instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected E createNextEvent() {
		return (E) new AfterInstanceDeleteEvent<I, TwoPhaseEvent>(getInstance());
	}
}
