package com.sirma.itt.sch.web;

import org.apache.log4j.Logger;

import com.sirma.cmf.web.menu.NavigationMenuEvent;

/**
 * PMSchTest.
 * 
 * @author svelikov
 */
public class PMSchTest {

	protected static final Logger log = Logger.getLogger(PMSchTest.class);

	/**
	 * Creates the navigation menu event.
	 * 
	 * @param menu
	 *            the menu
	 * @param action
	 *            the action
	 * @return the navigation menu event
	 */
	public NavigationMenuEvent createNavigationMenuEvent(String menu, String action) {
		NavigationMenuEvent event = new NavigationMenuEvent(menu, action);

		return event;
	}

}
