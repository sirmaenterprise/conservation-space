package com.sirma.itt.seip.instance.security;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.instance.security.DomainObjectsBaseRoleEvaluator;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;

/**
 * Calculates the permissions for class instance. Since there is no definition the intersection between role and status
 * is skipped and all actions provided from role extensions are returned
 *
 * @author bbanchev
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypes.CLASS)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 91)
public class ClassInstanceRoleEvaluator extends DomainObjectsBaseRoleEvaluator<ClassInstance> {

	private static final List<Class> SUPPORTED = Arrays.<Class> asList(ClassInstance.class);

	protected Class<ClassInstance> allowedClass() {
		return ClassInstance.class;
	}

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED;
	}

	@Override
	protected Pair<Role, RoleEvaluator<ClassInstance>> evaluateInternal(ClassInstance target, Resource user,
			final RoleEvaluatorRuntimeSettings settings) {
		return getAssignedPermission(target, user);
	}

	@Override
	protected Boolean filterInternal(ClassInstance target, Resource resource, Role role, Set<Action> actions) {
		return Boolean.TRUE;
	}
}
