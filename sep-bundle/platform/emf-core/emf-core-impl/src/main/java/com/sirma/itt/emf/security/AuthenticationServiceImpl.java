/*
 *
 */
package com.sirma.itt.emf.security;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.domain.model.GenericProxy;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Default basic implementation of AuthenticationService.
 * 
 * @author BBonev
 */
@SessionScoped
public class AuthenticationServiceImpl implements AuthenticationService, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1207386961369489250L;

	/** The sso enabled. */
	@Inject
	@Config(name = SecurityConfigurationProperties.SECURITY_SSO_ENABLED, defaultValue = "false")
	private Boolean ssoEnabled;

	/** The default container. */
	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_CONTAINER)
	private String defaultContainer;

	@Inject
	@Config(name = EmfConfigurationProperties.SYSTEM_LANGUAGE, defaultValue = "bg")
	private String defaultLanguage;

	/** The currently authenticated user. */
	private User authenticatedUser;

	/** The effective authentication. */
	private User effectiveAuthentication;

	/**
	 * Invoked when {@link UserIdentifiedEvent} is fired. Constructs a CmfUser instance and fills
	 * the user data.
	 * 
	 * @param event
	 *            fired event
	 */
	public void onUserAuthenticated(@Observes UserAuthenticatedEvent event) {
		setAuthenticatedUser(event.getAuthenticatedUser());
		setEffectiveAuthentication(event.getAuthenticatedUser());
		authenticatedUser.getProperties().put(ResourceProperties.LANGUAGE, defaultLanguage);
		// TODO implement specifically
		authenticatedUser.setTenantId(defaultContainer);
	}

	/**
	 * Resets the user and effective authentication on logout.
	 * 
	 * @param event
	 *            the event
	 */
	public void onUserLogout(@Observes UserLogoutEvent event) {
		setAuthenticatedUser(null);
		setEffectiveAuthentication(null);
	}

	@Override
	public String getCurrentUserId() {
		if (getCurrentUser() != null) {
			return getCurrentUser().getName();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Produces
	@CurrentUser
	@RequestScoped
	@Named("currentUser")
	@Override
	public User getCurrentUser() {
		if (!ssoEnabled) {
			authenticateAsAdmin();
		}
		User user = getAuthenticatedUser();
		if (user instanceof GenericProxy) {
			return (User) ((GenericProxy<Resource>) user).getTarget();
		}
		return user;
	}

	@Override
	public void setAuthenticatedUser(User user) {
		authenticatedUser = user;
	}

	/**
	 * Gets the currently authenticated user.
	 * 
	 * @return the currently authenticated user
	 */
	private User getAuthenticatedUser() {
		return authenticatedUser;
	}

	@Override
	public void setEffectiveAuthentication(User user) {
		effectiveAuthentication = user;
	}

	@Override
	public User getEffectiveAuthentication() {
		return effectiveAuthentication;
	}

	@Override
	public void authenticateAsAdmin() {
		User adminUser = SecurityContextManager.getAdminUser();
		setAuthenticatedUser(adminUser);
		setEffectiveAuthentication(adminUser);
	}

	@Override
	public boolean isCurrentUser(String userId) {
		return EqualsHelper.nullSafeEquals(getCurrentUserId(), userId, true);
	}

	@Override
	public String getCurrentContainer() {
		// TODO get it from the authenticated user
		String container = defaultContainer;
		return container;
	}

	@Override
	public boolean isAuthenticated() {
		return getCurrentUser() != null;
	}
}
