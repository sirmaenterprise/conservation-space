package com.sirmaenterprise.sep.bpm.camunda.rest;

import static com.sirmaenterprise.sep.bpm.camunda.util.BPMInstanceUtil.resolveInstance;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirmaenterprise.sep.bpm.camunda.service.ActivityDetails;
import com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService;

/**
 * Generic rest service providing details for activity instances.
 * 
 * @author bbanchev
 * @author simeon iliev
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@Singleton
public class BPMInfoRestService {

	@Inject
	private CamundaBPMService bpmService;
	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	/**
	 * Returns the activity details information for given instance. If the instance is not a bpm activity null is
	 * returned.
	 * 
	 * @param instanceId
	 *            the instance id to get activity details for
	 * @return the activity details bean for given instance
	 */
	@GET
	@Transactional
	@Path("/{id}/bpm/activity")
	public ActivityDetails getDetails(@PathParam("id") String instanceId) {
		return bpmService.getActivityDetails(resolveInstance(instanceId, instanceTypeResolver)).orElse(null);
	}

}
