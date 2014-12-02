package com.sirma.itt.emf.authentication.sso.saml;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * The Class EmfFilterConfig used to store the application context (i.e.
 * /emf/..)
 * 
 * @author Ivo Rusev
 */
@ApplicationScoped
public class EmfContext {

	/** The config. */
	private FilterConfig config;

	/** The servlet context. */
	private ServletContext servletContext;

	/**
	 * Initializes the class using the {@link FilterConfig} when security
	 * filters initialize.
	 * 
	 * @param filterConfig {@link FilterConfig}
	 */
	public void init(FilterConfig filterConfig) {
		this.config = filterConfig;
		if (config != null) {
			servletContext = config.getServletContext();
		} else {
			throw new RuntimeException("Was unable to initialize servlet context: ");
		}
	}

	/**
	 * Gets the servlet context.
	 * 
	 * @return the servlet context
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * Sets the servlet context.
	 * 
	 * @param servletContext
	 *            the new servlet context
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
