package com.sirma.itt.seip.content.actions.icons;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest service for class icons upload.
 *
 * @author Nikolay Ch
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@ApplicationScoped
public class AddIconsRestService {

	@Inject
	private Actions actions;

	/**
	 * Execute the add icons action.
	 * Receives the icons for the given instance :
	 * 	{
	 *  	"icons": [
	 *  		{"size": 16, "image": "16x16imageinbase64format" },
	 *  		{"size": 84, "image": "64x64imageinbase64format" }
	 *  	]
	 *  }
	 *
	 * @param request
	 *            {@link AddIconsRequest} containing the information for the move operation
	 * @return the response
	 */
	@POST
	@Path("/{id}/actions/addicons")
	public Response uploadIcons(AddIconsRequest request) {
		actions.callAction(request);
		return Response.ok().build();
	}
}
