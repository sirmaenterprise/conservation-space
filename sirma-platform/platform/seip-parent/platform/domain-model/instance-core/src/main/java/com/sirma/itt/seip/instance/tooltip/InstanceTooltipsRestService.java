package com.sirma.itt.seip.instance.tooltip;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.rest.utils.Versions;

/**
 * This class represent REST controller that will handle tooltip management.
 *
 * @author cdimitrov
 * @author nvelkov
 */
@Transactional
@Path("/instances")
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class InstanceTooltipsRestService {

	@Inject
	private InstanceTooltipsService tooltipService;

	/**
	 * Retrieves the instance's tooltip.
	 *
	 * @param instanceId the current instance id
	 * @return tooltip for the instance
	 */
	@Path("/{id}/tooltip")
	@GET
	public String getTooltip(@PathParam(KEY_ID) String instanceId) {
		return tooltipService.getTooltip(instanceId);
	}

}