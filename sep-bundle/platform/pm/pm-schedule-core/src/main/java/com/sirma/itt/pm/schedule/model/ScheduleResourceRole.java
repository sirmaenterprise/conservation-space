package com.sirma.itt.pm.schedule.model;

import com.sirma.itt.emf.resources.model.ResourceRole;


/**
 * Just a wrapper class to wrap a {@link ResourceRole} object. Used to define different {@link TypeConverter} to be used in the scheduler.
 */
public class ScheduleResourceRole {
	
	/** The resource role. */
	private ResourceRole resourceRole;
	
	/**
	 * Instantiates a new schedule resource role.
	 *
	 * @param resourceRole the resource role
	 */
	public ScheduleResourceRole(ResourceRole resourceRole) {
		this.resourceRole = resourceRole;
	}

	/**
	 * Gets the resource role.
	 *
	 * @return the resource role
	 */
	public ResourceRole getResourceRole() {
		return resourceRole;
	}

	/**
	 * Sets the resource role.
	 *
	 * @param resourceRole the new resource role
	 */
	public void setResourceRole(ResourceRole resourceRole) {
		this.resourceRole = resourceRole;
	}

}
