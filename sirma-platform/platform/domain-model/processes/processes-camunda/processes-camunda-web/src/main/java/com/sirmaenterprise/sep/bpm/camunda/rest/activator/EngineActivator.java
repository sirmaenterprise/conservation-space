package com.sirmaenterprise.sep.bpm.camunda.rest.activator;

import java.util.Set;

import javax.ws.rs.ApplicationPath;

import org.camunda.bpm.webapp.impl.engine.EngineRestApplication;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * The {@link EngineActivator} activates the Camunda core engine rest services.
 *
 * @author bbanchev
 */
@ApplicationPath("/api/engine")
public class EngineActivator extends EngineRestApplication {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = super.getClasses();
		classes.remove(JacksonJsonProvider.class);
		return classes;
	}
}
