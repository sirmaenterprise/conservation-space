package com.sirma.itt.emf.web.application;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.web.config.EmfWebConfigurationProperties;

/**
 * Configures jsf specific parameters based on the application mode.
 * 
 * @author Adrian Mitev
 */
public class JsfProjectConfigurator {

	private static final Logger LOG = LoggerFactory.getLogger(JsfProjectConfigurator.class);

	@Inject
	@Config(name = EmfConfigurationProperties.APPLICATION_MODE_DEVELOPEMENT, defaultValue = "false")
	private Boolean devMode;

	@Inject
	@Config(name = EmfWebConfigurationProperties.UI_JSF_PHASETRACKER, defaultValue = "false")
	private Boolean phaseTracker;

	@Inject
	@Config(name = EmfWebConfigurationProperties.RESOURCES_MERGE, defaultValue = "false")
	private Boolean mergeResources;

	@Inject
	@Config(name = EmfWebConfigurationProperties.RESOURCES_MINIFY, defaultValue = "false")
	private Boolean minifyResources;

	/**
	 * Sets jsf projects when the servlet context is started.
	 * 
	 * @param event
	 *            ServletContextEvent.
	 */
	public void onApplicationStarted(@Observes ServletContextEvent event) {
		event.getServletContext().setAttribute(
				EmfConfigurationProperties.APPLICATION_MODE_DEVELOPEMENT, devMode);
		event.getServletContext().setAttribute(EmfWebConfigurationProperties.UI_JSF_PHASETRACKER,
				phaseTracker);
		event.getServletContext().setInitParameter("com.sirma.itt.faces.combineResources",
				mergeResources.toString());
		event.getServletContext().setInitParameter("com.sirma.itt.faces.compressResources",
				minifyResources.toString());

		if (devMode == Boolean.TRUE) {
			event.getServletContext().setInitParameter("javax.faces.PROJECT_STAGE", "Development");
			event.getServletContext().setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "0");

			LOG.info("Project stage set to Development");
		} else {
			event.getServletContext().setInitParameter("javax.faces.PROJECT_STAGE", "Production");
			event.getServletContext().setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "-1");
			event.getServletContext().setInitParameter("facelets.DEVELOPMENT", "true");

			LOG.info("Project stage set to Production");
		}
	}
}
