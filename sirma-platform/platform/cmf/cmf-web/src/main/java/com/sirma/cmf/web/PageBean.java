package com.sirma.cmf.web;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Page action.
 *
 * @author svelikov
 */
@Named
@SessionScoped
public class PageBean implements Serializable {

	private static final long serialVersionUID = 2839543463618147298L;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "clientside.rnc.debug.mode", type = Boolean.class, defaultValue = "false", label = "To enable client site debug mode of RnC")
	private ConfigurationProperty<Boolean> rncModuleDebugMode;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "ui.autocomplete.minimumitems", type = Integer.class, defaultValue = "5", label = "The minimum number of results that must be initially populated in order to keep the search field in autocomplete fields visible.")
	private ConfigurationProperty<Integer> uiAutocompleteMinimumItems;

	/**
	 * Getter method for rncModuleDebugMode.
	 *
	 * @return the rncModuleDebugMode
	 */
	public Boolean getRncModuleDebugMode() {
		return rncModuleDebugMode.get();
	}

	/**
	 * Getter method for uiAutocompleteMinimumItems.
	 *
	 * @return the uiAutocompleteMinimumItems
	 */
	public Integer getUiAutocompleteMinimumItems() {
		return uiAutocompleteMinimumItems.get();
	}

}
