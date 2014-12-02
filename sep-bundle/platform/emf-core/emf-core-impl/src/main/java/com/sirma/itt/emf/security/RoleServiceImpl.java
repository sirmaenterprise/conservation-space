package com.sirma.itt.emf.security;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.MANAGER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.VIEWER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.security.SecurityModel.BaseRoles;
import com.sirma.itt.emf.security.model.RoleIdentifier;

/**
 * The RoleServiceImpl provides access and management of roles
 */
@ApplicationScoped
public class RoleServiceImpl implements Serializable, RoleService {

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
			BaseRoles[] values = SecurityModel.BaseRoles.values();
			ArrayList<RoleIdentifier> rolesInSystemTemp = new ArrayList<RoleIdentifier>(
					values.length);
			for (BaseRoles emfRole : values) {
				rolesInSystemTemp.add(emfRole);
			}
			rolesInSystem = Collections.unmodifiableList(rolesInSystemTemp);
		}
		return rolesInSystem;
	}

	@Override
	public RoleIdentifier getRoleIdentifier(String rolename) {
		if (rolename == null) {
			return null;
		}
		// TODO as role mapper extension
		if (rolename.equals("manager")) {
			return MANAGER;
		} else if (rolename.equals("none")) {
			return VIEWER;
		}
		return BaseRoles.getIdentifier(rolename);
	}
}
