package com.sirma.itt.seip.rest.resources.instances;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_DEFINITION_ID;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_PARENT_INSTANCE_ID;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_PROPERTIES;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.delete.DeleteRequest;
import com.sirma.itt.seip.instance.actions.save.SaveRequest;
import com.sirma.itt.seip.rest.annotations.http.method.PATCH;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.handlers.writers.PropertiesFilterBuilder;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * CRUD operations on instances.
 *
 * @author yasko
 * @author A. Kunchev
 */
@Transactional
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
public class InstanceResource {

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private InstanceTypeResolver typeResolver;

	@Inject
	private DatabaseIdManager idManager;

	@Inject
	private Actions actions;

	/**
	 * Creates a new instance.
	 *
	 * @param instance Instance to create.
	 * @return Persisted instance.
	 * @see SaveRequest
	 */
	@POST
	public Instance create(Instance instance) {
		if (instance.getId() != null && typeResolver.resolveReference(instance.getId()).isPresent()) {
			throw new BadRequestException("Instance is already created " + instance.getId());
		}

		SaveRequest createRequest = SaveRequest.buildSaveRequest(instance, new Date(), idManager::isPersisted);
		return (Instance) actions.callAction(createRequest);
	}

	/**
	 * Update an instance by merging the provided instance with the one retrieved by the specified identifier. For the
	 * instance is created version if it is not entirely new.
	 *
	 * @param id Identifier of the instance to be updated.
	 * @param instance Updated instance data.
	 * @return The updated instance.
	 * @see SaveRequest
	 */
	@PATCH
	@Path("/{id}")
	public Instance update(@PathParam(KEY_ID) String id, Instance instance) {
		SaveRequest updateRequest = SaveRequest.buildSaveRequest(instance, idManager::isPersisted);
		return (Instance) actions.callAction(updateRequest);
	}

	/**
	 * See {@link InstanceResource#update(String, Instance)}. This operation is the same (except that the provided
	 * instances must contain an identifier), but applied to a list of instances. For the not new instances is created
	 * version.
	 *
	 * @param instances the collection of instances which should be updated
	 * @param properties the requested properties to return
	 * @return collection of updated instances. May return empty collection when there aren't updated instances
	 * @see SaveRequest
	 */
	@PATCH
	public InstancesLoadResponse updateAll(List<Instance> instances,
			@QueryParam(KEY_PROPERTIES) List<String> properties) {
		Date versionTimestamp = new Date();
		Function<Instance, SaveRequest> updateInstanceAction = instance -> SaveRequest.buildSaveRequest(instance,
				versionTimestamp, idManager::isPersisted);

		LinkedList<Instance> list = instances
				.stream()
					.filter(isValidInstance())
					.map(updateInstanceAction)
					.map(actions::callSlowAction)
					.map(Instance.class::cast)
					.collect(Collectors.toCollection(LinkedList::new));

		PropertiesFilterBuilder filter = InstanceToJsonSerializer.allOrGivenProperties(properties);
		return new InstancesLoadResponse().setInstances(list).setPropertiesFilter(filter);
	}

	private static Predicate<Instance> isValidInstance() {
		return instance -> {
			if (StringUtils.isBlank((String) instance.getId())) {
				throw new BadRequestException("Instance id is required when updating.");
			}
			return true;
		};
	}

	/**
	 * Retrieve an instance by identifier.
	 *
	 * @param id Instance identifier.
	 * @param allowDeleted could be used to specify if the service should return information about soft deleted
	 *        instances
	 * @return Instance retrieved by the provided identifier.
	 */
	@GET
	@Path("/{id}")
	public Instance find(@PathParam(KEY_ID) String id,
			@DefaultValue("false") @QueryParam(RequestParams.KEY_ALLOW_DELETED) boolean allowDeleted) {
		try {
			return domainInstanceService.loadInstance(id, allowDeleted);
		} catch (InstanceNotFoundException e) {
			// if the instance is missing we should throw web exception with correct status
			throw new ResourceNotFoundException(e);
		}
	}

