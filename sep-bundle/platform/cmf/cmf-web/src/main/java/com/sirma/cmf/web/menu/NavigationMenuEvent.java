package com.sirma.cmf.web.menu;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when user selects some of the navigation menu items.
 * 
 * @author svelikov
 */
@Documentation("Event fired when user selects some of the navigation menu items.")
public class NavigationMenuEvent implements EmfEvent {

	/**
	 * Menu name the triggered the event.
	 */
	private String menu;

	/**
	 * Action name.
	 */
	private String action;

	/** The navigation. */
	private String navigation;

	/**
	 * Instantiates a new navigation event.
	 * 
	 * @param menu
	 *            the menu
	 * @param action
	 *            the action
	 */
	public NavigationMenuEvent(String menu, String action) {
		this.menu = menu;
		this.action = action;
		this.navigation = getNavigationString();
	}

	/**
	 * Builds a navigation string from the menu name and action name.
	 * 
	 * @return navigation string.
	 */
	private String getNavigationString() {
		return action;
	}

	/**
	 * Gets the menu.
	 * 
	 * @return the menu
	 */
	public String getMenu() {
		return menu;
	}

	/**
	 * Sets the menu.
	 * 
	 * @param menu
	 *            the new menu
	 */
	public void setMenu(String menu) {
		this.menu = menu;
	}

	/**
	 * Gets the action.
	 * 
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Sets the action.
	 * 
	 * @param action
	 *            the new action
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * Getter method for navigation.
	 * 
	 * @return the navigation
	 */
	public String getNavigation() {
		return navigation;
	}

	/**
	 * Setter method for navigation.
	 * 
	 * @param navigation
	 *            the navigation to set
	 */
	public void setNavigation(String navigation) {
		this.navigation = navigation;
	}

}
