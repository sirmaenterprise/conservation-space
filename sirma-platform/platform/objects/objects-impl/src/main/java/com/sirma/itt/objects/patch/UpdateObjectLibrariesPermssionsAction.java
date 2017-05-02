package com.sirma.itt.objects.patch;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Enables object library permissions in relation db - uses {@link PermissionService} which fires related event to
 * enable permissions in semantic. Only missing libraries information is added
 *
 * @author bbanchev
 */
@ApplicationScoped
@Named(UpdateObjectLibrariesPermssionsAction.ACTION_NAME)
public class UpdateObjectLibrariesPermssionsAction {

	public static final String ACTION_NAME = "UpdateObjectLibrariesPermssionsAction";

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateObjectLibrariesPermssionsAction.class);

	@Inject
	private Instance<SemanticDefinitionService> semanticDefinitionService;
	@Inject
	private PermissionService permissionService;
	@Inject
	private ResourceService resourceService;
	@Inject
	private SecurityConfiguration securityConfiguration;
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/**
	 * Add change listener so that when the role changes we can reaply the action.
	 */
	@PostConstruct
	void initialize() {
		securityConfiguration.getManagerRole().addConfigurationChangeListener(c -> initializeLibraryPermissions());
	}

	/**
	 * Executes the actual update.
	 *
	 * @param context
	 *            the context
	 */
	@Startup(phase = StartupPhase.AFTER_APP_START)
	@RunAsAllTenantAdmins
	@OnTenantAdd
	public void initializeLibraryPermissions() {
		LOGGER.debug("Starting of initialization of Object Libraries Permissions");
		List<ClassInstance> objectLibrary = semanticDefinitionService.get().getLibrary(LibraryProvider.OBJECT_LIBRARY);

		String allOtherUsers = resourceService.getAllOtherUsers().getId().toString();

		List<PermissionsChange> permissionChanges = PermissionsChange
				.builder()
					.addRoleAssignmentChange(allOtherUsers, SecurityModel.BaseRoles.CONSUMER.getIdentifier())
					.setLibraryIndicatorChange(true)
					.build();

		for (ClassInstance classInstance : objectLibrary) {
			// the class instance should not be used for permission assignment
			ObjectInstance library = new ObjectInstance();
			String uri = namespaceRegistryService.getShortUri(classInstance.getId().toString());
			library.setId(uri);
			library.setType(classInstance);
			InstanceReference libraryRef = library.toReference();
			if (!permissionService.getPermissionModel(libraryRef).isDefined()) {
				permissionService.setPermissions(libraryRef, permissionChanges);
			}
		}
	}
}