package com.sirma.sep.keycloak.rest;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.jboss.resteasy.plugins.servlet.ExtendedResteasyServletInitializer;

/**
 * Removes incompatible Keycloak rest resources from auto discovered set.
 *
 * @author smustafov
 */
public class KeycloakRestInitializer implements Extension {

	/**
	 * Initializes the excluded non-compatible resources from auto initialization before any resource is collected by
	 * RestEasy.
	 *
	 * @param event the event
	 */
	public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
		ExtendedResteasyServletInitializer.addIgnoredPackage("org.keycloak.admin.client.resource");
	}

}
