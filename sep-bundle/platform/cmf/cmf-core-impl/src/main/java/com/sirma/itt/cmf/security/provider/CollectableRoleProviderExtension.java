package com.sirma.itt.cmf.security.provider;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.ADMINISTRATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.MANAGER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.COLLABORATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONTRIBUTOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.RoleImpl;

/**
 * Roles are propagated as final step to the higher roles automatically. Currently is done by hand,
 * but the order could be provided by the map or otherway.
 */
@Extension(target = RoleProviderExtension.TARGET_NAME, order = 100)
public class CollectableRoleProviderExtension extends AbstractRoleProviderExtension {
	/**
	 * {@inheritDoc} <br>
	 * <strong> Enrich each higher model from the model on previous level starting from bottom to
	 * the up
	 */
	@Override
	public Map<RoleIdentifier, Role> getModel(Map<RoleIdentifier, Role> chained) {

		merge(chained, CONSUMER, CONTRIBUTOR);
		merge(chained, CONTRIBUTOR, COLLABORATOR);
		merge(chained, COLLABORATOR, MANAGER);
		merge(chained, CREATOR, MANAGER);
		merge(chained, MANAGER, ADMINISTRATOR);
		return chained;
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
	protected void merge(Map<RoleIdentifier, Role> chained, RoleIdentifier roleIdfrom,
			RoleIdentifier roleIdTo) {

		Role roleFrom = chained.get(roleIdfrom);
		if (roleFrom == null) {
			return;
		}
		Role roleTo = chained.get(roleIdTo);
		if (roleTo == null) {
			roleTo = new RoleImpl(roleIdTo, new HashMap<Permission, List<Pair<Class<?>, Action>>>());
			mergerRoles(roleFrom, roleTo);
			chained.put(roleIdTo, roleTo);
		} else {
			mergerRoles(roleFrom, roleTo);
		}
	}

}
