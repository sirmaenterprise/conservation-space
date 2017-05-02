package com.sirma.itt.seip.permissions;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.NO_PERMISSION;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.omg.CORBA.NO_PERMISSION;

import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProvider;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Default implementation of {@link RoleProvider} for CMF to initialize the allowed actions and permissions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefaultRoleProvider implements RoleProvider {

	@Inject
	@ExtensionPoint(value = RoleProviderExtension.TARGET_NAME)
	private Iterable<RoleProviderExtension> providers;

	@Override
	public Map<RoleIdentifier, Role> provide() {
		return getData();
	}

	/**
	 * Gets the role data.
	 *
	 * @return the data
	 */
	protected Map<RoleIdentifier, Role> getData() {
		Map<RoleIdentifier, Role> lastState = new HashMap<>();
		for (RoleProviderExtension roleProvider : providers) {
			// the providers should only enrich the mapping
			roleProvider.getModel(lastState);
		}
		setNoPermissionRole(lastState);
		return lastState;
	}

	/**
	 * Sets {@link NO_PERMISSION} role, if it isn't set by provider.
	 */
	private static void setNoPermissionRole(Map<RoleIdentifier, Role> rolesMap) {
		rolesMap.putIfAbsent(NO_PERMISSION, new Role(NO_PERMISSION));
	}

}
