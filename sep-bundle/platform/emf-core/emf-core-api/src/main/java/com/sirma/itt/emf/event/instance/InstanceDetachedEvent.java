package com.sirma.itt.emf.event.instance;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fire when detach action is performed on the target instance. This means that the provided
 * child instance has been removed as a child of the target instance.
 * 
 * @param <I>
 *            the target instance type
 * @author BBonev
 */
@Documentation("Event fire when detach action is performed on the target instance. This means that the provided child instance has been removed as a child of the target instance.")
public class InstanceDetachedEvent<I extends Instance> extends AbstractInstanceEvent<I> {

	/** The child. */
	private final Instance child;

	/**
	 * Instantiates a new instance attached event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public InstanceDetachedEvent(I target, Instance child) {
		super(target);
		this.child = child;
	}

	/**
	 * Getter method for child.
	 * 
	 * @return the child
	 */
	public Instance getChild() {
		return child;
	}
}