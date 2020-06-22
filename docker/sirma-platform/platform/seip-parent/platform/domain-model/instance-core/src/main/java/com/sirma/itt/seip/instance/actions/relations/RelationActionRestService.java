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
 * <li>addRelation</li>
 * <li>removeRelation</li>
 * <li>updateRelations</li>
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
	 * @param request {@link AddRelationRequest} containing the information for the operation, like - user operation,
	 *        context path, placeholder, etc.
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
	 * @param request {@link RemoveRelationRequest} containing the information for the operation, like - user operation,
	 *        context path, placeholder, etc.
	 * @return the updated instance after the action is executed
	 */
	@POST
	@Path("/{id}/actions/removeRelation")
	public Instance removeRelation(RemoveRelationRequest request) {
		return (Instance) actions.callAction(request);
	}

	/**
	 * Executes operation that update (add/remove) relations between the current instance and the specified instance
	 * ids.
	 * <p>
	 * Typical request is:
	 *
	 * <pre>
	 *         <code>
	 *             {
	 *               "id": "emf:0001",
	 *               "add": [{
	 *                        "linkId": "emf:hasAttachment",
	 *                        "ids": ["emf:0002", "emf:0003"]
	 *                       }, {
	 *                         "linkId": "emf:hasWatchers",
	 *                         "ids": ["emf:0002", "emf:0003"]
	 *                      }],
	 *                "remove": [{
	 *                            "linkId": "emf:hasAttachment",
	 *                            "ids": ["emf:0004", "emf:0005"]
	 *                           }, {
	 *                            "linkId": "emf:hasWatchers",
	 *                            "ids": ["emf:0004", "emf:0005"]
	 *                          }]
	 *              }
	 *         </code>
	 * </pre>
	 * </p>
	 *
	 * @param request {@link UpdateRelationsRequest} containing the information for the operation, like - user
	 *        operation, context path, placeholder, etc.
	 * @return the updated instance after the action is executed
	 */
	@POST
	@Path("/actions/updateRelations")
	public Instance updateRelations(UpdateRelationsRequest request) {
		return (Instance) actions.callAction(request);
	}
}
