package com.sirma.itt.seip.instance.actions.publish;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest end point to handle instance publish action requests
 *
 * @author BBonev
 */
@Path("instances")
@ApplicationScoped
@Produces(Versions.V2_JSON)
@Consumes(Versions.V2_JSON)
public class PublishRestService {

	@Inject
	private Actions actions;

	/**
	 * Action request for generic publish action. The action handles publish of idoc and uploaded content.
	 *
	 * @param request
	 *            the publish request
	 * @return the published instance
	 */
	@POST
	@Path("{id}/actions/publish")
	public Instance publish(PublishActionRequest request) {
		return (Instance) actions.callAction(request);
	}

	/**
	 * Action request for publish as PDF action. The action handles publish of idoc and uploaded content by
	 * exporting/converting data to PDF.
	 *
	 * @param request
	 *            the publish request
	 * @return the published instance
	 */
	@POST
	@Path("{id}/actions/publishAsPdf")
	public Instance publishAsPdf(PublishAsPdfActionRequest request) {
		return (Instance) actions.callAction(request);
	}
}
