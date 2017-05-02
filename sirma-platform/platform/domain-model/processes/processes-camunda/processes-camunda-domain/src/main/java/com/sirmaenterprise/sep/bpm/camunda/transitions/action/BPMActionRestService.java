package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Defines the BPM action rest service that handles bpmTransition actions
 * 
 * @author bbanchev
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@Singleton
public class BPMActionRestService {

	@Inject
	private Actions actions;

	/**
	 * Executes a transition of activity part of business process.
	 *
	 * @param request
	 *            {@link BPMTransitionRequest} containing the information for the operation, like - user operation,
	 *            transition data, target instance, etc.
	 * @return the collection of newly created instances after the transition
	 */
	@POST
	@Path("/{id}/actions/bpmTransition")
	@SuppressWarnings("unchecked")
	public Collection<Instance> executeTransition(BPMTransitionRequest request) {
		return (Collection<Instance>) actions.callAction(request);
	}

	/**
	 * Executes a start of business process.
	 *
	 * @param request
	 *            {@link BPMStartRequest} containing the information for the operation, like - user operation,
	 *            transition data, target instance, etc.
	 * @return the collection of newly created instances after the transition
	 */
	@POST
	@Path("/{id}/actions/bpmStart")
	@SuppressWarnings("unchecked")
	public Collection<Instance> executeStart(BPMStartRequest request) {
		return (Collection<Instance>) actions.callAction(request);
	}

	/**
	 * Executes a start of business process.
	 *
	 * @param request
	 *            {@link BPMStartRequest} containing the information for the operation, like - user operation,
	 *            transition data, target instance, etc.
	 * @return the collection of newly created instances after the transition
	 */
	@POST
	@Path("/{id}/actions/bpmStop")
	@SuppressWarnings("unchecked")
	public Collection<Instance> executeStop(BPMStopRequest request) {
		return (Collection<Instance>) actions.callAction(request);
	}

	/**
	 * Executes a release of business process task.
	 *
	 * @param request
	 *            {@link BPMReleaseRequest} containing the information for the operation, like - user operation,
	 *            transition data, target instance, etc.
	 * @return the collection of newly created instances after the transition
	 */
	@POST
	@Path("/{id}/actions/bpmRelease")
	@SuppressWarnings("unchecked")
	public Collection<Instance> executeRelease(BPMReleaseRequest request) {
		return (Collection<Instance>) actions.callAction(request);
	}

	/**
	 * Executes a claim of business process task.
	 *
	 * @param request
	 *            {@link BPMClaimRequest} containing the information for the operation, like - user operation,
	 *            transition data, target instance, etc.
	 * @return the collection of newly created instances after the transition
	 */
	@POST
	@Path("/{id}/actions/bpmClaim")
	@SuppressWarnings("unchecked")
	public Collection<Instance> executeClaim(BPMClaimRequest request) {
		return (Collection<Instance>) actions.callAction(request);
	}
}
