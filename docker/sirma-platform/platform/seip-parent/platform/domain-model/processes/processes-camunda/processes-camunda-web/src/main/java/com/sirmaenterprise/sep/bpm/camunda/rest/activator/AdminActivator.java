package com.sirmaenterprise.sep.bpm.camunda.rest.activator;

import java.util.Set;

import javax.ws.rs.ApplicationPath;

import org.camunda.bpm.admin.impl.web.AdminApplication;
import org.camunda.bpm.webapp.impl.security.auth.UserAuthenticationResource;

import com.sirmaenterprise.sep.bpm.camunda.rest.UserSSOAuthenticationResource;

/**
 * The {@link AdminActivator} activates the Camunda admin rest services. {@link UserAuthenticationResource} is replaced
 * by the SSO implementation {@link UserSSOAuthenticationResource}.
 *
 * @author bbanchev
 */
@ApplicationPath("/api/admin")
public class AdminActivator extends AdminApplication {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = super.getClasses();
		classes.remove(UserAuthenticationResource.class);
		classes.add(UserSSOAuthenticationResource.class);
		return classes;
	}
}
