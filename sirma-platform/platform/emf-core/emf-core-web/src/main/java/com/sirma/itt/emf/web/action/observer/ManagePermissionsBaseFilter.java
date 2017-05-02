package com.sirma.itt.emf.web.action.observer;

import java.util.Iterator;
import java.util.Set;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;

/**
 * Base filter implementation for removing the managePermissions action from action menus.
 *
 * @author yasko
 */
public class ManagePermissionsBaseFilter {

	/**
	 * Removes {@link ActionTypeConstants#MANAGE_PERMISSIONS} action from a set of actions.
	 *
	 * @param actions
	 *            Set of action to filter.
	 */
	public final void removeManagePermissionsAction(Set<Action> actions) {
		if (CollectionUtils.isEmpty(actions)) {
			return;
		}

		Iterator<Action> iterator = actions.iterator();
		while (iterator.hasNext()) {
			Action action = iterator.next();
			if (ActionTypeConstants.MANAGE_PERMISSIONS.equals(action.getActionId())) {
				iterator.remove();
				break;
			}
		}
	}
}
