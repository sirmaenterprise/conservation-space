package com.sirma.itt.emf.event.instance;

import com.sirma.itt.emf.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Base event fired after the instance is persisted for the first time.
 * 
 * @param <I>
 *            the concrete instance type
 * @param <E>
 *            the next event type
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Base event fired before the instance is persisted for the first time.")
public class AfterInstancePersistEvent<I extends Instance, E extends TwoPhaseEvent> extends
		AbstractInstanceTwoPhaseEvent<I, E> {

	/**
	 * Instantiates a new before instance persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterInstancePersistEvent(I instance) {
		super(instance);
	}

	@Override
	protected E createNextEvent() {
		return null;
	}

}
