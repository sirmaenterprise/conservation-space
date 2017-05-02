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

import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.security.AuthenticationService;
import com.sirma.itt.seip.resources.security.CurrentUser;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Default basic implementation of AuthenticationService.
 *
 * @author BBonev
 */
@SessionScoped
public class AuthenticationServiceImpl implements AuthenticationService, Serializable {
	private static final long serialVersionUID = -1207386961369489250L;

	private User authenticatedUser;
	private User effectiveAuthentication;

	@Inject
	private SecurityContext securityContext;
	@Inject
	private UserStore userStore;

	/**
	 * Invoked when {@link UserIdentifiedEvent} is fired.
	 *
	 * @param event
	 *            fired event
	 */
	public void onUserAuthenticated(@Observes UserAuthenticatedEvent event) {
		// backend authenticators extensively fires an UserAuthenticatedEvent and we don't want to change the current
		// session user with RunAs operations
		if (getAuthenticatedUser() != null) {
			return;
		}
		User user = (User) userStore.wrap(event.getAuthenticatedUser());
		setAuthenticatedUser(user);
		setEffectiveAuthentication(user);
	}

	/**
	 * Resets the real and effective authentication on logout.
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

	@Produces
	@CurrentUser
	@RequestScoped
	@Named("currentUser")
	@Override
	public User getCurrentUser() {
		return getAuthenticatedUser();
	}

	/**
	 * Sets the authenticated user.
	 *
	 * @param user
	 *            the new authenticated user
	 */
	private void setAuthenticatedUser(User user) {
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

	/**
	 * Sets the effective authentication.
	 *
	 * @param user
	 *            the new effective authentication
	 */
	public void setEffectiveAuthentication(User user) {
		effectiveAuthentication = user;
	}

	@Override
	public User getEffectiveAuthentication() {
		return effectiveAuthentication;
	}

	@Override
	public boolean isCurrentUser(String userId) {
		return EqualsHelper.nullSafeEquals(getCurrentUserId(), userId, true);
	}

	@Override
	public String getCurrentTenantId() {
		return securityContext.getCurrentTenantId();
	}

	@Override
	public boolean isAuthenticated() {
		return getCurrentUser() != null;
	}
}
