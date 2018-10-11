package com.sirma.itt.seip.permissions.db.patches;

import java.util.List;
import java.util.Optional;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.event.SemanticDefinitionsReloaded;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.permissions.EntityPermissions;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Enables object library permissions in relation db - uses {@link PermissionService} which fires related event to
 * enable permissions in semantic. Only missing libraries information is added
 *
 * @author bbanchev
 */
class LibrariesPermissionInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LibrariesPermissionInitializer.class);

	@Inject
	private Instance<SemanticDefinitionService> semanticDefinitionService;
	@Inject
	private PermissionService permissionService;
	@Inject
	private ResourceService resourceService;
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/**
	 * Initializes permissions of libraries on tenant creation. Also marks a library as such in the permission model, if
	 * its not marked.
	 */
	@OnTenantAdd
	void initializeLibraryPermissions() {
		List<ClassInstance> objectLibrary = semanticDefinitionService.get().getLibrary(LibraryProvider.OBJECT_LIBRARY);

		String allOtherUsers = resourceService.getAllOtherUsers().getId().toString();

		List<PermissionsChange> permissionChanges = PermissionsChange
				.builder()
					.addRoleAssignmentChange(allOtherUsers, SecurityModel.BaseRoles.CONSUMER.getIdentifier())
					.setLibraryIndicatorChange(true)
					.build();

		List<PermissionsChange> libraryPermissionChanges = PermissionsChange.builder()
				.setLibraryIndicatorChange(true)
				.build();

		for (ClassInstance classInstance : objectLibrary) {
			// the class instance should not be used for permission assignment
			ObjectInstance library = new ObjectInstance();
			String uri = namespaceRegistryService.getShortUri(classInstance.getId().toString());
			library.setId(uri);
			library.setType(classInstance);
			InstanceReference libraryRef = library.toReference();

			Optional<EntityPermissions> permissionsInfo = permissionService.getPermissionsInfo(libraryRef);
			if (!permissionsInfo.isPresent()) {
				permissionService.setPermissions(libraryRef, permissionChanges);
				LOGGER.info("Initialized library permissions of: {}", classInstance.getId());
			} else if (!permissionsInfo.get().isLibrary()) {
				// for some reason the library is not marked as library in permission model, marking it explicitly
				permissionService.setPermissions(libraryRef, libraryPermissionChanges);
				LOGGER.info("Marked {} as library in permission model", classInstance.getId());
			}
		}
	}

	/**
	 * Initializes permissions of libraries when semantic model change is triggered. The event for semantic model change
	 * is also triggered on server startup.
	 *
	 * @param event
	 *            fired when semantic definitions are reloaded
	 */
	void initializeLibraryPermissionsOnModelsReload(@Observes SemanticDefinitionsReloaded event) {
		initializeLibraryPermissions();
	}

}
