package com.sirma.itt.seip.rest.resources.instances;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_DEFINITION_ID;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_PARENT_INSTANCE_ID;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.BeanParam;
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
import com.sirma.itt.seip.instance.actions.save.CreateOrUpdateRequest;
import com.sirma.itt.seip.rest.annotations.http.method.PATCH;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * CRUD operations on instances.
 *
 * @author yasko
 * @author A. Kunchev
 */
@Path("/instances")
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@Transactional(TxType.REQUIRED)
public class InstanceResource {

	@Inject
	private SecurityContextManager securityContextManager;

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
	 * @param instance
	 *            Instance to create.
	 * @return Persisted instance.
	 * @see CreateOrUpdateRequest
	 * @see CreateOrUpdateAction
	 */
	@POST
	public Instance create(Instance instance) {
		if (instance.getId() != null && typeResolver.resolveReference(instance.getId()).isPresent()) {
			throw new BadRequestException("Instance is already created " + instance.getId());
		}

		CreateOrUpdateRequest createRequest = CreateOrUpdateRequest.buildCreateOrUpdateRequest(instance, new Date(),
				idManager::isPersisted);
		return (Instance) actions.callAction(createRequest);
	}

	/**
	 * Update an instance by merging the provided instance with the one retrieved by the specified identifier. For the
	 * instance is created version if it is not entirely new.
	 *
	 * @param id
	 *            Identifier of the instance to be updated.
	 * @param instance
	 *            Updated instance data.
	 * @return The updated instance.
	 * @see CreateOrUpdateRequest
	 * @see CreateOrUpdateAction
	 */
	@PATCH
	@Path("/{id}")
	public Instance update(@PathParam(KEY_ID) String id, Instance instance) {
		CreateOrUpdateRequest updateRequest = CreateOrUpdateRequest.buildCreateOrUpdateRequest(instance, new Date(),
				idManager::isPersisted);
		return (Instance) actions.callAction(updateRequest);
	}

	/**
	 * See {@link InstanceResource#update(String, Instance)}. This operation is the same (except that the provided
	 * instances must contain an identifier), but applied to a list of instances. For the not new instances is created
	 * version.
	 *
	 * @param instances
	 *            the collection of instances which should be updated
	 * @return collection of updated instances. May return empty collection when there aren't updated instances
	 * @see CreateOrUpdateRequest
	 * @see CreateOrUpdateAction
	 */
	@PATCH
	public List<Instance> updateAll(List<Instance> instances) {
		Date versionTimestamp = new Date();
		Function<Instance, CreateOrUpdateRequest> buildCreateRequest = instance -> CreateOrUpdateRequest.buildCreateOrUpdateRequest(instance, versionTimestamp,
				idManager::isPersisted);

		return instances
				.stream()
					.filter(isValidInstance())
					.map(buildCreateRequest)
					.map(securityContextManager.wrap().function(actions::callAction))
					.map(Instance.class::cast)
					.collect(Collectors.toCollection(LinkedList::new));
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
	 * @param id
	 *            Instance identifier.
	 * @param allowDeleted
	 *            could be used to specify if the service should return information about soft deleted instances
	 * @return Instance retrieved by the provided identifier.
	 */
	@GET
	@Path("/{id}")
	@Transactional(TxType.NOT_SUPPORTED)
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
	 * @param request
	 *            {@link RequestInfo} containing the search criteria.
	 * @return Matched instances.
	 */
	@GET
	@Transactional(TxType.NOT_SUPPORTED)
	public Collection<Instance> findAll(@BeanParam RequestInfo request) {
		Collection<String> identifiers = RequestParams.QUERY_IDS.get(request);
		return findAllInternal(identifiers);
	}

	/**
	 * Loads batch of instances.
	 *
	 * @param identifiers
	 * @return Matched instances.
	 */
	@POST
	@Path("/batch")
	@Transactional(TxType.NOT_SUPPORTED)
	public Collection<Instance> batch(Collection<String> identifiers) {
		return findAllInternal(identifiers);
	}

	private Collection<Instance> findAllInternal(Collection<String> identifiers) {
		if (CollectionUtils.isEmpty(identifiers)) {
			throw new BadRequestException("Can't load resources for null identifiers.");
		}

		return domainInstanceService.loadInstances(identifiers);
	}

	/**
	 * Retrieve default values for new instance.
	 *
	 * @param definitionId
	 * @param parentInstanceId
	 *            parent instance identifier or null if there is no parent
	 * @return Instance
	 */
	@GET
	@Path("/defaults")
	@Transactional(TxType.NOT_SUPPORTED)
	public Instance defaults(@QueryParam(KEY_DEFINITION_ID) String definitionId,
			@QueryParam(KEY_PARENT_INSTANCE_ID) String parentInstanceId) {
		Instance createdInstance = domainInstanceService.createInstance(definitionId, parentInstanceId);
		idManager.unregister(createdInstance);
		return createdInstance;
	}

	/**
	 * Delete an instance by identifier. Deletion is *not* permanent.
	 *
	 * @param id
	 *            Identifier of the instance to be deleted.
	 */
	@DELETE
	@Path("/{id}")
	public void delete(@PathParam(KEY_ID) String id) {
		deleteAll(Arrays.asList(id));
	}

	/**
	 * See {@link InstanceResource#delete(String)}. Operation is the same, but applied to a list of identifiers. List
	 * must be provided as a request payload.
	 *
	 * @param identifiers
	 *            List of instance identifiers to delete.
	 * @throws BadRequestException
	 *             when the passed collection is empty
	 * @throws ResourceException
	 *             when the passed identifiers could not be resolved to instances
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
			actions.callAction(request);
		});
	}

	/**
	 * Gets the instance context path. It's the path of the instance to it's parent root
	 *
	 * @param instanceId
	 *            the instance id
	 * @return the instance context path
	 */
	@GET
	@Path("/{id}/context")
	public ContextPath getInstanceContextPath(@PathParam(RequestParams.KEY_ID) String instanceId) {
		return new ContextPath(domainInstanceService.getInstanceContext(instanceId));
	}
}
