package com.sirmaenterprise.sep.roles.rest;

import java.util.List;
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
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Rest endpoint for permission role management
 *
 * @author BBonev
 */
@Transactional
@Path("/rolemgmt/roles")
@AdminResource
@ApplicationScoped
@Produces({ MediaType.APPLICATION_JSON, Versions.V2_JSON })
public class RoleResource {

	@Inject
	private RoleManagement roleManagement;

	@Inject
	private LabelProvider labelProvider;

	/**
	 * Get all non internal roles
	 *
	 * @return list of all roles
	 */
	@GET
	public List<RoleResponse> getRoles() {
		return roleManagement
				.getRoles()
					.filter(role -> !role.isInternal())
					.sorted() // by role priority
					.map(ResponseBuilders.buildRole(labelProvider))
					.collect(Collectors.toList());
	}
}
