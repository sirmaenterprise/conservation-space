package com.sirma.itt.emf.event;

import java.util.HashMap;
import java.util.Map;

/**
 * Base implementation for the context event
 * 
 * @author BBonev
 */
public abstract class AbstractContextEvent implements ContextEvent {

	/** The context. Context is lazy initialized only when needed */
	protected Map<String, Object> context;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getContext() {
		if (context == null) {
			// create relatively small map
			context = new HashMap<String, Object>(8);
		}
		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addToContext(String key, Object value) {
		getContext().put(key, value);
	}

}
