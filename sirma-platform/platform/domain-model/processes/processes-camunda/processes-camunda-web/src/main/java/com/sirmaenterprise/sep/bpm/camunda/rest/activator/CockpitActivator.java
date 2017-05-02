package com.sirmaenterprise.sep.bpm.camunda.rest.activator;

import javax.ws.rs.ApplicationPath;

import org.camunda.bpm.cockpit.impl.web.CockpitApplication;

/**
 * The {@link CockpitActivator} activates the Camunda Cockpit rest services.
 *
 * @author bbanchev
 */
@ApplicationPath("/api/cockpit")
public class CockpitActivator extends CockpitApplication {
	// no custom implementation
}
