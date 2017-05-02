package com.sirma.itt.seip.event;

/**
 * Abstract provider for events based on {@link EventType} enum. The events created by the provider are generic events
 * that are valid for all supported instance types in the application.
 *
 * @author BBonev
 * @param <I>
 *            the generic type
 */
public interface EventProvider<I> {
	/**
	 * Creates the event of the given type and target instance. If the event requires other arguments they could be
	 * passed in the varargs.
	 *
	 * @param type
	 *            the type of the event to create
	 * @param instance
	 *            the target instance to create event for
	 * @param operationId
	 *            an operation id to set if the event needs it
	 * @param otherArgs
	 *            the other arguments in order of appearance to be passed to events if needed
	 * @return the emf event. Note this could be <code>null</code> if the instance does not support the given event
	 *         type.
	 */
	EmfEvent createEvent(EventType type, I instance, String operationId, Object... otherArgs);
}
