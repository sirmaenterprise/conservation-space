package com.sirma.itt.emf.security;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.security.model.User;

/**
 * The Class AuthenticationServiceMock.
 */
@Specializes
public class AuthenticationServiceMock extends AuthenticationServiceImpl {

	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_CONTAINER)
	private String defaultContainer;
	/**
	 *
	 */
	private static final long serialVersionUID = 1697055105500202910L;

	@Override
	public boolean isAuthenticated() {

		return false;
	}

	@Override
	public String getCurrentContainer() {
		if (getCurrentUser() != null) {
			if (getCurrentUser().getTenantId() != null) {
				return getCurrentUser().getTenantId();
			}
		}
		return defaultContainer;
	}

	@Produces
	@CurrentUser
	@RequestScoped
	@Named("currentUser")
	@Override
	public User getCurrentUser() {
		User currentUser = super.getCurrentUser();
		if (currentUser == null) {
			authenticateAsAdmin();
			currentUser = super.getCurrentUser();
		}
		return currentUser;
	}

}
