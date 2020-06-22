package com.sirma.itt.seip.instance.lock.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.action.LockRequest;
import com.sirma.itt.seip.instance.lock.action.UnlockRequest;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Contains rest services for instance locking/unlocking.
 *
 * @author A. Kunchev
 */
@Path("/instances")
@ApplicationScoped
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
public class InstanceLockRestService {

	@Inject
	private Actions actions;

	/**
	 * Executes lock action for given instance.
	 *
	 * @param request
	 *            {@link LockRequest} object, contains request parameters needed for the operation
	 * @return {@link LockInfo} for the instance
	 */
	@POST
	@Path("/{id}/actions/lock")
	public LockInfo lock(LockRequest request) {
		return (LockInfo) actions.callAction(request);
	}

	/**
	 * Executes unlock action for given instance.
	 *
	 * @param id
	 *            the id of the instance that should be unlocked
	 * @return {@link LockInfo} for the instance
	 */
	@POST
	@Path("/{id}/actions/unlock")
	public LockInfo unlock(@PathParam(RequestParams.KEY_ID) String id) {
		UnlockRequest request = new UnlockRequest();
		request.setTargetId(id);
		return (LockInfo) actions.callAction(request);
	}

}
