package com.sirma.itt.objects.security.observers;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Update permission model of objects on there creation.
 *
 * @author nvelkov
 */
@ApplicationScoped
public class SpecialPermissionObserver {

	@Inject
	private PermissionService permissionService;
	@Inject
	private SecurityContext securityContext;
	@Inject
	private ResourceService resourceService;

	/**
	 * When a saved search is created, set the special permissions.
	 *
	 * @param event
	 *            the event
	 */
	public <I extends Instance, E extends TwoPhaseEvent> void onCreateSavedSearch(
			@Observes AfterInstancePersistEvent<I, E> event) {
		String instanceRdfType = event.getInstance().getAsString(DefaultProperties.SEMANTIC_TYPE);
		if (nullSafeEquals(instanceRdfType, EMF.SAVED_SEARCH.toString())) {
			InstanceReference reference = event.getInstance().toReference();
			String currentUser = securityContext.getAuthenticated().getSystemId().toString();

			PermissionsChangeBuilder builder = PermissionsChange.builder();
			builder.addRoleAssignmentChange(currentUser, SecurityModel.BaseRoles.MANAGER.getIdentifier());

			boolean mutable = event.getInstance().getBoolean("mutable", false);
			if (mutable) {
				Options.DISABLE_AUDIT_LOG.enable();
				builder.addRoleAssignmentChange(resourceService.getAllOtherUsers().getId().toString(),
						SecurityModel.BaseRoles.NO_PERMISSION.getIdentifier());
				permissionService.setPermissions(reference, builder.build());
				Options.DISABLE_AUDIT_LOG.disable();
			} else {
				builder.addRoleAssignmentChange(resourceService.getAllOtherUsers().getId().toString(),
						SecurityModel.BaseRoles.CONSUMER.getIdentifier());
				permissionService.setPermissions(reference, builder.build());
			}
		}
	}
}
