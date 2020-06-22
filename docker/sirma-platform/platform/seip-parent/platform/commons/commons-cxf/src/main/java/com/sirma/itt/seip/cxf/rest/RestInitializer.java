package com.sirma.itt.seip.cxf.rest;

import static org.jboss.resteasy.plugins.servlet.ExtendedResteasyServletInitializer.addIgnoredPackage;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Update auto discovered rest resource by removing incompatible CXF resources.
 * 
 * @author bbanchev
 */
public class RestInitializer implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		addIgnoredPackage("org.apache.cxf.jaxrs.servlet");
		addIgnoredPackage("org.apache.cxf.jaxrs.provider");
		addIgnoredPackage("org.apache.cxf.jaxrs.validation");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// skip
	}

}
