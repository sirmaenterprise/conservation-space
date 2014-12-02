package com.sirma.cmf.web.menu.main;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.cmf.web.Action;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.web.menu.main.event.MainMenuActionBinding;
import com.sirma.itt.emf.web.menu.main.event.MainMenuEvent;

/**
 * MainMenuAction backing bean.
 * 
 * @author svelikov
 */
@Named
public class MainMenuAction extends Action {

	@Inject
	private Event<MainMenuEvent> mainMenuEvent;

	/**
	 * Execute action.
	 * 
	 * @param menu
	 *            the menu
	 * @param action
	 *            the action
	 * @param leaveContext
	 *            if context should remain after this operation
	 * @return the string
	 */
	public String executeAction(String menu, String action, boolean leaveContext) {
		TimeTracker timer = TimeTracker.createAndStart();
		log.debug("Executing main menu action[" + action + "] menu[" + menu + "] leaveContext ["
				+ leaveContext + "]");

		if (leaveContext) {
			getDocumentContext().clearAndLeaveContext();
		} else {
			getDocumentContext().clear();
		}

		MainMenuEvent event = fireMenuEvent(menu, action);
		log.debug("Main menu action took " + timer.stopInSeconds() + " s");
		return event.getNavigation();
	}

	/**
	 * Fire menu event.
	 * 
	 * @param menu
	 *            the menu
	 * @param action
	 *            the action
	 * @return the main menu event
	 */
	private MainMenuEvent fireMenuEvent(String menu, String action) {

		MainMenuActionBinding binding = new MainMenuActionBinding(action);

		MainMenuEvent event = createEvent(createNavigationString(menu, action));
		mainMenuEvent.select(binding).fire(event);

		return event;
	}

	/**
	 * Creates the event.
	 * 
	 * @param navigation
	 *            the navigation
	 * @return the main menu event
	 */
	public MainMenuEvent createEvent(String navigation) {
		MainMenuEvent event = new MainMenuEvent(navigation);
		return event;
	}

	/**
	 * Creates the navigation string.
	 * 
	 * @param menu
	 *            the menu
	 * @param action
	 *            the action
	 * @return the string
	 */
	private String createNavigationString(String menu, String action) {
		return action;
	}
}
