package com.sirmaenterprise.sep.bpm.camunda.rest;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.camunda.bpm.engine.ProcessEngine;

import com.sirma.itt.seip.rest.utils.Versions;
import com.sirmaenterprise.sep.bpm.camunda.service.SecureProcessEngine;

/**
 * This class build specific URL to camunda resources that are needed by the UI.
 * 
 * 
 * @author simeon
 */
@Path("/camunda/api")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@Singleton
public class CamundaUrlRestBuilder {

	@Inject
	private ProcessEngine processEngine;

	/**
	 * Constructs the remote engine base url for executing camunda requests.
	 * 
	 * @return the remote engine name.
	 */
	@Path("/engine")
	@GET
	@SecureProcessEngine(notInitializedAccepted = false)
	public String generateEngineBaseURI() {
		StringBuilder builder = new StringBuilder();
		builder.append("/engine/engine/").append(processEngine.getName());
		return builder.toString();
	}

}
