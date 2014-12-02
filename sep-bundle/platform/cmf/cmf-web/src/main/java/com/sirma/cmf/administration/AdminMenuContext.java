package com.sirma.cmf.administration;

import java.io.Serializable;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

/**
 * Admin menu context holder.
 * 
 * @author y.yordanov
 */
@Named
@ViewAccessScoped
public class AdminMenuContext implements Serializable {

	private static final long serialVersionUID = -4197245229008014652L;

	private String selectedMenu;

	/**
	 * Getter method for selectedMenu.
	 * 
	 * @return the selectedMenu
	 */
	public String getSelectedMenu() {
		return selectedMenu;
	}

	/**
	 * Setter method for selectedMenu.
	 * 
	 * @param selectedMenu
	 *            the selectedMenu to set
	 */
	public void setSelectedMenu(String selectedMenu) {
		this.selectedMenu = selectedMenu;
	}

}
