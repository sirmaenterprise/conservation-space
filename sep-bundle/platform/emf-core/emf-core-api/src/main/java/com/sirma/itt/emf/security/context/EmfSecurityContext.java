package com.sirma.itt.emf.security.context;

import com.sirma.itt.emf.security.model.User;

/**
 * Default implementation for the {@link SecurityContext}.
 *
 * @author BBonev
 */
public class EmfSecurityContext implements SecurityContext, Cloneable {

	/** The authentication. */
	private User authentication;

	/** The effective authentication. */
	private User effectiveAuthentication;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getAuthentication() {
		return authentication;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getEffectiveAuthentication() {
		return effectiveAuthentication;
	}

	/**
	 * Setter method for authentication.
	 *
	 * @param authentication
	 *            the authentication to set
	 */
	public void setAuthentication(User authentication) {
		this.authentication = authentication;
	}

	/**
	 * Setter method for effectiveAuthentication.
	 *
	 * @param effectiveAuthentication
	 *            the effectiveAuthentication to set
	 */
	public void setEffectiveAuthentication(User effectiveAuthentication) {
		this.effectiveAuthentication = effectiveAuthentication;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EmfSecurityContext clone() {
		EmfSecurityContext context = new EmfSecurityContext();
		context.effectiveAuthentication = effectiveAuthentication;
		context.authentication = authentication;
		return context;
	}

}
