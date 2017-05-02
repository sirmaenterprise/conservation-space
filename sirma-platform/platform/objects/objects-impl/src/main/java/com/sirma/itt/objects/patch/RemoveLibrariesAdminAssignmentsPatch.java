package com.sirma.itt.objects.patch;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * @author Adrian Mitev
 */
public class RemoveLibrariesAdminAssignmentsPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private PermissionService permissionService;

	private LibraryProvider libraryProvider;

	private TransactionSupport transactionSupport;

	private SecurityConfiguration securityConfiguration;

	private ResourceService resourceService;

	private RoleService roleService;

	@Override
	public void setUp() throws SetupException {
		libraryProvider = CDI.instantiateBean(LibraryProvider.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		permissionService = CDI.instantiateBean(PermissionService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		securityConfiguration = CDI.instantiateBean(SecurityConfiguration.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		resourceService = CDI.instantiateBean(ResourceService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		roleService = CDI.instantiateBean(RoleService.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		TimeTracker tracker = TimeTracker.createAndStart();

		transactionSupport.invokeInNewTx(() -> migrate());

		LOGGER.info("Removed assignments in {} ms.", tracker.stop());
	}

	void migrate() {
		List<Instance> libraries = libraryProvider.getLibraries(ActionTypeConstants.VIEW_DETAILS);

		String adminUserId = securityConfiguration.getAdminUser().get().getSystemId().toString();
		Resource adminGroup = resourceService.getResource(securityConfiguration.getAdminGroup().get(),
				ResourceType.GROUP);
		String adminGroupId = null;
		if (adminGroup != null) {
			adminGroupId = adminGroup.getId().toString();
		}

		String managerRole = roleService.getManagerRole().getIdentifier();

		List<PermissionsChange> changes = PermissionsChange
				.builder()
					.removeRoleAssignmentChange(adminUserId, managerRole)
					.removeRoleAssignmentChange(adminGroupId, managerRole)
					.setLibraryIndicatorChange(true)
					.build();

		for (Instance library : libraries) {
			permissionService.setPermissions(library.toReference(), changes);
		}
	}

	@Override
	public String getConfirmationMessage() {
		return "Remove explicitly assigned permissions in libraries for admin and admin group was successful!";
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// Not used
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

}
