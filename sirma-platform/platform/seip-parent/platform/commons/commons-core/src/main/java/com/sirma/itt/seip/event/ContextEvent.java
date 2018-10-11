package com.sirma.itt.seip.event;

import java.util.Map;

/**
 * Event that provides context to pass the arguments to it
 *
 * @author BBonev
 */
public interface ContextEvent extends EmfEvent {

	/**
	 * Common context property that specifies the event qualifiers if any. This may be automatically filled by
	 * EventService implementation on upon event fire.
	 */
	String QUALIFIERS = "$EVENT_QUALIFIERS$";

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	Map<String, Object> getContext();

	/**
	 * Adds value to context.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	void addToContext(String key, Object value);
}
