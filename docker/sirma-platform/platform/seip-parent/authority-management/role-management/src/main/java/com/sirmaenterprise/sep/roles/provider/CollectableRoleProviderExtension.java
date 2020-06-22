package com.sirmaenterprise.sep.roles.provider;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.ADMINISTRATOR;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.COLLABORATOR;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CONTRIBUTOR;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.MANAGER;

import java.util.Map;

import com.sirma.itt.seip.permissions.AbstractRoleProviderExtension;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Roles are propagated as final step to the higher roles automatically. Currently is done by hand, but the order could
 * be provided by the map or otherway.
 */
@Extension(target = RoleProviderExtension.TARGET_NAME, order = 100)
public class CollectableRoleProviderExtension extends AbstractRoleProviderExtension {

	/**
	 * {@inheritDoc} <br>
	 * <strong> Enrich each higher model from the model on previous level starting from bottom to the up
	 */
	@Override
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> chained) {
		merge(chained, CONSUMER, CONTRIBUTOR);
		merge(chained, CONTRIBUTOR, COLLABORATOR);
		merge(chained, COLLABORATOR, MANAGER);
		merge(chained, CONTRIBUTOR, CREATOR);
		merge(chained, CREATOR, MANAGER);
		merge(chained, MANAGER, ADMINISTRATOR);
		return chained;
	}
}