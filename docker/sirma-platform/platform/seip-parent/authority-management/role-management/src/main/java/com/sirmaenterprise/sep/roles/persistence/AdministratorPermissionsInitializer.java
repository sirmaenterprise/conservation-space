package com.sirmaenterprise.sep.roles.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleActionChanges;
import com.sirmaenterprise.sep.roles.RoleActionModel;
import com.sirmaenterprise.sep.roles.RoleActionModel.RoleActionMapping;
import com.sirmaenterprise.sep.roles.RoleManagement;
import com.sirmaenterprise.sep.roles.events.ActionDefinitionsChangedEvent;

/**
 * Responsible for initializing administrator permissions, since the permissions xmls are no longer supported.
 *
 * @author smustafov
 */
public class AdministratorPermissionsInitializer {

	@Inject
	private RoleManagement roleManagement;

	/**
	 * Listens for action definitions change and assigns all the actions to the administrator role.
	 *
	 * @param event
	 *            event fired after actions definition are changed
	 */
	public void actionsChanged(@Observes ActionDefinitionsChangedEvent event) {
		RoleActionChanges roleActionChanges = new RoleActionChanges();
		RoleActionModel model = roleManagement.getRoleActionModel();

		List<RoleActionMapping> mappings = model
				.getActionsForRole(SecurityModel.BaseRoles.ADMINISTRATOR.getIdentifier())
				.collect(Collectors.toList());

		model.actions()
				.forEach(action -> roleActionChanges.enable(SecurityModel.BaseRoles.ADMINISTRATOR.getIdentifier(),
						action.getId(), getFilters(mappings, action)));

		roleManagement.updateRoleActionMappings(roleActionChanges);
	}

	private static Collection<String> getFilters(List<RoleActionMapping> mappings, ActionDefinition action) {
		Optional<RoleActionMapping> existingMapping = mappings.stream()
				.filter(mapping -> mapping.getAction().getId().equals(action.getId())).findAny();
		return existingMapping.map(RoleActionMapping::getFilters).orElse(Collections.emptyList());
	}

}
