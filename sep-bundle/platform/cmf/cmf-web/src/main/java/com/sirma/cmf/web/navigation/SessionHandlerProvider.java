package com.sirma.cmf.web.navigation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryBinding;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryEvent;
import com.sirma.itt.commons.utils.string.StringUtils;

/**
 * SessionHandlerProvider.
 * 
 * @author svelikov
 */
@ApplicationScoped
public class SessionHandlerProvider {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/** The session handler. */
	@Inject
	private EmfNavigationSession emfSessionHandler;

	@Inject
	private DocumentContext documentContext;

	/** The navigation history event. */
	@Inject
	private Event<NavigationHistoryEvent> navigationHistoryEvent;

	/**
	 * Getter method for emfSessionHandler.
	 * 
	 * @return the emfSessionHandler
	 */
	public EmfNavigationSession getEmfSessionHandler() {
		return emfSessionHandler;
	}

	/**
	 * Getter method for documentContext.
	 * 
	 * @return the documentContext
	 */
	public DocumentContext getDocumentContext() {
		return documentContext;
	}

	/**
	 * Fire navigation history event.
	 * 
	 * @param action
	 *            the path
	 */
	public void fireNavigationHistoryEvent(String action) {
		log.debug("Fire navigation history event for action[{}]", action);
		String currentPath = "";
		if (StringUtils.isNotNullOrEmpty(action)) {
			currentPath = action;
		}
		NavigationHistoryEvent event = new NavigationHistoryEvent();
		navigationHistoryEvent.select(new NavigationHistoryBinding(currentPath)).fire(event);
	}

}
