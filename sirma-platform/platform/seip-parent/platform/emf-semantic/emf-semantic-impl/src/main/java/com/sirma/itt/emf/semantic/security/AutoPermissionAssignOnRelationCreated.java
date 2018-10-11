package com.sirma.itt.emf.semantic.security;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.AddRoleAssignmentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Security;

/**
 * Observer that listens for add object property events and assign permissions to users depending on the configuration
 * of the created relation
 *
 * @author BBonev
 */
@ApplicationScoped
@Named(AutoPermissionAssignOnRelationCreated.NAME)
public class AutoPermissionAssignOnRelationCreated extends SchedulerActionAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String NAME = "AutoPermissionAssignOnRelationCreated";

	static final String MINIMAL_PERMISSION_ROLE = Security.AUTO_ASSIGN_PERMISSION_ROLE.getLocalName();
	static final String ALLOW_PERMISSION_OVERRIDE = Security.ALLOW_PERMISSION_OVERRIDE.getLocalName();
	static final String MINIMAL_PARENT_PERMISSION_ROLE = Security.AUTO_ASSIGN_PARENT_PERMISSION_ROLE.getLocalName();

	private static final String CHANGES = "sourceId";

	@Inject
	private PermissionService permissionService;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private RoleService roleService;

	@Inject
	private ResourceService resourceService;

	@Inject
	private SchedulerService schedulerService;

	@Inject
	private PermissionChangeRequestBuffer changeRequestBuffer;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private TransactionalPermissionChanges permissionChanges;

	@Inject
	private InstancePermissionsHierarchyResolver hierarchyResolver;

	@Inject
	private InstanceContextService contextService;

	void onRelationCreated(@Observes(during = TransactionPhase.BEFORE_COMPLETION) ObjectPropertyAddEvent event) {
		addChangeRequest(createChangeRequest(event));

		if (InstanceContextService.PART_OF_URI.equals(event.getObjectPropertyName())) {
			addChangeRequest(new ParentChangeRequest(event.getSourceId().toString(), event.getTargetId().toString()));
		}
	}

	private void addChangeRequest(PermissionChangeRequest changeRequest) {
		if (changeRequestBuffer.add(changeRequest)) {
			transactionSupport.invokeBeforeTransactionCompletion(this::flushChanges);
		}
	}

	private void flushChanges() {
		try {
			// get changes from the current transaction and reset the buffer so only the first to enter should trigger
			// update
			Collection<PermissionChangeRequest> changes = changeRequestBuffer.drainAll();
			if (isEmpty(changes)) {
				return;
			}
			transactionSupport.invokeOnSuccessfulTransactionInTx(() -> scheduleRoleAssignment(changes));
		} catch (ContextNotActiveException e) {
			LOGGER.warn("Could not get buffered changes!", e);
		}
	}

	private PermissionChangeRequest createChangeRequest(ObjectPropertyAddEvent event) {
		String addedRelation = event.getObjectPropertyName();
		PropertyInstance relation = semanticDefinitionService.getRelation(addedRelation);
		if (isRelationApplicable(relation)) {

			String minimalRoleId = relation.getString(MINIMAL_PERMISSION_ROLE);
			RoleIdentifier minimalRole = roleService.getRoleIdentifier(minimalRoleId);
			if (minimalRole == null) {
				// no configuration in the model or not recognized role name so nothing to do
				return null;
			}

			String targetInstance = Objects.toString(event.getSourceId(), null);
			String targetResource = Objects.toString(event.getTargetId(), null);
			boolean allowOverride = relation.getBoolean(ALLOW_PERMISSION_OVERRIDE);
			String parentRoleToAssign = relation.getString(MINIMAL_PARENT_PERMISSION_ROLE);
			return new AutoPermissionAssignmentChangeRequest(targetInstance, minimalRoleId, targetResource,
					allowOverride, parentRoleToAssign);
		}
		// not applicable so no change to generate
		return null;
	}

	private void scheduleRoleAssignment(Collection<PermissionChangeRequest> changes) {
		// the created configuration has a retry so that in case of created instance and non completed original
		// transaction it will try to assign the permissions later
		SchedulerConfiguration configuration = schedulerService
				.buildEmptyConfiguration(SchedulerEntryType.TIMED)
				.setScheduleTime(new Date())
				.setRemoveOnSuccess(true)
				.setPersistent(true)
				.setMaxRetryCount(5)
				.setRetryDelay(Long.valueOf(10))
				.setIncrementalDelay(true);

		SchedulerContext context = new SchedulerContext();
		context.put(CHANGES, (Serializable) changes);

		schedulerService.schedule(NAME, configuration, context);
	}

	@Override
	@RunAsSystem(protectCurrentTenant = true)
	public void execute(SchedulerContext context) throws Exception {
		Collection<PermissionChangeRequest> changes = context.getIfSameType(CHANGES, Collection.class);

		permissionChanges.disableAutomaticFlush();
		// process all changes sequentially as the permission service does not support parallel permission update
		for (PermissionChangeRequest changeRequest : changes) {
			if (changeRequest instanceof AutoPermissionAssignmentChangeRequest) {
				processPermissionChangeRequest((AutoPermissionAssignmentChangeRequest) changeRequest);
			} else if (changeRequest instanceof ParentChangeRequest) {
				processParentChangeRequest((ParentChangeRequest) changeRequest);
			}
		}

		// flush all changes now
		permissionChanges.drainChanges(permissionService::setPermissions);
	}

	private void processParentChangeRequest(ParentChangeRequest changeRequest) {
		Collection<InstanceReference> references = instanceTypeResolver.resolveReferences(
				Arrays.asList(changeRequest.getTargetInstance(), changeRequest.getTargetResource()));

		if (references.size() != 2) {
			// probably could not find the affected instance as this may enter here on new instance creation and
			// it will not be persisted, yet. This case is covered by the
			// method onAfterInstanceCreated(AfterInstancePersistEvent)
			return;
		}
		Iterator<InstanceReference> it = references.iterator();

		InstanceReference affectedInstance = it.next();
		InstanceReference newParent = it.next();
		PermissionsChangeBuilder builder = permissionChanges.builder(affectedInstance);

		builder.parentChange(newParent.getId());

		if (!hierarchyResolver.isAllowedForPermissionSource(newParent)) {
			builder.inheritFromParentChange(Boolean.FALSE);
		}
	}

	private void processPermissionChangeRequest(AutoPermissionAssignmentChangeRequest changeRequest) {

		String minimalRoleId = changeRequest.getRoleToAssign();
		RoleIdentifier minimalRole = roleService.getRoleIdentifier(minimalRoleId);
		if (minimalRole == null) {
			throw new EmfRuntimeException("Could not find information about role: " + minimalRoleId);
		}

		Collection<InstanceReference> references = resolveReferences(changeRequest.getTargetInstance(),
				changeRequest.getTargetResource());
		if (references.size() != 2) {
			// didn't find one or both instances
			LOGGER.warn(
					"Could not find both instances on automatic permission assign! Was looking for [{},{}] but got {}",
					changeRequest.getTargetInstance(), changeRequest.getTargetResource(), references);
			throw new EmfRuntimeException("Could not find both instances on automatic permission assign!");
		}

		doPermissionAssignIfNeeded(references, minimalRole, changeRequest.isAllowOverride(),
				changeRequest.getParentRoleToAssign());
	}

	private void doPermissionAssignIfNeeded(Collection<InstanceReference> references, RoleIdentifier minimalRole,
			boolean allowOverride, String parentRoleId) {

		Iterator<InstanceReference> it = references.iterator();
		InstanceReference currentInstance = it.next();
		InstanceReference targetResource = it.next();

		if (!isInstanceAllowedForAutoPermissionAssignment(currentInstance)) {
			// the current instance is not applicable for automatic permission assignment
			return;
		}

		if (!(targetResource.getType().instanceOf(EMF.USER) || targetResource.getType().instanceOf(EMF.GROUP))) {
			// the target is neither user or group
			return;
		}

		assignMinimalRole(currentInstance, targetResource, minimalRole, allowOverride);

		RoleIdentifier parentRole = roleService.getRoleIdentifier(parentRoleId);
		Optional<InstanceReference> context = contextService.getContext(currentInstance);
		if (isParentAllowedForPermissionAssignment(context)) {
			assignParentRole(context.get(), targetResource, parentRole);
		}
	}

	private static boolean isInstanceAllowedForAutoPermissionAssignment(InstanceReference currentInstance) {
		return !(currentInstance.getType().instanceOf(EMF.USER) || currentInstance.getType().instanceOf(EMF.GROUP)
				|| currentInstance.getType().instanceOf(EMF.CLASS_DESCRIPTION));
	}

	private Collection<InstanceReference> resolveReferences(String sourceId, String destinationId) {
		return instanceTypeResolver.resolveReferences(Arrays.asList(sourceId, destinationId));
	}

	private static boolean isParentAllowedForPermissionAssignment(Optional<InstanceReference> reference) {
		// check is root
		if (!reference.isPresent()) {
			return false;
		}
		return isInstanceAllowedForAutoPermissionAssignment(reference.get());
	}

	private boolean isRelationApplicable(PropertyInstance relation) {
		if (relation == null) {
			return false;
		}
		String rangeClass = relation.getRangeClass();
		ClassInstance range = semanticDefinitionService.getClassInstance(rangeClass);
		// we care only for relations with range emf:User or emf:Group
		return range != null && (isTypeOf(range, EMF.USER) || isTypeOf(range, EMF.GROUP));
	}

	private static boolean isTypeOf(InstanceType range, Serializable typeOf) {
		return range.instanceOf(typeOf) || range.hasSubType(typeOf.toString());
	}

	private void assignMinimalRole(InstanceReference target, InstanceReference resource, RoleIdentifier roleToAssign,
			boolean allowOverride) {

		ResourceRole currentRole = getAssignmentForCurrentUser(target, resource);
		boolean allowedToAssignPermissions = false;
		if (currentRole != null && currentRole.getRole() != null) {
			allowedToAssignPermissions = roleToAssign.getGlobalPriority() > currentRole.getRole().getGlobalPriority()
					|| allowOverride;
		} else {
			allowedToAssignPermissions = true;
		}

		if (allowedToAssignPermissions) {
			assignPermission(target, resource, roleToAssign);
		}
	}

	private ResourceRole getAssignmentForCurrentUser(InstanceReference instance, InstanceReference resource) {
		Map<String, ResourceRole> permissionAssignments = permissionService.getPermissionAssignments(instance);
		if (permissionAssignments.containsKey(resource.getId())) {
			return permissionAssignments.get(resource.getId());
		}
		List<Instance> groups = resourceService.getContainingResources(resource.getId());
		if (isEmpty(groups)) {
			return null;
		}
		return groups.stream()
				.filter(group -> !group.getId().equals(resourceService.getAllOtherUsers().getId()))
				.map(Instance::getId)
				.map(permissionAssignments::get)
				.filter(Objects::nonNull)
				.max(Comparator.comparing(ResourceRole::getRole))
				.orElse(null);
	}

	private void assignParentRole(InstanceReference target, InstanceReference resource, RoleIdentifier roleToAssign) {
		if (target == null || roleToAssign == null) {
			return;
		}

		ResourceRole currentRole = permissionService.getPermissionAssignment(target, resource.getId());
		if (currentRole == null || currentRole.getRole() == null
				|| hasPermissionsViaAllOthers(currentRole.getAuthorityId(), resource.getId())) {
			assignPermission(target, resource, roleToAssign);
		}
	}

	private boolean hasPermissionsViaAllOthers(String authorityId, String resourceId) {
		Serializable allOtherUsersId = resourceService.getAllOtherUsers().getId();
		// if assigned to all other users and the resource itself is not all other users,
		// so we don't change permissions for all other users
		return allOtherUsersId.equals(authorityId) && !resourceId.equals(allOtherUsersId);
	}

	private void assignPermission(InstanceReference target, InstanceReference resource, RoleIdentifier roleToAssign) {
		PermissionsChangeBuilder builder = permissionChanges.builder(target);

		// do not assign roles with lower priority, when there is already assigned role with bigger priority to the
		// resource
		if (!hasHigherRole(builder.build(), roleToAssign, resource.getId())) {
			builder.addRoleAssignmentChange(resource.getId(), roleToAssign.getIdentifier());
		}
	}

	private boolean hasHigherRole(List<PermissionsChange> currentChanges, RoleIdentifier roleToAssign,
			String authority) {
		for (PermissionsChange permissionsChange : currentChanges) {
			if (permissionsChange instanceof AddRoleAssignmentChange) {
				AddRoleAssignmentChange permissionAssignment = (AddRoleAssignmentChange) permissionsChange;

				if (permissionAssignment.getAuthority().equals(authority)) {
					RoleIdentifier existingRole = roleService.getRoleIdentifier(permissionAssignment.getRole());
					if (roleToAssign.getGlobalPriority() < existingRole.getGlobalPriority()) {
						return true;
					}
				}
			}
		}

		return false;
	}

}
