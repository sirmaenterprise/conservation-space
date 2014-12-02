package com.sirma.cmf.web.search.facet;

import java.io.Serializable;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

/**
 * Holder for selected search filter name.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class SelectedFilternameHolder implements Serializable {

	private static final long serialVersionUID = 3934020438684572251L;

	private String selectedFilterName;

	/**
	 * Getter method for selectedFilterName.
	 * 
	 * @return the selectedFilterName
	 */
	public String getSelectedFilterName() {
		return selectedFilterName;
	}

	/**
	 * Setter method for selectedFilterName.
	 * 
	 * @param selectedFilterName
	 *            the selectedFilterName to set
	 */
	public void setSelectedFilterName(String selectedFilterName) {
		this.selectedFilterName = selectedFilterName;
	}

}
