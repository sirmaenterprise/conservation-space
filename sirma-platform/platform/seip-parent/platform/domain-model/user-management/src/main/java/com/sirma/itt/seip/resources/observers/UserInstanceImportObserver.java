package com.sirma.itt.seip.resources.observers;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.BeforeInstanceImportEvent;
import com.sirma.itt.seip.resources.EmfResourcesUtil;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Observer for processing user instance import operations.
 *
 * @author smustafov
 */
class UserInstanceImportObserver {

	@Inject
	private SecurityContext securityContext;

	/**
	 * Handles user id generation before instance import validation is executed. Builds full username with tenant id and
	 * replaces the value in the instance. The instance validation before import will report error if there is a user
	 * with the same username.
	 *
	 * @param event thrown before instance import validation is executed
	 */
	void onBeforeInstanceImport(@Observes BeforeInstanceImportEvent event) {
		Instance instance = event.getInstance();
		if (instance != null) {
			String id = (String) instance.getId();
			// check if the db id contains the tenant id, and if not then we are creating new user, this is in order to
			// avoid check in database/cache and speed it up
			if (instance.type().is(ObjectTypes.USER) && !id.contains(securityContext.getCurrentTenantId())) {
				String userName = EmfResourcesUtil.buildUserName(instance.getAsString(ResourceProperties.USER_ID),
						securityContext);
				instance.add(ResourceProperties.USER_ID, userName);
			}
		}
	}

}
