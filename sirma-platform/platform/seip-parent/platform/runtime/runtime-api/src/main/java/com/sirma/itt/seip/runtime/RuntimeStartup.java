/*
 *
 */
package com.sirma.itt.seip.runtime;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Controller that manages the sequence of startup events and component loading.
 *
 * @author BBonev
 */
@WebListener
public class RuntimeStartup implements ServletContextListener {

	@Inject
	private Event<RuntimeInitializationStart> runtimeInitEvent;

	@Inject
	private Event<RuntimeInitializationComplete> runtimeCompleteEvent;

	@Inject
	private BootStrategyResolver bootStrategyProvider;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// notify for beginning of the startup process
		runtimeInitEvent.fire(new RuntimeInitializationStart(sce.getServletContext()));

		// execute the boot strategy
		bootStrategyProvider.resolveBootStrategy()
				.executeStrategy(sce.getServletContext(),
						ctx -> runtimeCompleteEvent.fire(new RuntimeInitializationComplete(ctx)));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// nothing to add here
	}
}
