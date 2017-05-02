package com.sirma.cmf.web.menu;

import java.io.Serializable;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.itt.seip.time.TimeTracker;

/**
 * Navigation menu backing bean.
 *
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class NavigationMenuAction extends MenuAction implements Serializable {
	private static final long serialVersionUID = 8400215259083052612L;

	@Override
	public String executeAction(final String menu, final String action, final boolean leaveContext) {
		TimeTracker timer = TimeTracker.createAndStart();
		log.debug("Executing navigation menu action[" + action + "] menu[" + menu + "]");
		final NavigationMenuEvent event = fireNavigationEvent(menu, action);

		if (leaveContext) {
			getDocumentContext().clearAndLeaveContext();
		}
		log.debug("Navigation menu action took " + timer.stopInSeconds() + " s");
		return super.navigate(event);
	}
}
