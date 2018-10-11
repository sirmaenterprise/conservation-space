package com.sirma.itt.seip.instance.security;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.VIEWER;

import java.io.Serializable;
import java.util.Set;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.permissions.BaseRoleEvaluator;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.seip.resources.Resource;

/**
 * Abstract domain objects evaluator with reusable methods
 *
 * @param <E>
 *            is the instance type
 * @author bbanchev
 */
public abstract class DomainObjectsBaseRoleEvaluator<E extends Instance> extends BaseRoleEvaluator<E> {

	/**
	 * Some predefined action constants to be used when filtering actions
	 */
	protected static final Action RESTORE_PERMISSIONS = new EmfAction(ActionTypeConstants.RESTORE_PERMISSIONS);

	/**
	 * Filter restore permission only for managers on root level
	 *
	 * @param target
	 *            is the instance
	 * @param resource
	 *            is the user or group
	 * @param role
	 *            is the current role
	 * @param actions
	 *            the actions to remove from
	 */
	protected void filterRestorePermissions(Instance target, Resource resource, Role role, Set<Action> actions) {
		if (role.getRoleId() == SecurityModel.BaseRoles.MANAGER && !isRootManager(target, resource)) {
			actions.remove(RESTORE_PERMISSIONS);
		}
	}

	@Override
	protected Pair<Role, RoleEvaluator<E>> evaluateInternal(E target, Resource user,
			final RoleEvaluatorRuntimeSettings settings) {
		// if object is deleted we cannot do anything
		if (isDeleted(target)) {
			return constructRoleModel(VIEWER);
		}

		// version instances should have same permissions as the current instance
		Instance instance = getCurrentInstanceIfVersion(target);
		return getAssignedPermission(instance, user);
	}

	/**
	 * Retrieves current instance, if the target is a version.
	 */
	private Instance getCurrentInstanceIfVersion(E target) {
		Serializable id = target.getId();
		if (InstanceVersionService.isVersion(id)) {
			// load deleted as well, they should be shown in other versions
			return instanceService.loadDeleted(InstanceVersionService.getIdFromVersionId(id)).orElseThrow(
					() -> new EmfRuntimeException("The requested instance [id=" + id + "] does not exist."));
		}

		return target;
	}
}
