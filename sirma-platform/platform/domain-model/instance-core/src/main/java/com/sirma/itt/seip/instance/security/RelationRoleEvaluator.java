package com.sirma.itt.seip.instance.security;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.VIEWER;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.permissions.BaseRoleEvaluator;
import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Implementation for links role evaluations
 *
 * @author bbanchev
 */
@Singleton
@RoleEvaluatorType(ObjectTypes.LINK)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 140)
public class RelationRoleEvaluator extends BaseRoleEvaluator<Instance> {
	private static final List<Class> SUPPORTED = Arrays
			.asList(new Class<?>[] { LinkInstance.class, LinkReference.class });

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private DatabaseIdManager idManager;

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED;
	}

	@Override
	public Pair<Role, RoleEvaluator<Instance>> evaluate(Instance target, Resource resource,
			RoleEvaluatorRuntimeSettings settings) {
		if (target.getId() == null) {
			return constructRoleModel(VIEWER);
		}
		return super.evaluate(target, resource, settings);
	}

	@Override
	protected Pair<Role, RoleEvaluator<Instance>> evaluateInternal(Instance target, Resource user,
			final RoleEvaluatorRuntimeSettings settings) {
		// if link is deleted we cannot do anything
		if (isInstanceInStates(target, PrimaryStates.DELETED)) {
			return constructRoleModel(VIEWER);
		}
		// if he owns the object
		Serializable createdBy = target.get(DefaultProperties.CREATED_BY);
		// if the relations is created by the system then the user role is always VIEWER
		if (areUsersEqual(createdBy, securityContextManager.getSystemUser())) {
			return constructRoleModel(BaseRoles.VIEWER);
		}

		if (areUsersEqual(createdBy, user)) {
			return constructRoleModel(BaseRoles.CREATOR);
		}

		Instance sourceInstance = getSourceInstance(target);
		// this one is special case when instance is in the process of creation and we are adding relations to it in
		// that case the role probably will not be present and we are creating the relation so we should have
		// permissions for it
		if (!idManager.isPersisted(sourceInstance)) {
			// instance is null or not persisted
			return constructRoleModel(BaseRoles.CREATOR);
		}
		// the source instance here is non null and persisted - we can check user role
		ResourceRole resourceRole = permissionService.getPermissionAssignment(sourceInstance.toReference(),
				user.getId());
		if (resourceRole != null) {
			return constructRoleModel(sourceInstance, user, resourceRole.getRole(), chainRuntimeSettings);
		}
		return constructRoleModel(BaseRoles.VIEWER);
	}

	/**
	 * Method for finding the source of a target instance.
	 *
	 * @param target
	 *            the instance we want to find the source.
	 * @return the source instance.
	 */
	private static Instance getSourceInstance(Instance target) {
		Instance sourceInstance = null;
		if (target instanceof LinkInstance) {
			Instance from = ((LinkInstance) target).getFrom();
			if (from != null) {
				sourceInstance = from;
			}
		} else if (target instanceof LinkReference) {
			InstanceReference reference = ((LinkReference) target).getFrom();
			if (reference != null) {
				sourceInstance = reference.toInstance();
			}
		}
		return sourceInstance;
	}

	/**
	 * Are users equal.
	 *
	 * @param property
	 *            the property
	 * @param targetUser
	 *            the target user
	 * @return true, if successful
	 */
	private boolean areUsersEqual(Serializable property, Serializable targetUser) {
		return resourceService.areEqual(property, targetUser);
	}

	@Override
	protected Boolean filterInternal(Instance target, Resource resource, Role role, Set<Action> actions) {
		return Boolean.FALSE;
	}

}
