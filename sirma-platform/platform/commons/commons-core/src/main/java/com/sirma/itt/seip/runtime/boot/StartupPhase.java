package com.sirma.itt.seip.runtime.boot;

/**
 * Defines the startup phases that component can be assigned for loading. The phases are executed in order. The loading
 * advances to the next phase after
 *
 * @author BBonev
 */
public enum StartupPhase {
	/**
	 * Phase during application deployment. This is the first phase on startup. In this phase should be defined system .
	 * If a component throws exception or takes too long can stop the deployment.
	 */
	DEPLOYMENT,

	/**
	 * Next phase called after successful finish of the deployment step. This is called before users have access to the
	 * server. This phase could be used to user data patching or migration. If component fails to this them the server
	 * will not be accessible until resolved.
	 */
	BEFORE_APP_START,

	/**
	 * A phase at witch the application is accessible by users. This is useful for components that just need to be
	 * notified for server start. Components initialized in this phase will not block the server startup.
	 */
	AFTER_APP_START,

	/**
	 * The last phase only to indicate that application has started. This could not be used for scheduling startup
	 * components but only is used for status info.
	 */
	STARTUP_COMPLETE;
}
