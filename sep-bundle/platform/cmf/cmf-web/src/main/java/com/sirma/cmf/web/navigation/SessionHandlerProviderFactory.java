package com.sirma.cmf.web.navigation;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.emf.event.ApplicationStartupEvent;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;

/**
 * A factory for creating SessionHandlerProvider objects.
 * 
 * @author svelikov
 */
public class SessionHandlerProviderFactory {

	/** The factory. */
	@Inject
	private Instance<SessionHandlerProvider> factory;

	/** The session handler provider. */
	private static SessionHandlerProvider sessionHandlerProvider;

	/**
	 * On application start.
	 * 
	 * @param event
	 *            the event
	 */
	public void onApplicationStart(@Observes ApplicationStartupEvent event) {
		if (!factory.isUnsatisfied()) {
			sessionHandlerProvider = factory.get();
		}
	}

	/**
	 * Gets the SessionHandlerProvider.
	 * 
	 * @return the SessionHandlerProvider
	 */
	public static SessionHandlerProvider getSessionHandlerProvider() {
		if (sessionHandlerProvider == null) {
			throw new EmfConfigurationException("SessionHandlerProvider implementation not found!");
		}
		return sessionHandlerProvider;
	}
}