	/**
	 * Search for instances.
	 *
	 * @param identifiers of the instances that should be loaded
	 * @param properties the properties that should be loaded for the instances
	 * @param allowDeleted could be used to specify if the service should return information about soft deleted
	 *        instances
	 * @return object that holds loaded instances with specified properties if such are requested
	 */
	@GET
	public InstancesLoadResponse findAll(@QueryParam(KEY_ID) List<String> identifiers,
			@QueryParam(KEY_PROPERTIES) List<String> properties,
			@DefaultValue("false") @QueryParam("allowDeleted") boolean allowDeleted) {
		return findAllInternal(new InstancesLoadRequest()
				.setInstanceIds(identifiers)
					.setProperties(properties)
					.setAllowDeleted(allowDeleted));
	}

	/**
	 * Loads batch of instances.
	 *
	 * @param request contains request data, like identifiers of the instances that should be loaded and the properties
	 *        that should be loaded for those instances
	 * @return object that holds loaded instances with specified properties if such are requested
	 */
	@POST
	@Path("/batch")
	public InstancesLoadResponse batch(InstancesLoadRequest request) {
		return findAllInternal(request);
	}

	private InstancesLoadResponse findAllInternal(InstancesLoadRequest request) {
		if (CollectionUtils.isEmpty(request.getInstanceIds())) {
			throw new BadRequestException("The list of instance identifiers is empty.");
		}

		Collection<Instance> instances = domainInstanceService.loadInstances(request.getInstanceIds(),
				request.getAllowDeleted());
		PropertiesFilterBuilder filter = InstanceToJsonSerializer.allOrGivenProperties(request.getProperties());
		return new InstancesLoadResponse().setInstances(instances).setPropertiesFilter(filter);
	}

	/**
	 * Retrieve default values for new instance.
	 *
	 * @param definitionId the requested definition identifier that should be loaded and it's properties returned.
	 * @param parentInstanceId parent instance identifier or null if there is no parent
	 * @return Instance
	 */
	@GET
	@Path("/defaults")
	public Instance defaults(@QueryParam(KEY_DEFINITION_ID) String definitionId,
			@QueryParam(KEY_PARENT_INSTANCE_ID) String parentInstanceId) {
		Instance createdInstance = domainInstanceService.createInstance(definitionId, parentInstanceId);
		idManager.unregister(createdInstance);
		return createdInstance;
	}

	/**
	 * Delete an instance by identifier. Deletion is *not* permanent.
	 *
	 * @param id Identifier of the instance to be deleted.
	 */
	@DELETE
	@Path("/{id}")
	public void delete(@PathParam(KEY_ID) String id) {
		deleteAll(Collections.singletonList(id));
	}

	/**
	 * See {@link InstanceResource#delete(String)}. Operation is the same, but applied to a list of identifiers. List
	 * must be provided as a request payload.
	 *
	 * @param identifiers List of instance identifiers to delete.
	 * @throws BadRequestException when the passed collection is empty
	 * @throws ResourceException when the passed identifiers could not be resolved to instances
	 */
	@DELETE
	public void deleteAll(List<String> identifiers) {
		if (CollectionUtils.isEmpty(identifiers)) {
			throw new BadRequestException("The passed id collection is empty.");
		}

		identifiers.forEach(id -> {
			DeleteRequest request = new DeleteRequest();
			request.setTargetId(id);
			request.setUserOperation(DeleteRequest.DELETE_OPERATION);
			actions.callSlowAction(request);
		});
	}

	/**
	 * Gets the instance context path. It's the path of the instance to it's parent root
	 *
	 * @param instanceId the instance id
	 * @return the instance context path
	 */
	@GET
	@Path("/{id}/context")
	public ContextPath getInstanceContextPath(@PathParam(RequestParams.KEY_ID) String instanceId) {
		return new ContextPath(domainInstanceService.getInstanceContext(instanceId));
	}
}