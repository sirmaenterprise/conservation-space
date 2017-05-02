package com.sirma.itt.seip.permissions.observers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_LIBRARY_PERMISSIONS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_PARENT_PERMISSIONS;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.annotation.DisableAudit;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.event.AfterInstanceMoveEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
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
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private TransactionalPermissionChanges permissionsChangeBuilder;

	/**
	 * Instance creation assign inherited permissions if there is a parent of the instance or special permission to the
	 * creator of the instance
	 *
	 * @param event the event
	 */
	@DisableAudit
	public <I extends Instance> void onAfterInstanceCreated(@Observes AfterInstancePersistEvent<I, ?> event) {
		I instance = event.getInstance();
		Instance inheritFrom = InstanceUtil.getDirectParent(instance);

		InstanceReference reference = instance.toReference();
		InstanceReference library = hierarchyResolver.getLibrary(reference);

		PermissionsChangeBuilder builder = permissionsChangeBuilder.builder(reference);

		builder.libraryChange(library.getIdentifier());
		builder.inheritFromLibraryChange(instance.type().hasTrait(INHERIT_LIBRARY_PERMISSIONS));

		builder.inheritFromParentChange(instance.type().hasTrait(INHERIT_PARENT_PERMISSIONS));

		if (isEligibleForInheritance(inheritFrom)) {
			builder.parentChange((String) inheritFrom.getId());
		} else {
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
	public void onInstanceMoved(@Observes AfterInstanceMoveEvent event) {
		Instance newParent = event.getTargetInstance();
		Instance movedInstance = event.getInstance();
		InstanceReference movedReference = movedInstance.toReference();

		PermissionsChangeBuilder builder = permissionsChangeBuilder.builder(movedReference);

		if (isEligibleForInheritance(newParent)) {
			builder.parentChange((String) newParent.getId());
		} else {
			builder.parentChange(null);
		}
	}

	private boolean isEligibleForInheritance(Instance instance) {
		return instance != null && hierarchyResolver.isAllowedForPermissionSource(instance.toReference());
	}
}
