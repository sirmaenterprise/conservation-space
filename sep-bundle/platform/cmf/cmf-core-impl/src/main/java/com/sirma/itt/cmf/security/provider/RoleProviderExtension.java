package com.sirma.itt.cmf.security.provider;

import java.util.Map;

import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;

/**
 * The RoleProviderExtension should provide role and permission mapping in sub modules. Extensions'
 * response is to or not to override current chained state. If extension wants only to enrich
 * {@link RoleIdentifier} should work with current state otherwise should reset what is set on the
 * key<br>
 * <strong> Each module should include permission and actions for the lowest level to appear. Roles
 * are propagated as final step to the higher roles automatically</strong>
 */
public interface RoleProviderExtension extends Plugin {

	/** The target name. */
	String TARGET_NAME = "RoleProviderExtension";

	/**
	 * Gets the role to permission and actions mapping. This is module specific logic. Extensions'
	 * response is to or not to override current chained state. If extension wants only to enrich
	 * {@link RoleIdentifier} should work with current holden in chainedRoles
	 *
	 * @param chainedRoles
	 *            provides the current state roles that are updated from lower priority modules
	 * @return the updated state
	 */
	Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> chainedRoles);

}
