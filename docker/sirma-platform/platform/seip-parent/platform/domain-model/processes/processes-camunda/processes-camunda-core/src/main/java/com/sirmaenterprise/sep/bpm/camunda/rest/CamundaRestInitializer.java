package com.sirmaenterprise.sep.bpm.camunda.rest;

import static org.jboss.resteasy.plugins.servlet.ExtendedResteasyServletInitializer.addIgnoredPackage;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

/**
 * Removes incompatible rest resources from auto discovered set.
 *
 * @author bbanchev
 */
public class CamundaRestInitializer implements Extension {

	/**
	 * Initialize the excluded non-compatible resources from auto initialization.
	 *
	 * @param bbd
	 *            the observed event
	 */
	@SuppressWarnings("static-method")
	public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
		appendPackages();
	}

	private static void appendPackages() {
		addIgnoredPackage("org.camunda.bpm.engine.rest.history");
		addIgnoredPackage("org.camunda.bpm.engine.rest.impl.history");
		addIgnoredPackage("org.camunda.bpm.engine.rest.sub.runtime");
		addIgnoredPackage("org.camunda.bpm.engine.rest.sub.identity");
		addIgnoredPackage("org.camunda.bpm.engine.rest.sub.history");
		addIgnoredPackage("org.camunda.bpm.engine.rest.sub.task");
		addIgnoredPackage("org.camunda.bpm.engine.rest.sub.batch");
		addIgnoredPackage("org.camunda.bpm.engine.rest.sub.management");
		addIgnoredPackage("org.camunda.bpm.engine.rest.sub");
		addIgnoredPackage("org.camunda.bpm.engine.rest.sub.externaltask");
		addIgnoredPackage("org.camunda.bpm.engine.rest.sub.metrics");
		addIgnoredPackage("org.camunda.bpm.engine.rest.sub.repository");
	}
}