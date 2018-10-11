package com.sirma.itt.seip.runtime;

import javax.servlet.ServletContext;

/**
 * Event fired to notify that the runtime boot process is completed.
 * 
 * @author nvelkov
 */
public class RuntimeInitializationComplete {

	private final ServletContext servletContext;

	/**
	 * Instantiates a new runtime initialization complete.
	 *
	 * @param servletContext
	 *            the servlet context
	 */
	public RuntimeInitializationComplete(ServletContext servletContext) {
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
