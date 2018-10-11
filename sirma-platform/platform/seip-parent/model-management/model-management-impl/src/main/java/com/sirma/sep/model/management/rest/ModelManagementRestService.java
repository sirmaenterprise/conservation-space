package com.sirma.sep.model.management.rest;

import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirma.sep.model.management.ModelManagementService;
import com.sirma.sep.model.management.ModelProperty;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;
import com.sirma.sep.model.management.response.ModelResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import java.util.List;

/**
 * Administration web service for managing the available models in the system.
 *
 * @author Mihail Radkov
 */
@Path("/administration/model-management")
@AdminResource
@Singleton
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
	@Produces(MediaType.APPLICATION_JSON)
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
	@Produces(MediaType.APPLICATION_JSON)
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
	@Produces(MediaType.APPLICATION_JSON)
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
	@Produces(MediaType.APPLICATION_JSON)
	public ModelResponse getModel(@QueryParam("model") String id) {
		return managementService.getModel(id);
	}
}
