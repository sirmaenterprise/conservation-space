package com.sirma.itt.emf.web.event.observers;

import java.util.Iterator;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.permissions.action.ActionEvaluatedEvent;

/**
 * Observer for the {@link ActionEvaluatedEvent} which removes the {@link ActionTypeConstants#RESTORE_PERMISSIONS}.
 *
 * @author yasko
 */
@Singleton
public final class RemoveManagePermissionsActionObserver {

	/**
	 * Observers the {@link ActionEvaluatedEvent} and removes the {@link ActionTypeConstants#RESTORE_PERMISSIONS}
	 * action. The action is removed only if the event's placeholder value is not {@code null}.
	 *
	 * @param event
	 *            Event containing the action to filter.
	 */
	public void removeManagePermissionsAction(@Observes ActionEvaluatedEvent event) {
		Set<Action> actions = event.getActions();

		if (event.getPlaceholder() == null || CollectionUtils.isEmpty(actions)) {
			return;
		}

		Iterator<Action> iterator = actions.iterator();
		while (iterator.hasNext()) {
			Action action = iterator.next();
			if (ActionTypeConstants.RESTORE_PERMISSIONS.equals(action.getActionId())) {
				iterator.remove();
				break;
			}
		}
	}
}
