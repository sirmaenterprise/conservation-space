package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event that notifies for change context. It holds information about affected instance, old parent and the new one.
 *
 * @author Boyan Tonchev.
 */
public class ParentChangedEvent extends AbstractInstanceEvent<Instance> {

	private Instance oldParent;
	private Instance newParent;

	/**
	 * Instantiates a new {@link ParentChangedEvent}.
	 *
	 * @param instance  - affected instance.
	 * @param oldParent - old parent of <code>instance</code>.
	 * @param newParent - new parent of <code>instance</code>.
	 */
	public ParentChangedEvent(Instance instance, Instance oldParent, Instance newParent) {
		super(instance);
		this.oldParent = oldParent;
		this.newParent = newParent;
	}

	/**
	 * Getter for old parent of instance.
	 *
	 * @return the old parent of instance.
	 */
	public Instance getOldParent() {
		return this.oldParent;
	}

	/**
	 * Getter for new parent of instance.
	 *
	 * @return - the new parent of instance.
	 */
	public Instance getNewParent() {
		return this.newParent;
	}
}
