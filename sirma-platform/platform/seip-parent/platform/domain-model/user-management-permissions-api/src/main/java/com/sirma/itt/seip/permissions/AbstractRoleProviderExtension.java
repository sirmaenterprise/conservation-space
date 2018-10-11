package com.sirma.itt.seip.permissions;

import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.seip.permissions.action.ActionRegistry;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;

/**
 * Basic implementation for {@link RoleProviderExtension} holding some common code.
 *
 * @author bbanchev
 */
public abstract class AbstractRoleProviderExtension implements RoleProviderExtension, SecurityModel {

	/** The action registry. */
	@Inject
	protected ActionRegistry actionRegistry;

	protected AbstractRoleProviderExtension() {
		// only subclassed
	}

	/**
	 * Merge two roles internal. If the destination is missing is simple put at the end of merging
	 *
	 * @param chained
	 *            the current state permission model
	 * @param roleIdfrom
	 *            the role to get info from
	 * @param roleIdTo
	 *            the role id to add permissions to. if empty it is created
	 */
	protected static void merge(Map<RoleIdentifier, Role> chained, RoleIdentifier roleIdfrom, RoleIdentifier roleIdTo) {
		Role roleFrom = chained.get(roleIdfrom);
		if (roleFrom == null) {
			return;
		}
		Role roleTo = chained.get(roleIdTo);
		if (roleTo == null) {
			roleTo = new Role(roleIdTo, null);
			chained.put(roleIdTo, roleTo);
		}
		Role.mergerRoles(roleFrom, roleTo);
	}

}
