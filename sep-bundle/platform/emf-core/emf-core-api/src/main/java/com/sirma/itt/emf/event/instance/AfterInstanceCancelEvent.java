package com.sirma.itt.emf.event.instance;

import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after a instance has been canceled. The event will carry the instance that has been
 * canceled.<br>
 * <b>NOTE</b>: not all operations that required instance will work with the instance.
 * 
 * @param <I>
 *            the instance type
 * @param <E>
 *            the secondary event type type
 */
@Documentation("Event fired after a instance has been canceled. The event will carry the instance that has been canceled."
		+ "<br><b>NOTE</b>: not all operations that required instance will work with the instance.")
public class AfterInstanceCancelEvent<I extends Instance, E extends TwoPhaseEvent> extends
		AbstractInstanceTwoPhaseEvent<I, E> {

	/**
	 * Instantiates a new after instance cancel event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterInstanceCancelEvent(I instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected E createNextEvent() {
		return null;
	}

}
