package com.sirma.itt.pm.security.provider;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.ADMINISTRATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.COLLABORATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONTRIBUTOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.MANAGER;
import static com.sirma.itt.pm.security.PmSecurityModel.PmRoles.PROJECT_MANAGER;

import java.util.Map;

import com.sirma.itt.cmf.security.provider.CollectableRoleProviderExtension;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;

/**
 * Pm extension of {@link CollectableRoleProviderExtension} to support pm roles as well
 *
 * @author bbanchev
 */
@Extension(target = com.sirma.itt.cmf.security.provider.RoleProviderExtension.TARGET_NAME, order = 100, priority = 1)
public class PmCollectableRoleProviderExtension extends CollectableRoleProviderExtension {

	@Override
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> chained) {
		merge(chained, CONSUMER, CONTRIBUTOR);
		merge(chained, CONTRIBUTOR, COLLABORATOR);
		merge(chained, COLLABORATOR, MANAGER);
		merge(chained, CREATOR, MANAGER);
		merge(chained, MANAGER, PROJECT_MANAGER);
		merge(chained, PROJECT_MANAGER, ADMINISTRATOR);
		return chained;
	}

}
