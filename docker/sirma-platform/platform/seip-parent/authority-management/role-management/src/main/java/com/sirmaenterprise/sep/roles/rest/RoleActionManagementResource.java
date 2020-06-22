package com.sirmaenterprise.sep.roles.rest;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.permissions.action.RoleActionFilterService;
import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirmaenterprise.sep.roles.RoleActionChanges;
import com.sirmaenterprise.sep.roles.RoleActionModel;
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Endpoint for role action management
 *
 * @author BBonev
 */
@Transactional
@AdminResource
@Path("/rolemgmt")
@ApplicationScoped
@Produces({ MediaType.APPLICATION_JSON, Versions.V2_JSON })
public class RoleActionManagementResource {

	@Inject
	private RoleActionFilterService filterService;

	@Inject
	private RoleManagement roleManagement;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private UserPreferences userPreferences;

	/**
	 * Provide action mappings for all roles
	 *
	 * @return the role actions mapping
	 */
	@GET
	@Path("/roleActions")
	public RoleActionsResponse getAllRoleActions() {
		RoleActionModel model = roleManagement.getRoleActionModel();
		RoleActionsResponse response = new RoleActionsResponse();

		// collect mapping for non internal roles (like administrator, possible assignee etc)
		// these roles should not be managed be the users (at least for now)
		model
				.roleActions()
					.filter(entry -> !entry.getRole().isInternal())
					.map(ResponseBuilders::buildRoleActions)
					.forEach(response::add);

		// get the referenced action definitions sorted alphabetically by labels
		model
				.actions()
					.filter(action -> action.isEnabled())
					.map(ResponseBuilders.buildAction(labelProvider))
					.sorted(sortByLabel())
					.forEach(response::add);

		// get referenced role definitions sorted by priority
		model
				.roles()
					.filter(role -> !role.isInternal())
					.sorted() // by role priority
					.map(ResponseBuilders.buildRole(labelProvider))
					.forEach(response::add);
		return response;
	}

	private Comparator<? super ActionResponse> sortByLabel() {
		Collator collator = Collator.getInstance(new Locale(userPreferences.getLanguage()));
		// here the labels are already resolved and fixed
		return (a1, a2) -> collator.compare(a1.getLabel(), a2.getLabel());
	}

	/**
	 * Saves changes to role action assignments
	 *
	 * @param roleActions
	 *            the list of changes to add to the role action assignments
	 * @return the role action assignments after the changes
	 */
	@POST
	@Path("/roleActions")
	public RoleActionsResponse saveRoleActionAssigments(List<RoleAction> roleActions) {
		if (isEmpty(roleActions)) {
			return getAllRoleActions();
		}
		RoleActionChanges changes = new RoleActionChanges();
		for (RoleAction roleAction : roleActions) {
			if (roleAction.isEnabled()) {
				changes.enable(roleAction.getRole(), roleAction.getAction(), roleAction.getFilters());
			} else {
				changes.disable(roleAction.getRole(), roleAction.getAction(), roleAction.getFilters());
			}
		}

		roleManagement.updateRoleActionMappings(changes);

		return getAllRoleActions();
	}

	/**
	 * Get all available role filters
	 *
	 * @return role filters
	 */
	@GET
	@Path("/filters")
	public List<String> getFilers() {
		List<String> filters = new ArrayList<>(filterService.getFilters());
		Collections.sort(filters, String.CASE_INSENSITIVE_ORDER);
		return filters;
	}
}
