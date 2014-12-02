package com.sirma.itt.emf.event.resource;

import java.util.List;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.util.Documentation;

/**
 * The ResourcesChangedEvent is fired as post event for updating resources for instance. The
 * currently saved values and the old values are provided
 *
 * @author bbanchev
 */
@Documentation("Fired as post event for current list of resources update.")
public class ResourcesChangedEvent extends AbstractInstanceEvent<Instance> {

	/** The new resources. */
	private List<Resource> newResources;

	/** The old resources. */
	private List<Resource> oldResources;

	/**
	 * Instantiates a new resources changed event.
	 *
	 * @param instance
	 *            the instance
	 * @param newResources
	 *            the new resources
	 * @param oldResources
	 *            the old resources
	 */
	public ResourcesChangedEvent(Instance instance, List<Resource> newResources,
			List<Resource> oldResources) {
		super(instance);
		this.setNewResources(newResources);
		this.setOldResources(oldResources);
	}

	/**
	 * Gets the old resources.
	 *
	 * @return the oldResources
	 */
	public List<Resource> getOldResources() {
		return oldResources;
	}

	/**
	 * Sets the old resources.
	 *
	 * @param oldResources
	 *            the oldResources to set
	 */
	public void setOldResources(List<Resource> oldResources) {
		this.oldResources = oldResources;
	}

	/**
	 * @return the newResources
	 */
	public List<Resource> getNewResources() {
		return newResources;
	}

	/**
	 * @param newResources
	 *            the newResources to set
	 */
	public void setNewResources(List<Resource> newResources) {
		this.newResources = newResources;
	}

}
