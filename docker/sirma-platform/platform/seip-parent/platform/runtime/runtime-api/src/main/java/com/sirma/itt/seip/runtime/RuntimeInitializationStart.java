/**
 *
 */
package com.sirma.itt.seip.runtime;

import javax.servlet.ServletContext;

/**
 * Event fired to notify that the runtime boot process is starting. This is the first event fired in the runtime.
 *
 * @author BBonev
 */
public class RuntimeInitializationStart {

	/** The servlet context. */
	private final ServletContext servletContext;

	/**
	 * Instantiates a new runtime initialization start.
	 *
	 * @param servletContext
	 *            the servlet context
	 */
	public RuntimeInitializationStart(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Gets the servlet context.
	 *
	 * @return the servlet context
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

}
