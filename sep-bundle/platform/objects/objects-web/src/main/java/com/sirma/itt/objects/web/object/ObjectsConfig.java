package com.sirma.itt.objects.web.object;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.objects.constants.ObjectsConfigProperties;

/**
 * Provider for config options.
 * 
 * @author yasko
 * 
 */
@Named
@ApplicationScoped
public class ObjectsConfig {

	@Inject
	@Config(name = ObjectsConfigProperties.SEARCH_ATTACH_DEFAULT_SELECTED_TYPES, defaultValue = "")
	private String attachDefaultSelectedTypes;

	@Inject
	@Config(name = ObjectsConfigProperties.SEARCH_ATTACH_AVAILABLE_TYPES_FILTER, defaultValue = "emf:DomainObject")
	private String attachAvailableTypesFilter;

	/**
	 * @return the attachDefaultSelectedTypes
	 */
	public String getAttachDefaultSelectedTypes() {
		return attachDefaultSelectedTypes;
	}

	/**
	 * @return the attachAvailableTypesFilter
	 */
	public String getAttachAvailableTypesFilter() {
		return attachAvailableTypesFilter;
	}

}
