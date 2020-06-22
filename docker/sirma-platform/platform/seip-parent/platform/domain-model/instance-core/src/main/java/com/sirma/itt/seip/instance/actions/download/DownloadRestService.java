package com.sirma.itt.seip.instance.actions.download;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Executes the download action for instance.
 *
 * @author A. Kunchev
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class DownloadRestService {

	@Inject
	private Actions actions;

	/**
	 * Returns the link with which the content of the given instance could be retrieved. This service is done so we
	 * could extended the action execution with additional specific logic, if needed, without changing the logic for
	 * content retrieving.
	 *
	 * @param request
	 *            {@link DownloadRequest} object which contains the information needed to execute the action
	 * @return link with which the content of the passed instance can be download
	 */
	@POST
	@Path("/{id}/actions/download")
	public String getDownloadLink(DownloadRequest request) {
		return (String) actions.callAction(request);
	}

}
