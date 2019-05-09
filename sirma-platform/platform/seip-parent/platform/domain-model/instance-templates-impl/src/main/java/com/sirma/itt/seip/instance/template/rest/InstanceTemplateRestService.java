package com.sirma.itt.seip.instance.template.rest;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.sep.instance.template.InstanceTemplateService;

/**
 * Provides rest endpoints for manipulation of the templates of the instances.
 *
 * @author Adrian Mitev
 */
/**
 * TODO should be implemented as {@link Action}, because there is additional functionality executed there, like instance
 * locking during action execution, etc.
 */
@Transactional
@Produces(Versions.V2_JSON)
@Path("instances")
public class InstanceTemplateRestService {

	@Inject
	private InstanceTemplateService instanceTemplateService;

	/**
	 * Starts the process of instance template update.
	 *
	 * @param request
	 *            contains the information about which instances to update.
	 */
	@POST
	@Consumes(Versions.V2_JSON)
	@Path("actions/update-instance-template")
	public void initiateInstanceTemplateUpdateOperation(InstanceTemplateUpdateRequest request) {
		instanceTemplateService.updateInstanceViews(request.getTemplateInstance());
	}

	/**
	 * Single instance template update rest point.
	 *
	 * @param request
	 *            contains the information about the instance to update
	 * @return updated instance
	 */
	@POST
	@Consumes(Versions.V2_JSON)
	@Path("actions/update-to-latest-template")
	public Instance initiateSingleInstanceTemplateUpdateOperation(InstanceTemplateUpdateRequest request) {
		return instanceTemplateService.updateInstanceView(request.getInstance());
	}

	/**
	 * Instance's template actual version getter rest point.
	 *
	 * @param request
	 *            contains the information about the instance
	 * @return instance's template actual version
	 */
	@POST
	@Consumes(Versions.V2_JSON)
	@Path("template-version")
	public String getInstanceTemplateVersion(InstanceTemplateUpdateRequest request) {
		return instanceTemplateService.getInstanceTemplateVersion(request.getInstance());
	}

}
