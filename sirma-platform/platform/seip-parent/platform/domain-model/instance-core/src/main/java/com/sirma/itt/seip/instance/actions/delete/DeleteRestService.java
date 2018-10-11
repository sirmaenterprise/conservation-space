package com.sirma.itt.seip.instance.actions.delete;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.delete.DeleteRequest;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Exposes the delete instance action as http endpoint.
 *
 * @author Adrian Mitev
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class DeleteRestService {

	@Inject
	private Actions actions;

	/**
	 * Calls the Actions service to execute the delete action.
	 *
	 * @param request
	 *            {@link DeleteRequest} object which contains the information needed to execute the action
	 * @return ids of the deleted instances
	 */
	@POST
	@Path("/{id}/actions/delete")
	@SuppressWarnings("unchecked")
	public Collection<String> delete(DeleteRequest request) {
		return (Collection<String>) actions.callAction(request);
	}
}