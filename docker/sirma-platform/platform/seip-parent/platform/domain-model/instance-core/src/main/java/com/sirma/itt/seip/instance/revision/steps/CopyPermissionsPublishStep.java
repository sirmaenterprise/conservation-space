package com.sirma.itt.seip.instance.revision.steps;

import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.PermissionModelType;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Publish step to assign the permissions to a revision. The permissions are set by copying all permissions from the
 * source instance and setting them as special permissions to the revision so that the revision is not dependent on the
 * original instance.
 *
 * @author BBonev
 */
@Extension(target = PublishStep.EXTENSION_NAME, order = 120)
public class CopyPermissionsPublishStep implements PublishStep {

	@Inject
	private PermissionService permissionService;

	@Inject
	private InstancePermissionsHierarchyResolver hierarchyResolver;

	@Override
	public void execute(PublishContext publishContext) {
		InstanceReference permissionsSource = publishContext.getRequest().getInstanceToPublish().toReference();
		InstanceReference permissionsTarget = publishContext.getRevision().toReference();
		PermissionsChangeBuilder permissionsBuilder = PermissionsChange.builder();

		InstanceReference library = hierarchyResolver.getLibrary(permissionsSource);
		permissionsBuilder.libraryChange(library.getId());

		PermissionModelType permissionModel = permissionService.getPermissionModel(permissionsSource);
		if (permissionModel.isDefined()) {
			permissionsBuilder.inheritFromLibraryChange(permissionModel.isLibrary());
			permissionsBuilder.inheritFromParentChange(permissionModel.isInherited());
		}

		Map<String, ResourceRole> assignments = permissionService.getPermissionAssignments(permissionsSource);

		for (Entry<String, ResourceRole> entry : assignments.entrySet()) {
			permissionsBuilder.addRoleAssignmentChange(entry.getKey(), entry.getValue().getRole().getIdentifier());
		}
		permissionService.setPermissions(permissionsTarget, permissionsBuilder.build());
	}

	@Override
	public String getName() {
		return Steps.COPY_PERMISSIONS.getName();
	}

}
