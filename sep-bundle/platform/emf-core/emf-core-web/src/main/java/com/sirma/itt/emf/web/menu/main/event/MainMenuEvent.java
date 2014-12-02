package com.sirma.itt.emf.web.menu.main.event;

import com.sirma.itt.emf.event.AbstractContextEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * MainMenuEvent is fired when user selects any item in the main menu. This event carries the
 * navigation string and can hold some additional data stored in underlying context.
 * 
 * @author svelikov
 */
@Documentation("MainMenuEvent is fired when user selects any item in the main menu. This event carries the navigation string and can hold some additional data stored in underlying context.")
public class MainMenuEvent extends AbstractContextEvent {

	/** The navigation. */
	private String navigation;

	/**
	 * Instantiates a new main menu event.
	 * 
	 * @param navigation
	 *            the navigation
	 */
	public MainMenuEvent(String navigation) {
		this.navigation = navigation;
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
