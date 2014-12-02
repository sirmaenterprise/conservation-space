package com.sirma.itt.emf.event.instance;

import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when an instance is going to be canceled, but before the actual cancellation. The
 * event carry the instance that going to be canceled.
 * 
 * @param <I>
 *            the Instance type
 * @param <E>
 *            the next phase event type
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired when an instance is going to be canceled, but before the actual cancellation. The event carry the instance that going to be canceled.")
public class BeforeInstanceCancelEvent<I extends Instance, E extends AfterInstanceCancelEvent<I, TwoPhaseEvent>>
		extends AbstractInstanceTwoPhaseEvent<I, E> {

	/**
	 * Instantiates a new before instance cancel event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeInstanceCancelEvent(I instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected E createNextEvent() {
		return (E) new AfterInstanceCancelEvent<I, TwoPhaseEvent>(getInstance());
	}
}
