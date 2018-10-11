package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fired when an instance has been moved in the application graph. This means that the direct parent (owning
 * instance) of an instance has been changed.
 *
 * @param <I>
 *            the moved instance type
 * @author BBonev
 */
@Documentation("Event fired when an instance has been moved in the application graph. This means that the direct parent (owning instance) of an instance has been changed. ")
public class InstanceMovedEvent<I extends Instance> extends AbstractInstanceEvent<I> {

	/** The old parent. */
	private final Instance oldParent;

	/** The new parent. */
	private final Instance newParent;

	/**
	 * Instantiates a new instance moved event.
	 *
	 * @param instance
	 *            the instance that has been moved
	 * @param oldParent
	 *            the old parent instance
	 * @param newParent
	 *            the new parent instance
	 */
	public InstanceMovedEvent(I instance, Instance oldParent, Instance newParent) {
		super(instance);
		this.oldParent = oldParent;
		this.newParent = newParent;
	}

	/**
	 * Getter method for oldParent.
	 *
	 * @return the oldParent
	 */
	public Instance getOldParent() {
		return oldParent;
	}

	/**
	 * Getter method for newParent.
	 *
	 * @return the newParent
	 */
	public Instance getNewParent() {
		return newParent;
	}

}
