package com.sirma.itt.objects.web.object;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Provider for config options.
 *
 * @author yasko
 */
@Named
@ApplicationScoped
public class ObjectsConfig {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.attach.defaultSelectedTypes", label = "Used to specify types selected by default when searching for objects to attach to a section.")
	private ConfigurationProperty<String> attachDefaultSelectedTypes;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.attach.availableTypesFilter", defaultValue = "emf:DomainObject", label = "Used to filter the types available when searching for an object to attach to a section.")
	private ConfigurationProperty<String> attachAvailableTypesFilter;

	/**
	 * @return the attachDefaultSelectedTypes
	 */
	public String getAttachDefaultSelectedTypes() {
		return attachDefaultSelectedTypes.get();
	}

	/**
	 * @return the attachAvailableTypesFilter
	 */
	public String getAttachAvailableTypesFilter() {
		return attachAvailableTypesFilter.get();
	}

}
