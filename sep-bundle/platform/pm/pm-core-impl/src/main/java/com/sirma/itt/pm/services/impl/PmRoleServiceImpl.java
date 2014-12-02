package com.sirma.itt.pm.services.impl;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.COLLABORATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONTRIBUTOR;
import static com.sirma.itt.pm.security.PmSecurityModel.PmRoles.PROJECT_MANAGER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;

import com.sirma.itt.emf.security.RoleServiceImpl;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.pm.security.PmSecurityModel.PmRoles;

/**
 * The RoleServiceImpl provides access and management of roles. Pm custom implementation
 */
@ApplicationScoped
@Specializes
public class PmRoleServiceImpl extends RoleServiceImpl {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6634701858563556551L;

	/** The roles in system. */
	private List<RoleIdentifier> rolesInSystem;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RoleIdentifier> getActiveRoles() {
		if (rolesInSystem == null) {
			List<RoleIdentifier> roles = new ArrayList<>(4);
			roles.add(CONSUMER);
			roles.add(CONTRIBUTOR);
			roles.add(COLLABORATOR);
			roles.add(PROJECT_MANAGER);
			rolesInSystem = Collections.unmodifiableList(roles);
		}
		return rolesInSystem;
	}

	@Override
	public RoleIdentifier getRoleIdentifier(String rolename) {
		RoleIdentifier roleIdentifier = super.getRoleIdentifier(rolename);
		if (roleIdentifier == null) {
			return PmRoles.getIdentifier(rolename);
		}
		return roleIdentifier;
	}
}
