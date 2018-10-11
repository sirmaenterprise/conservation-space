package com.sirma.itt.seip.rest.resources.instances;

import java.util.Collection;

import com.google.common.base.Objects;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.handlers.writers.PropertiesFilterBuilder;

/**
 * Contains the data from the instances loading request or in other words the loaded instances that should be returned
 * to the client and properties filter which is used to as an option to remove not needed properties, when the instances
 * are converted to JSON format.
 *
 * @author A. Kunchev
 */
public class InstancesLoadResponse {

	private Collection<Instance> instances;
	private PropertiesFilterBuilder propertiesFilter;

	public Collection<Instance> getInstances() {
		return instances;
	}

	public InstancesLoadResponse setInstances(Collection<Instance> instances) {
		this.instances = instances;
		return this;
	}

	public PropertiesFilterBuilder getPropertiesFilter() {
		return Objects.firstNonNull(propertiesFilter, PropertiesFilterBuilder.MATCH_ALL);
	}

	public InstancesLoadResponse setPropertiesFilter(PropertiesFilterBuilder propertiesFilter) {
		this.propertiesFilter = propertiesFilter;
		return this;
	}

}
