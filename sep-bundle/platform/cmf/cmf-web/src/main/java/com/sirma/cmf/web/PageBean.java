package com.sirma.cmf.web;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.web.config.EmfWebConfigurationProperties;

/**
 * Page action.
 * 
 * @author svelikov
 */
@Named
@SessionScoped
public class PageBean implements Serializable {

	private static final long serialVersionUID = -3598943037214110129L;

	/** The rnc module debug mode. */
	@Inject
	@Config(name = EmfWebConfigurationProperties.CLIENTSIDE_RNC_DEBUG_MODE, defaultValue = "false")
	private Boolean rncModuleDebugMode;

	@Inject
	@Config(name = EmfWebConfigurationProperties.UI_AUTOCOMPLETE_MINIMUMITEMS, defaultValue = "5")
	private Integer uiAutocompleteMinimumItems;

	/**
	 * Getter method for rncModuleDebugMode.
	 * 
	 * @return the rncModuleDebugMode
	 */
	public Boolean getRncModuleDebugMode() {
		return rncModuleDebugMode;
	}

	/**
	 * Setter method for rncModuleDebugMode.
	 * 
	 * @param rncModuleDebugMode
	 *            the rncModuleDebugMode to set
	 */
	public void setRncModuleDebugMode(Boolean rncModuleDebugMode) {
		this.rncModuleDebugMode = rncModuleDebugMode;
	}

	/**
	 * Getter method for uiAutocompleteMinimumItems.
	 * 
	 * @return the uiAutocompleteMinimumItems
	 */
	public Integer getUiAutocompleteMinimumItems() {
		return uiAutocompleteMinimumItems;
	}

	/**
	 * Setter method for uiAutocompleteMinimumItems.
	 * 
	 * @param uiAutocompleteMinimumItems
	 *            the uiAutocompleteMinimumItems to set
	 */
	public void setUiAutocompleteMinimumItems(Integer uiAutocompleteMinimumItems) {
		this.uiAutocompleteMinimumItems = uiAutocompleteMinimumItems;
	}

}
