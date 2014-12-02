package com.sirma.itt.cmf.security.provider;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.RoleProvider;

/**
 * Default implementation of {@link RoleProvider} for CMF to initialize the allowed actions and
 * permissions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class CmfRoleProvider implements RoleProvider {

	@Inject
	@ExtensionPoint(value = RoleProviderExtension.TARGET_NAME)
	private Iterable<RoleProviderExtension> collection;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<RoleIdentifier, Role> provide() {
		return getData();
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	protected Map<RoleIdentifier, Role> getData() {
		Map<RoleIdentifier, Role> lastState = new HashMap<RoleIdentifier, Role>();
		for (RoleProviderExtension roleProvider : collection) {
			// the providers should only enrich the mapping
			roleProvider.getModel(lastState);
		}
		return lastState;
	}

}
