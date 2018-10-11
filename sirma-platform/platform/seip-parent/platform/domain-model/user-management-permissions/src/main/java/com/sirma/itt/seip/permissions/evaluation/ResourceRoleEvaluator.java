package com.sirma.itt.seip.permissions.evaluation;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.VIEWER;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.permissions.BaseRoleEvaluator;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;

/**
 * Role evaluator for users
 *
 * @author BBonev
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypes.USER)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 20)
public class ResourceRoleEvaluator extends BaseRoleEvaluator<Resource> {

	private static final List<Class> SUPPORTED = Arrays.asList(new Class<?>[] { EmfUser.class, EmfGroup.class });

	private static final EmfAction UNLOCK = new EmfAction(ActionTypeConstants.UNLOCK);

	@Inject
	private LockService lockService;

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED;
	}

	@Override
	protected Pair<Role, RoleEvaluator<Resource>> evaluateInternal(Resource target, Resource user,
			RoleEvaluatorRuntimeSettings settings) {
		return getAssignedPermission(target, user, VIEWER);
	}

	@Override
	protected Boolean filterInternal(Resource target, Resource resource, Role role, Set<Action> actions) {
		LockInfo lockStatus = lockService.lockStatus(target.toReference());
		if (!lockStatus.isLockedByMe()) {
			actions.remove(UNLOCK);
		}

		return Boolean.FALSE;
	}

}
