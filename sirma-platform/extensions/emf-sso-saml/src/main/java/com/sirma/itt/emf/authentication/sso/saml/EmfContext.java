package com.sirma.itt.emf.authentication.sso.saml;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.servlet.ServletContext;

import com.sirma.itt.seip.runtime.RuntimeInitializationStart;

/**
 * The Class EmfFilterConfig used to store the application context (i.e. /emf/..)
 *
 * @author Ivo Rusev
 */
@ApplicationScoped
public class EmfContext {

	private ServletContext servletContext;

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

	/**
	 * Context initialized.
	 *
	 * @param ris
	 *            the ris
	 */
	public void contextInitialized(@Observes RuntimeInitializationStart ris) {
		servletContext = ris.getServletContext();
	}
}
