package com.sirma.itt.seip.permissions.observers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_LIBRARY_PERMISSIONS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_PARENT_PERMISSIONS;

import java.util.Map;
import java.util.stream.Stream;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.annotation.DisableAudit;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.instance.event.ParentChangedEvent;
import com.sirma.itt.seip.permissions.EntityPermissions;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Update permission model of created instances in relation to their parent/context. Sets the inherited roles from the
 * current parents or special permissions for the creator if no parent is specified
 *
 * @author BBonev
 */
public class InheritedPermissionObserver {

	@Inject
	private RoleService roleService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private InstancePermissionsHierarchyResolver hierarchyResolver;

	@Inject
	private TransactionalPermissionChanges permissionsChangeBuilder;

	@Inject
	private PermissionService permissionService;

	@Inject
	private InstanceContextService contextService;

	/**
	 * Instance creation assign inherited permissions if there is a parent of the instance or special permission to the
	 * creator of the instance
	 *
	 * @param event the event
	 */
	@DisableAudit
	public <I extends Instance> void onAfterInstanceCreated(@Observes AfterInstancePersistEvent<I, ?> event) {
		I instance = event.getInstance();
		if (instance.type().is(ObjectTypes.USER)) {
			// users does not inherit or manage any inheritance permissions
			// all needed permissions are set in the observer for user creation integration
			// see UserInstanceCreationObserver.assignPermissions | CMF-26128
			return;
		}
		InstanceReference inheritFrom = contextService.getContext(instance).orElse(null);

		InstanceReference reference = instance.toReference();
		InstanceReference library = hierarchyResolver.getLibrary(reference);

		PermissionsChangeBuilder builder = permissionsChangeBuilder.builder(reference);

		builder.libraryChange(library.getId());
		boolean enabledLibraryInheritance = instance.type().hasTrait(INHERIT_LIBRARY_PERMISSIONS);
		builder.inheritFromLibraryChange(enabledLibraryInheritance);

		boolean enableParentInheritance = instance.type().hasTrait(INHERIT_PARENT_PERMISSIONS);

		// if the created instance has no enabled parent or library inheritance we should
		// assign manager permissions to the instance for the creator as he/she may not get any other permissions for
		// the instance he/she is creating
		boolean noEffectivePermissionInheritance = !(enabledLibraryInheritance || enableParentInheritance);

		// by default the parent is not allowed for inheritance
		// so if there is no parent special permissions will be assigned to the instance
		boolean isParentAllowedForInheritance = false;

		if (inheritFrom != null) {
			// if the instance's parent is eligible then parent inheritance will be enabled
			isParentAllowedForInheritance = isEligibleForInheritance(inheritFrom);

			// all instances will be set as parents but if they are not allowed for parent inheritance it will not be
			// enabled even if configured in the model
			// these information is stored in the semantic database but it's omitted from the permissions database model
			// that leads to wrong assumptions for the permissions
			enableParentInheritance &= isParentAllowedForInheritance;

			// we set the parent no mater if it's eligible or not
			// the inheritance flag will control if we take the permissions from him or not
			builder.parentChange(inheritFrom.getId());
		}

		builder.inheritFromParentChange(enableParentInheritance);

		if (inheritFrom == null || !isParentAllowedForInheritance || noEffectivePermissionInheritance) {
			// ensure at least one manager
			String currentUser = securityContext.getAuthenticated().getSystemId().toString();
			builder.addRoleAssignmentChange(currentUser, roleService.getManagerRole().getIdentifier());
		}
	}

	/**
	 * Updates the permission data by setting the new parent when the instance is moved.
	 *
	 * @param event holds information about the instance and its new parent.
	 */
	@DisableAudit
	public void onParentChanged(@Observes ParentChangedEvent event) {
		if (event.getNewParent() != null) {
			processWithParent(event);
		} else {
			processWithoutParent(event);
		}
	}

	private void processWithParent(ParentChangedEvent event) {
		Instance newParent = event.getNewParent();
		InstanceReference movedReference = event.getInstance().toReference();
		InstanceReference newParentReference = newParent.toReference();

		PermissionsChangeBuilder builder = permissionsChangeBuilder.builder(movedReference);

		boolean isParentInheritanceEnabled = permissionService.getPermissionsInfo(movedReference)
				.filter(EntityPermissions::isInheritFromParent)
				.isPresent();
		if (isParentInheritanceEnabled) {
			// only keep the parent inheritance if it's already enabled and the new parent is eligible for such
			boolean isParentAllowedForInheritance = isEligibleForInheritance(newParentReference);
			builder.inheritFromParentChange(isParentAllowedForInheritance);
		}
		builder.parentChange(newParentReference.getId());
	}

	private void processWithoutParent(ParentChangedEvent event) {
		InstanceReference movedReference = event.getInstance().toReference();
		PermissionsChangeBuilder builder = permissionsChangeBuilder.builder(movedReference);

		String managerRole = SecurityModel.BaseRoles.MANAGER.getIdentifier();
		getUsersWithRole(movedReference, managerRole).forEach(
				userId -> builder.addRoleAssignmentChange(userId, managerRole));
		builder.parentChange(null);
	}

	private Stream<String> getUsersWithRole(InstanceReference instanceReference, String roleIdentifier) {
		return permissionService.getPermissionAssignments(instanceReference)
				.entrySet()
				.stream()
				.filter(entry -> roleIdentifier.equals(entry.getValue().getRole().getIdentifier()))
				.map(Map.Entry::getKey);
	}

	private boolean isEligibleForInheritance(InstanceReference parentReference) {
		return hierarchyResolver.isAllowedForPermissionSource(parentReference);
	}
}
