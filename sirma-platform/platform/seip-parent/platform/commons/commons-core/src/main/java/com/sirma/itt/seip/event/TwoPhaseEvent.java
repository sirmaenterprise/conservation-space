package com.sirma.itt.seip.event;

/**
 * Base interface for two phased events. This event is fired before and after the operation. The event object will be
 * the same of the two calls. The event implementation should provide context where the calling method should put the
 * variables that need to be kept between the calls.
 *
 * @author BBonev
 */
public interface TwoPhaseEvent extends ContextEvent {

	/**
	 * Creates the next phase event. The returned object will be fired as post operation event. The object state should
	 * have the same context as the original object if needed. The implementation may cache the returned object to allow
	 * multiple calls of the method to return the same instance.
	 *
	 * @param <E>
	 *            the second phase event type
	 * @return the event object or <code>null</code> if nothing is needed to be fired
	 */
	<E extends TwoPhaseEvent> E getNextPhaseEvent();
}
