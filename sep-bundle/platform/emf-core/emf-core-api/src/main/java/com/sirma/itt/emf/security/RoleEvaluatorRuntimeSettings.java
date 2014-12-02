package com.sirma.itt.emf.security;

import java.util.Collection;

import com.sirma.itt.emf.security.model.RoleIdentifier;

/**
 * The RoleEvaluatorRuntimeSettings is wrapper for settings used in role evaluators to provide fine
 * grained evaluation.
 */
public class RoleEvaluatorRuntimeSettings {

	/** The skipped roles. */
	private Collection<RoleIdentifier> irrelevantRoles;

	/**
	 * Gets the irrelevant roles.
	 *
	 * @return the irrelevant roles
	 */
	public Collection<RoleIdentifier> getIrrelevantRoles() {
		return irrelevantRoles;
	}

	/**
	 * Sets the irrelevant roles.
	 *
	 * @param irrelevantRoles
	 *            the new irrelevant roles
	 */
	public void setIrrelevantRoles(Collection<RoleIdentifier> irrelevantRoles) {
		this.irrelevantRoles = irrelevantRoles;
	}

}
