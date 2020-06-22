package com.sirmaenterprise.sep.roles.rest;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Rest endpoint for action management
 *
 * @author BBonev
 */
@Transactional
@Path("/rolemgmt/actions")
@AdminResource
@ApplicationScoped
@Produces({ MediaType.APPLICATION_JSON, Versions.V2_JSON })
public class ActionsResource {
	@Inject
	private RoleManagement roleManagement;
	@Inject
	private LabelProvider labelProvider;
	@Inject
	private UserPreferences userPreferences;

	/**
	 * Get list of all actions in the applications
	 *
	 * @return all actions list
	 */
	@GET
	public List<ActionResponse> getAllActions() {
		return roleManagement
				.getActions()
					.map(ResponseBuilders.buildAction(labelProvider))
					.sorted(actionsByLabel())
					.collect(Collectors.toList());
	}

	private Comparator<? super ActionResponse> actionsByLabel() {
		Collator sorter = Collator.getInstance(new Locale(userPreferences.getLanguage()));
		return (r1, r2) -> sorter.compare(r1.getLabel(), r2.getLabel());
	}
}
