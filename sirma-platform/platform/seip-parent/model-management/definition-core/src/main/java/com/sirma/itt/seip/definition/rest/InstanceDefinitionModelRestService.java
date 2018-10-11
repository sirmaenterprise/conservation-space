package com.sirma.itt.seip.definition.rest;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.EDIT_DETAILS;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_OPERATION;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.rest.writers.DefinitionModelBodyWriter;
import com.sirma.itt.seip.definition.rest.writers.DefinitionModelMapBodyWriter;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Used to retrieve {@link DefinitionModel} for instances. The definitions are extracted from the instance, so the
 * revision of the definition is correct.
 *
 * @author A. Kunchev
 */
@Transactional
@Path("/instances")
@Produces(Versions.V2_JSON)
@Consumes(Versions.V2_JSON)
@ApplicationScoped
public class InstanceDefinitionModelRestService {

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private DefinitionService definitionService;

	/**
	 * Retrieves {@link DefinitionModel} for passed instance id. The instance is first resolved and then used to extract
	 * the correct definition for it.
	 *
	 * @param id
	 *            the id of the instance which definition model will be returned
	 * @param operation
	 *            the operation that is executed. Used to evaluate any mandatory fields for the transition. Default
	 *            value for this is "editDetails"
	 * @param requestedFields
	 *            the names of the requested properties. For those properties also will be calculated and returned
	 *            dependent properties
	 * @return {@link DefinitionModelObject} used to convert the {@link DefinitionModel} to JSON
	 * @see DefinitionModelBodyWriter
	 */
	@GET
	@Path("/{id}/model")
	public DefinitionModelObject getInstanceDefinitionModel(@PathParam(KEY_ID) String id,
			@DefaultValue(EDIT_DETAILS) @QueryParam(KEY_OPERATION) String operation,
			@QueryParam(RequestParams.KEY_PROPERTIES) List<String> requestedFields) {
		Instance instance = instanceTypeResolver.resolveReference(id).map(InstanceReference::toInstance).orElseThrow(
				() -> new InstanceNotFoundException(id));
		DefinitionModel model = definitionService.getInstanceDefinition(instance);
		return new DefinitionModelObject()
				.setOperation(operation)
					.setInstance(instance)
					.setDefinitionModel(model)
					.setRequestedFields(requestedFields);
	}

	/**
	 * Retrieves {@link DefinitionModel}s for the passed instance ids. First the instances for the request are resolved
	 * and then used to build map with {@link DefinitionModelObject}. For the key for the map are used the instance ids
	 * of the resolved instances and for the value are build {@link DefinitionModelObject} from the instances. For every
	 * instance is retrieved the {@link DefinitionModel} from the instance, that way we guarantee that we get the
	 * correct definition.
	 *
	 * @param instanceIds
	 *            the id of the instances which models should be loaded
	 * @param requestedFields
	 *            the names of the requested properties. For those properties also will be calculated and returned
	 *            dependent properties
	 * @param operation
	 *            the operation that is executed. Used to evaluate any mandatory fields for the transition. Default
	 *            value for this is "editDetails"
	 * @return map with instance ids and {@link DefinitionModelObject} used to convert to JSON
	 * @see DefinitionModelMapBodyWriter
	 */
	@GET
	@Path("/model")
	public Map<String, DefinitionModelObject> getInstancesDefinitionModels(@QueryParam(KEY_ID) List<String> instanceIds,
			@QueryParam("requestedFields") List<String> requestedFields,
			@DefaultValue(EDIT_DETAILS) @QueryParam(KEY_OPERATION) String operation) {
		LoadModelsRequest request = new LoadModelsRequest()
				.setInstanceIds(instanceIds)
					.setRequestedFields(requestedFields)
					.setOperation(operation);
		return getInstancesDefinitionModelsInternal(request);
	}

	/**
	 * Retrieves {@link DefinitionModel}s for the passed instance ids.
	 *
	 * @param request
	 *            contains required data for instance models loading
	 * @return map with instance ids and {@link DefinitionModelObject} used to convert to JSON
	 */
	@POST
	@Path("/model/batch")
	public Map<String, DefinitionModelObject> getInstancesDefinitionModelsBatch(LoadModelsRequest request) {
		return getInstancesDefinitionModelsInternal(request);
	}

	private Map<String, DefinitionModelObject> getInstancesDefinitionModelsInternal(LoadModelsRequest request) {
		Collection<String> ids = request.getInstanceIds();
		if (ids.isEmpty()) {
			throw new BadRequestException("There are no instance ids in the request or the request key is wrong.");
		}

		Collection<Instance> instances = instanceTypeResolver.resolveInstances(ids);
		Map<String, DefinitionModelObject> result = CollectionUtils.createHashMap(instances.size());

		for (Instance instance : instances) {
			DefinitionModel model = definitionService.getInstanceDefinition(instance);
			if (model == null) {
				continue;
			}

			DefinitionModelObject object = new DefinitionModelObject()
					.setOperation(request.getOperation())
						.setInstance(instance)
						.setDefinitionModel(model)
						.setRequestedFields(request.getRequestedFields());

			result.put((String) instance.getId(), object);
		}

		return result;
	}

}
