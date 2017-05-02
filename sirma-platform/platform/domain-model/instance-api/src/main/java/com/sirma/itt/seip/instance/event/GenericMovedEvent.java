package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Event fired when instance is moved from one parent to other
 *
 * @author bbanchev
 */
@Documentation("")
public class GenericMovedEvent extends InstanceMovedEvent<Instance> {

	/**
	 * Instantiates a new generic moved event.
	 *
	 * @param instance
	 *            the instance moved
	 * @param oldParent
	 *            the old parent
	 * @param newParent
	 *            the new parent
	 */
	public GenericMovedEvent(Instance instance, Instance oldParent, Instance newParent) {
		super(instance, oldParent, newParent);
	}

}
