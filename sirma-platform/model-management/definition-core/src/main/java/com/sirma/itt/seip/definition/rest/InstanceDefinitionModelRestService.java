package com.sirma.itt.seip.definition.rest;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.EDIT_DETAILS;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_OPERATION;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.QUERY_IDS;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.QUERY_OPERATION;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.Collection;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.rest.writers.DefinitionModelBodyWriter;
import com.sirma.itt.seip.definition.rest.writers.DefinitionModelMapBodyWriter;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Used to retrieve {@link DefinitionModel} for instances. The definitions are extracted from the instance, so the
 * revision of the definition is correct.
 *
 * @author A. Kunchev
 */
@Path("/instances")
@Produces(Versions.V2_JSON)
@Consumes(Versions.V2_JSON)
@ApplicationScoped
public class InstanceDefinitionModelRestService {

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private InstanceContextInitializer contextInitializer;

	/**
	 * Retrieves {@link DefinitionModel} for passed instance id. The instance is first resolved and then used to extract
	 * the correct definition for it.
	 *
	 * @param id
	 *            the id of the instance which definition model will be returned
	 * @param operation
	 *            the operation that is executed. Used to evaluate any mandatory fields for the transition. Default
	 *            value for this is "editDetails"
	 * @return {@link DefinitionModelObject} used to convert the {@link DefinitionModel} to JSON
	 * @see DefinitionModelBodyWriter
	 */
	@GET
	@Path("/{id}/model")
	public DefinitionModelObject getInstanceDefinitionModel(@PathParam(KEY_ID) String id,
			@DefaultValue(EDIT_DETAILS) @QueryParam(KEY_OPERATION) String operation) {
		Instance instance = instanceTypeResolver.resolveReference(id).map(InstanceReference::toInstance).orElseThrow(
				() -> new ResourceException(NOT_FOUND,
						new ErrorData().setMessage("Could not find instance with id: " + id), null));
		contextInitializer.restoreHierarchy(instance);

		return buildDefinitionModelObject(operation, instance, dictionaryService.getInstanceDefinition(instance));
	}

	/**
	 * Retrieves {@link DefinitionModel}s for the passed instance ids. First the instances for the request are resolved
	 * and then used to build map with {@link DefinitionModelObject}. For the key for the map are used the instance ids
	 * of the resolved instances and for the value are build {@link DefinitionModelObject} from the instances. For every
	 * instance is retrieved the {@link DefinitionModel} from the instance, that way we guarantee that we get the
	 * correct definition.
	 *
	 * @param request
	 *            the {@link RequestInfo} from which we retrieve the instances ids and the operation
	 * @return map with instance ids and {@link DefinitionModelObject} used to convert to JSON
	 * @see DefinitionModelMapBodyWriter
	 */
	@GET
	@Path("/model")
	public Map<String, DefinitionModelObject> getInstancesDefinitionModels(@BeanParam RequestInfo request) {
		Collection<String> ids = QUERY_IDS.get(request);
		String operation = QUERY_OPERATION.get(request);
		return getInstancesDefinitionModelsInternal(ids, operation);
	}

	/**
	 * Retrieves {@link DefinitionModel}s for the passed instance ids.
	 *
	 * @param ids
	 *            instance ids
	 * @param operation
	 *            operation id
	 * @return map with instance ids and {@link DefinitionModelObject} used to convert to JSON
	 */
	@POST
	@Path("/model/batch")
	public Map<String, DefinitionModelObject> getInstancesDefinitionModelsBatch(Collection<String> ids, @QueryParam(KEY_OPERATION)String operation) {
		return getInstancesDefinitionModelsInternal(ids, operation);
	}

	private Map<String, DefinitionModelObject> getInstancesDefinitionModelsInternal(Collection<String> ids,
			String operation) {
		if (ids.isEmpty()) {
			throw new BadRequestException("There are no instance ids in the request or the request key is wrong.");
		}

		Collection<Instance> instances = instanceTypeResolver.resolveInstances(ids);
		Map<String, DefinitionModelObject> result = CollectionUtils.createHashMap(instances.size());

		for (Instance instance : instances) {
			DefinitionModel model = dictionaryService.getInstanceDefinition(instance);
			if (model == null) {
				continue;
			}

			DefinitionModelObject object = buildDefinitionModelObject(operation, instance, model);
			result.put((String) instance.getId(), object);
		}

		return result;
	}

	private static DefinitionModelObject buildDefinitionModelObject(String operation, Instance instance,
			DefinitionModel model) {
		DefinitionModelObject object = new DefinitionModelObject();
		object.setOperation(operation);
		object.setInstance(instance);
		object.setDefinitionModel(model);
		return object;
	}

}
