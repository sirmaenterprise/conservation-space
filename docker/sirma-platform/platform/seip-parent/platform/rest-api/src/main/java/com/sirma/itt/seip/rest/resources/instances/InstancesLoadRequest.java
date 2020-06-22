package com.sirma.itt.seip.rest.resources.instances;

import java.util.Collection;

/**
 * Contains information used to load specific instances. There is an option to specify properties that should be load
 * for those instances.
 *
 * @author A. Kunchev
 */
public class InstancesLoadRequest {

	/** The ids of the instances, which should be load. */
	private Collection<String> instanceIds;

	/**
	 * (Optional) The properties that should be load for the instances. All properties will be load, if not specified.
	 */
	private Collection<String> properties;

	/** Used to specify, if information about soft deleted instances should be returned. Default value: {@code false} */
	private boolean allowDeleted = false;

	public Collection<String> getInstanceIds() {
		return instanceIds;
	}

	public InstancesLoadRequest setInstanceIds(Collection<String> instanceIds) {
		this.instanceIds = instanceIds;
		return this;
	}

	public Collection<String> getProperties() {
		return properties;
	}

	public InstancesLoadRequest setProperties(Collection<String> properties) {
		this.properties = properties;
		return this;
	}

	public boolean getAllowDeleted() {
		return allowDeleted;
	}

	public InstancesLoadRequest setAllowDeleted(boolean allowDeleted) {
		this.allowDeleted = allowDeleted;
		return this;
	}

}