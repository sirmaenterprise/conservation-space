package com.sirma.itt.emf.event;

import com.sirma.itt.emf.instance.model.Instance;

/**
 * Base instance two phase event.
 * 
 * @param <I>
 *            the instance type
 * @param <E>
 *            the next phase event type
 * @author BBonev
 */
@SuppressWarnings("unchecked")
public abstract class AbstractInstanceTwoPhaseEvent<I extends Instance, E extends TwoPhaseEvent>
		extends AbstractTwoPhaseEvent<E> {

	/** The instance. */
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
