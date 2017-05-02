package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;

/**
 * Base event fired before the instance is persisted for the first time.
 *
 * @param <I>
 *            the concrete instance type
 * @param <E>
 *            the next event type type
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Base event fired before the instance is persisted for the first time.")
public class BeforeInstancePersistEvent<I extends Instance, E extends AfterInstancePersistEvent<I, TwoPhaseEvent>>
		extends AbstractInstanceTwoPhaseEvent<I, E> {

	/**
	 * Instantiates a new before instance persist event.
	 *
	 * @param instance
	 *            the instance
	 */
	public BeforeInstancePersistEvent(I instance) {
		super(instance);
	}

	@Override
	protected E createNextEvent() {
		return (E) new AfterInstancePersistEvent<I, TwoPhaseEvent>(getInstance());
	}

}
