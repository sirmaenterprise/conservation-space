package com.sirma.itt.seip.instance.actions.save;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.annotations.http.method.PATCH;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest service that provides create or update operations over instances.
 *
 * @author nvelkov
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class SaveRestService {

	@Inject
	private Actions actions;

	/**
	 * Executes operations that creates or uploads an instance.
	 * <p>
	 * Typical request is
	 *
	 * <pre>
	 * <code>{
	 *   "userOperation" : "userOperation",
	 *   "targetInstance" : {
	 *   	properties : {
	 *         "mimetype" : "png"
	 *      }
	 *   }
	 * }</code>
	 * </pre>
	 *
	 * @deprecated user {@link #save(SaveRequest)} which is with different end point -> @Path("/{id}/actions/save")
	 *             [deprecated v2.20.0]
	 * @param request {@link SaveRequest} containing the information for the operation, like - user operation, context
	 *        path, placeholder, etc.
	 * @return the updated instance after the action is executed
	 */
	@PATCH
	@Path("/{id}/actions/createOrUpdate")
	@Deprecated
	public Instance saveOld(SaveRequest request) {
		return save(request);
	}

	/**
	 * Executes operations that creates or uploads an instance.
	 * <p>
	 * Typical request is
	 *
	 * <pre>
	 * <code>{
	 *   "userOperation" : "userOperation",
	 *   "targetInstance" : {
	 *   	properties : {
	 *         "mimetype" : "png"
	 *      }
	 *   }
	 * }</code>
	 * </pre>
	 *
	 * @param request {@link SaveRequest} containing the information for the operation, like - user operation, context
	 *        path, placeholder, etc.
	 * @return the updated instance after the action is executed
	 */
	@PATCH
	@Path("/{id}/actions/save")
	public Instance save(SaveRequest request) {
		return (Instance) actions.callAction(request);
	}
}