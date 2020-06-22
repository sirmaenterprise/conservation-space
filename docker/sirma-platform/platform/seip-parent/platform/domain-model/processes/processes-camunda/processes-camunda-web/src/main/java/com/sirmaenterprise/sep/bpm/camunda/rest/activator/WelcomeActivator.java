package com.sirmaenterprise.sep.bpm.camunda.rest.activator;

import javax.ws.rs.ApplicationPath;

import org.camunda.bpm.welcome.impl.web.WelcomeApplication;

/**
 * The {@link WelcomeActivator} activates the Camunda welcome application rest services.
 *
 * @author bbanchev
 */
@ApplicationPath("/api/welcome")
public class WelcomeActivator extends WelcomeApplication {
	// no custom implementation
}
