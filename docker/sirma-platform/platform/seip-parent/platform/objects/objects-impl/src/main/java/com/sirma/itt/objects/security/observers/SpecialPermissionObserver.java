package com.sirma.itt.objects.security.observers;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.MANAGER;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;
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
	private SecurityContext securityContext;

	@Inject
	private ResourceService resourceService;

	@Inject
	private TransactionalPermissionChanges permissionsChangeBuilder;

	/**
	 * When a saved search is created, set the special permissions.
	 *
	 * @param event
	 *            the event
	 */
	public <I extends Instance, E extends TwoPhaseEvent> void onCreateSavedSearch(
			@Observes AfterInstancePersistEvent<I, E> event) {
		I instance = event.getInstance();
		String instanceRdfType = instance.getAsString(DefaultProperties.SEMANTIC_TYPE);
		if (!nullSafeEquals(instanceRdfType, EMF.SAVED_SEARCH.toString())) {
			return;
		}

		PermissionsChangeBuilder builder = permissionsChangeBuilder.builder(instance.toReference());
		String currentUser = securityContext.getAuthenticated().getSystemId().toString();
		builder.addRoleAssignmentChange(currentUser, MANAGER.getIdentifier());

		if (!instance.getBoolean("mutable", false)) {
			builder.addRoleAssignmentChange(resourceService.getAllOtherUsers().getId().toString(),
					CONSUMER.getIdentifier());
		}
	}

}
