package com.sirma.cmf.web.menu;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.Action;

/**
 * MenuAction backing bean.
 * 
 * @author svelikov
 */
@Named
public abstract class MenuAction extends Action {

	private String selectedMenu;

	/** Menu action event. */
	@Inject
	@NavigationMenu
	protected Event<NavigationMenuEvent> navigationMenuEvent;

	/**
	 * Execute action and clears the context object.
	 * 
	 * @param menu
	 *            the menu
	 * @param action
	 *            the action
	 * @param leaveContext
	 *            if context should remain after this operation
	 * @return the string
	 */
	public abstract String executeAction(String menu, String action, boolean leaveContext);

	/**
	 * Fire navigation event.
	 * 
	 * @param menu
	 *            the menua
	 * @param action
	 *            the action
	 * @return the navigation event
	 */
	public NavigationMenuEvent fireNavigationEvent(final String menu, final String action) {
		final NavigationMenuEvent event = createNavigationEvent(menu, action);

		MenuActionBinding menuActionBinding = new MenuActionBinding(action);

		navigationMenuEvent.select(menuActionBinding).fire(event);
		return event;
	}

	/**
	 * The actual navigation method. The outcome is taken from the
	 * 
	 * @param event
	 *            the event
	 * @return Navigation string. {@link NavigationMenuEvent}.
	 */
	public String navigate(final NavigationMenuEvent event) {

		String navigationString = event.getNavigation();

		log.debug("CMFWeb: Executing MenuAction.navigate: [" + navigationString + "]");

		return navigationString;
	}

	/**
	 * Creates a {@link NavigationMenuEvent} object.
	 * 
	 * @param menu
	 *            The menu name to set in event object.
	 * @param action
	 *            The action name to set in event object.
	 * @return {@link NavigationEvent}.
	 */
	protected NavigationMenuEvent createNavigationEvent(final String menu, final String action) {

		final NavigationMenuEvent navigationEvent = new NavigationMenuEvent(menu, action);

		return navigationEvent;
	}

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
