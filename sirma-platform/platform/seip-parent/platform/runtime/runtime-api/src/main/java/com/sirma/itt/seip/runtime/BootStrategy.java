package com.sirma.itt.seip.runtime;

import java.util.function.Consumer;

import javax.servlet.ServletContext;

/**
 * Boot strategy marker that defines the way startup components are executed.
 *
 * @author BBonev
 */
public interface BootStrategy {

	/**
	 * Execute the boot strategy.
	 *
	 * @param servletContext
	 * 		The servlet context in which components start
	 * @param onComplete
	 * 		a handler that should be called when the strategy completes the startup process. This allows realisation
	 * 		of asynchronous parallel implementations
	 */
	void executeStrategy(ServletContext servletContext, Consumer<ServletContext> onComplete);
}
