package com.sirma.itt.seip.domain.event;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;

/**
 * Base instance two phase event.
 *
 * @param <I>
 *            the instance type
 * @param <E>
 *            the next phase event type
 * @author BBonev
 */
public abstract class AbstractInstanceTwoPhaseEvent<I extends Instance, E extends TwoPhaseEvent>
		extends AbstractTwoPhaseEvent<E> {

	private final I instance;

	/**
	 * Instantiates a new abstract case two phase event.
	 *
	 * @param instance
	 *            the instance
	 */
	public AbstractInstanceTwoPhaseEvent(I instance) {
		this.instance = instance;
	}

	/**
	 * Gets the single instance of AbstractCaseTwoPhaseEvent.
	 *
	 * @return single instance of AbstractCaseTwoPhaseEvent
	 */
	public I getInstance() {
		return instance;
	}
}
