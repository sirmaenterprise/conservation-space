package com.sirma.itt.emf.web.application;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Configures jsf specific parameters based on the application mode.
 *
 * @author Adrian Mitev
 */
public class JsfProjectConfigurator {

	private static final Logger LOG = LoggerFactory.getLogger(JsfProjectConfigurator.class);

	@Inject
	private ApplicationConfigurationProvider applicationConfiguration;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "ui.resources.merge", type = Boolean.class, defaultValue = "false", sensitive = true, system = true, label = "When true the css and javascript resources are merged in a single file for faster page loading")
	private ConfigurationProperty<Boolean> mergeResources;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "ui.resources.minify", type = Boolean.class, defaultValue = "false", sensitive = true, system = true, label = "When true the css and javascript resources will be minifed after merging")
	private ConfigurationProperty<Boolean> minifyResources;

	/**
	 * Sets jsf projects when the servlet context is started.
	 *
	 * @param event
	 *            ServletContextEvent.
	 */
	public void onApplicationStarted(@Observes ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();
		servletContext.setAttribute(ApplicationConfigurationProvider.APPLICATION_MODE_DEVELOPMENT,
				applicationConfiguration.getApplicationModeDevelopement());
		servletContext.setAttribute(ApplicationConfigurationProvider.UI_JSF_PHASETRACKER,
				applicationConfiguration.getJsfPhaseTracker());

		applicationConfiguration.getApplicationMode().addConfigurationChangeListener(
				c -> servletContext.setAttribute(c.getName(), c.get()));
		applicationConfiguration.getJsfPhaseTrackerConfiguration().addConfigurationChangeListener(
				c -> servletContext.setAttribute(c.getName(), c.get()));

		servletContext.setInitParameter("com.sirma.itt.faces.combineResources", mergeResources.get().toString());
		servletContext.setInitParameter("com.sirma.itt.faces.compressResources", minifyResources.get().toString());

		if (applicationConfiguration.getApplicationModeDevelopement().booleanValue()) {
			servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Development");
			servletContext.setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "0");

			LOG.info("Project stage set to Development");
		} else {
			servletContext.setInitParameter("javax.faces.PROJECT_STAGE", "Production");
			servletContext.setInitParameter("javax.faces.FACELETS_REFRESH_PERIOD", "-1");
			servletContext.setInitParameter("facelets.DEVELOPMENT", "true");

			LOG.info("Project stage set to Production");
		}
	}
}
