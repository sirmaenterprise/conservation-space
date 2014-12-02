package com.sirma.cmf.web.menu;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.web.config.EmfWebConfigurationProperties;

/**
 * Navigation menu backing bean.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class NavigationMenuAction extends MenuAction implements Serializable {

	private static final long serialVersionUID = -1946439397352927944L;

	/** The dms link. */
	@Inject
	@Config(name = EmfWebConfigurationProperties.DMS_LINK, defaultValue = "")
	private String dmsLink;

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * Getter method for dmsLink.
	 * 
	 * @return the dmsLink
	 */
	public String getDmsLink() {
		return dmsLink;
	}

	/**
	 * Setter method for dmsLink.
	 * 
	 * @param dmsLink
	 *            the dmsLink to set
	 */
	public void setDmsLink(String dmsLink) {
		this.dmsLink = dmsLink;
	}

}
