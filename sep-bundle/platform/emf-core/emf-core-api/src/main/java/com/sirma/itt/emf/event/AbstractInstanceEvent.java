package com.sirma.itt.emf.event;

import com.sirma.itt.emf.util.Documentation;

/**
 * Base event class for events that are fired over an instance object.
 * 
 * @param <I>
 *            the concrete instance type
 * @author BBonev
 */
@Documentation("Base event class for events that are fired over an instance object")
public abstract class AbstractInstanceEvent<I> implements EmfEvent {

	/** The instance. */
	protected final I instance;

	/**
	 * Instantiates a new abstract instance event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AbstractInstanceEvent(I instance) {
		this.instance = instance;
	}

	/**
	 * Getter method for instance.
	 * 
	 * @return the instance
	 */
	public I getInstance() {
		return instance;
	}

}
