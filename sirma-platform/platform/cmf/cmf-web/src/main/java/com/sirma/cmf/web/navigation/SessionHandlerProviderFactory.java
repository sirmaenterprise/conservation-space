package com.sirma.cmf.web.navigation;

import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * A factory for creating SessionHandlerProvider objects.
 *
 * @author svelikov
 */
public class SessionHandlerProviderFactory {

	/** The session handler provider. */
	private static SessionHandlerProvider sessionHandlerProvider;

	/**
	 * On application start.
	 *
	 * @param event
	 *            the event
	 */
	@Startup(phase = StartupPhase.BEFORE_APP_START)
	static void onApplicationStart(SessionHandlerProvider handlerProvider) {
		setSessionHandlerProvider(handlerProvider);
	}

	/**
	 * Sets the session handler provider.
	 *
	 * @param sessionHandlerProvider
	 *            the new session handler provider
	 */
	private static void setSessionHandlerProvider(SessionHandlerProvider sessionHandlerProvider) {
		SessionHandlerProviderFactory.sessionHandlerProvider = sessionHandlerProvider;
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
