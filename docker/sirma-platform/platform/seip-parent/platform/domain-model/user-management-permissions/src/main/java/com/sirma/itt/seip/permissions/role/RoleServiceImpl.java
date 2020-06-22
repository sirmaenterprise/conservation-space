package com.sirma.itt.seip.permissions.role;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.MANAGER;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.VIEWER;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;

/**
 * The RoleServiceImpl provides access and management of roles
 */
@ApplicationScoped
public class RoleServiceImpl implements Serializable, RoleService {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6634701858563556551L;

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Override
	public List<RoleIdentifier> getActiveRoles() {
		return SecurityModel.BaseRoles.PUBLIC;
	}

	@Override
	public RoleIdentifier getRoleIdentifier(String rolename) {
		if (rolename == null) {
			return null;
		}
		// TODO as role mapper extension
		if ("manager".equals(rolename)) {
			return MANAGER;
		} else if ("none".equals(rolename)) {
			return VIEWER;
		}
		return SecurityModel.BaseRoles.getIdentifier(rolename);
	}

	@Override
	public RoleIdentifier getManagerRole() {
		return getRoleIdentifier(securityConfiguration.getManagerRole().computeIfNotSet(() -> "manager"));
	}
}
