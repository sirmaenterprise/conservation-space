package com.sirma.itt.seip.permissions.action;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Set;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Action filter removes disable action for admin user to prevent him from being deactivated as it will block the system.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 05/10/2017
 */
@Singleton
class AdminActionFilter {

	private static final Action DEACTIVATE = new EmfAction(ActionTypeConstants.DEACTIVATE);
	private static final Action ACTIVATE = new EmfAction(ActionTypeConstants.ACTIVATE);

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Inject
	private ResourceService resourceService;

	/**
	 * Remove disable action for admin user
	 *
	 * @param event the action filtering event
	 */
	void removeAdminDisableAction(@Observes ActionEvaluatedEvent event) {
		Instance instance = event.getInstance();
		Set<Action> actions = event.getActions();
		if (!actions.isEmpty()) {
			if (isAdminUser(instance) || isSystemGroup(instance)) {
				actions.remove(DEACTIVATE);
			} else if (isSystemUser(instance)) {
				// the system user should not be allowed to get activated or deactivated
				actions.remove(ACTIVATE);
				actions.remove(DEACTIVATE);
			}
		}
	}

	private boolean isSystemUser(Instance instance) {
		return instance.type().is(ObjectTypes.USER) && resourceService.areEqual(
				securityContextManager.getSystemUser().getSystemId(), instance.getId());
	}

	private boolean isSystemGroup(Instance instance) {
		return instance.type().is(ObjectTypes.GROUP) && (isAdminGroup(instance) || isEveryoneGroup(instance));
	}

	private boolean isEveryoneGroup(Instance instance) {
		return nullSafeEquals(resourceService.getAllOtherUsers().getId(), instance.getId());
	}

	private boolean isAdminGroup(Instance instance) {
		return resourceService.areEqual(securityConfiguration.getAdminGroup().get(), instance.getId());
	}

	private boolean isAdminUser(Instance instance) {
		return instance.type().is(ObjectTypes.USER) &&
				nullSafeEquals(instance.getId(), securityContextManager.getAdminUser().getSystemId());
	}
}
