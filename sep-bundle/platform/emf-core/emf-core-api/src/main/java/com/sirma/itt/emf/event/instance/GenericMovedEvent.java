package com.sirma.itt.emf.event.instance;

import com.sirma.itt.emf.event.instance.InstanceMovedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

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
