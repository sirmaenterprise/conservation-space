package com.sirma.sep.model.management.rest;

import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirma.sep.model.management.ModelManagementService;
import com.sirma.sep.model.management.ModelProperty;
import com.sirma.sep.model.management.DeploymentValidationReport;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.request.ModelUpdateRequest;
import com.sirma.sep.model.management.request.ModelDeploymentRequest;
import com.sirma.sep.model.management.response.ModelResponse;
import com.sirma.sep.model.management.response.ModelUpdateResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;

/**
 * Administration web service for managing the available models in the system.
 *
 * @author Mihail Radkov
 */
@Path("/administration/model-management")
@AdminResource
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelManagementRestService {

	@Inject
	private ModelManagementService managementService;

	/**
	 * Retrieves the models hierarchy between semantic class models and definition models.
	 *
	 * @return the models hierarchy
	 */
	@GET
	@Path("/hierarchy")
	public List<ModelHierarchyClass> getHierarchy() {
		return managementService.getModelHierarchy();
	}

	/**
	 * Retrieves the available models meta information.
	 *
	 * @return the meta information for the available models
	 * @see ModelsMetaInfo
	 * @see com.sirma.sep.model.management.meta.ModelMetaInfo
	 */
	@GET
	@Path("/meta-info")
	public ModelsMetaInfo getMetaInfo() {
		return managementService.getMetaInfo();
	}

	/**
	 * Retrieves the whole set of {@link ModelProperty} related to all available semantic and definition models.
	 *
	 * @return list of all {@link ModelProperty}
	 */
	@GET
	@Path("/properties")
	public List<ModelProperty> getProperties() {
		return managementService.getProperties();
	}

	/**
	 * Selects the model hierarchy for the provided identifier.
	 *
	 * @param id the model identifier for models selection. May be semantic or definition one.
	 * @return model response corresponding to the select identifier. If for the identifier no models are available then the response will
	 * be empty.
	 */
	@GET
	public ModelResponse getModel(@QueryParam("model") String id) {
		return managementService.getModel(id);
	}

	/**
	 * Updates the model by applying the given changes
	 *
	 * @param updateRequest the update request to process
	 * @return a response containing the model nodes
	 */
	@POST
	public ModelUpdateResponse updateModel(ModelUpdateRequest updateRequest) {
		return managementService.updateModel(updateRequest);
	}

	/**
	 * Run validation for deployment of all non deployed model nodes
	 *
	 * @return the list of non deployed models and any validation information if applicable
	 */
	@GET
	@Path("/deploy")
	public DeploymentValidationReport runDeploymentValidation() {
		return managementService.validateDeploymentCandidates();
	}

	/**
	 * Trigger deploy process of the given non deployed models. Can use the response from the GET /deploy call
	 *
	 * @param deploymentRequest the requested models to deploy
	 * @return the accepted status or error
	 */
	@POST
	@Path("/deploy")
	public Response deploy(ModelDeploymentRequest deploymentRequest) {
		DeploymentValidationReport report = managementService.deployChanges(deploymentRequest);

		Response.Status responseStatus = Response.Status.ACCEPTED;
		if (!report.isValid()) {
			responseStatus = Response.Status.BAD_REQUEST;
		}
		return Response.status(responseStatus).entity(report).build();
	}
}
