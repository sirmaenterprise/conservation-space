package com.sirma.itt.seip.eai.cs.permissions;

import static com.sirma.itt.seip.eai.cs.EAIServicesConstants.PROPERTY_INTEGRATED_FLAG_ID;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.NO_PERMISSION;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.permissions.BaseRoleEvaluator;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.PermissionModelType;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;

/**
 * The {@link IntegratedObjectRoleEvaluator} provides role evaluation for integrated objects only that have no
 * permission model yet.
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypes.OBJECT)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 90.1)
public class IntegratedObjectRoleEvaluator extends BaseRoleEvaluator<ObjectInstance> {

	/** The Constant SUPPORTED. */
	private static final List<Class> SUPPORTED = Arrays.asList(new Class<?>[] { ObjectInstance.class });
	@Inject
	private InstancePermissionsHierarchyResolver instancePermissionsHierarchyResolver;

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED;
	}

	@Override
	public Pair<Role, RoleEvaluator<ObjectInstance>> evaluate(ObjectInstance target, Resource resource,
			RoleEvaluatorRuntimeSettings settings) {
		return evaluateInternal(target, resource, settings);
	}

	private boolean isPermissionModelUnknown(ObjectInstance target) {
		PermissionModelType model = permissionService.getPermissionModel(target.toReference());
		if (model == null || !model.isDefined()) {
			return true;
		}
		return false;
	}

	@Override
	protected Boolean filterInternal(ObjectInstance target, Resource resource, Role role, Set<Action> actions) {
		return null;// NOSONAR
	}

	@Override
	protected Pair<Role, RoleEvaluator<ObjectInstance>> evaluateInternal(ObjectInstance target, Resource user,
			RoleEvaluatorRuntimeSettings settings) {
		// if object is integrated and model is uknown check the library
		if (target.getBoolean(PROPERTY_INTEGRATED_FLAG_ID) && isPermissionModelUnknown(target)) {
			InstanceReference targetReference = target.toReference();
			InstanceReference root = instancePermissionsHierarchyResolver.getLibrary(targetReference);
			if (root != null && !targetReference.equals(root)) {
				return getAssignedPermission(root.toInstance(), user, NO_PERMISSION);
			}
		}
		return constructRoleModel(NO_PERMISSION);
	}

}
