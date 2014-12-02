package com.sirma.cmf.web.userdashboard.event;

import com.sirma.itt.emf.event.AbstractContextEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * DashletToolbarActionEvent is fired when user executes some action trough any dashlet toolbar.
 * This event carries the navigation string and can hold some additional data stored in underlying
 * context.
 * 
 * @author svelikov
 */
@Documentation("DashletToolbarActionEvent is fired when user selects any item in the main menu. This event carries the navigation string and can hold some additional data stored in underlying context.")
public class DashletToolbarActionEvent extends AbstractContextEvent {

	/** The navigation. */
	private String navigation;

	/**
	 * Instantiates a new dashlet toolbar action event.
	 */
	public DashletToolbarActionEvent() {
	}

	/**
	 * Instantiates a new main menu event.
	 * 
	 * @param navigation
	 *            the navigation
	 */
	public DashletToolbarActionEvent(String navigation) {
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
