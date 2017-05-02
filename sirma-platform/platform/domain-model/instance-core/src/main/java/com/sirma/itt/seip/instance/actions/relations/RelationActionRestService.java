package com.sirma.itt.seip.instance.actions.relations;

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
 * Rest service that provides relation action over instances. Supported actions are:
 * <ul>
 * <li>addRelation
 * <li>removeRelation
 * </ul>
 *
 * @author BBonev
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class RelationActionRestService {

	@Inject
	private Actions actions;

	/**
	 * Executes operations that adds a relation between the current instance and the specified instance ids.
	 * <p>
	 * Typical request is
	 *
	 * <pre>
	 * <code>{
	 *   "userOperation" : "userOperation",
	 *   "removeExisting" : true/false
	 *   "relations" : {
	 *      "emf:addParent" : ["emf:instance1", "emf:instance2"]
	 *   }
	 * }</code>
	 * </pre>
	 *
	 * @param request
	 *            {@link AddRelationRequest} containing the information for the operation, like - user operation,
	 *            context path, placeholder, etc.
	 * @return the updated instance after the action is executed
	 */
	@POST
	@Path("/{id}/actions/addRelation")
	public Instance addRelation(AddRelationRequest request) {
		return (Instance) actions.callAction(request);
	}

	/**
	 * Executes operations that removes a relation between the current instance and the specified instance ids.
	 * <p>
	 * Typical request is
	 *
	 * <pre>
	 * <code>{
	 *   "userOperation" : "userOperation",
	 *   "relations" : {
	 *      "emf:addParent" : ["emf:instance1", "emf:instance2"]
	 *   }
	 * }</code>
	 * </pre>
	 *
	 * @param request
	 *            {@link RemoveRelationRequest} containing the information for the operation, like - user operation,
	 *            context path, placeholder, etc.
	 * @return the updated instance after the action is executed
	 */
	@POST
	@Path("/{id}/actions/removeRelation")
	public Instance removeRelation(RemoveRelationRequest request) {
		return (Instance) actions.callAction(request);
	}
}
