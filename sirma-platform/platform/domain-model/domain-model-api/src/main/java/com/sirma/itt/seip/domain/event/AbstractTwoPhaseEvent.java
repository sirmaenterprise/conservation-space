package com.sirma.itt.seip.domain.event;

import com.sirma.itt.seip.event.TwoPhaseEvent;

/**
 * Base implementation for the two phase event.
 *
 * @param <E>
 *            the next phase event type
 * @author BBonev
 */
public abstract class AbstractTwoPhaseEvent<E extends TwoPhaseEvent> extends AbstractContextEvent
		implements TwoPhaseEvent {

	private E event;

	@Override
	@SuppressWarnings("unchecked")
	public E getNextPhaseEvent() {
		if (event == null) {
			event = createNextEvent();
			// does not call getContext method or the context will be created with not purpose and
			// will lead to memory/garbage leak
			if (event != null && context != null) {
				event.getContext().putAll(getContext());
			}
		}
		return event;
	}

	/**
	 * Creates the next event object instance that will be populated and returned.
	 *
	 * @return the empty event object or <code>null</code> if not supported
	 */
	protected abstract E createNextEvent();
}
