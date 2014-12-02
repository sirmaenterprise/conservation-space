package com.sirma.itt.cmf.security.evaluator;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.VIEWER;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.SecurityModel.BaseRoles;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.Role;

/**
 * Implementation for links role evaluations
 *
 * @author bbanchev
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypesCmf.LINK)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 140)
public class RelationRoleEvaluator extends BaseRoleEvaluator<Instance> implements
		RoleEvaluator<Instance> {
	private static final List<Class<?>> SUPPORTED = Arrays.asList(new Class<?>[] {
			LinkInstance.class, LinkReference.class });
	/**
	 * {@inheritDoc}
	 */
	protected Class<LinkInstance> allowedClass() {
		return LinkInstance.class;
	}

	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pair<Role, RoleEvaluator<Instance>> evaluateInternal(Instance target,
			Resource user, final RoleEvaluatorRuntimeSettings settings) {
		// if link is deleted we cannot do anything
		if (isInstanceInStates(target, PrimaryStates.DELETED)) {
			return constructRoleModel(VIEWER);
		}
		// if he owns the object
		Map<String, Serializable> properties = target.getProperties();
		Serializable createdBy = properties.get(DefaultProperties.CREATED_BY);
		// if the relations is created by the system then the user role is always VIEWER
		if (areUsersEqual(createdBy, SecurityContextManager.getSystemUser())) {
			return constructRoleModel(BaseRoles.VIEWER);
		}
		// check if the provided user is creator
		if (areUsersEqual(createdBy, user)) {
			if (isRoleIrrelevant(settings, CREATOR)) {
				return constructRoleModel(CREATOR);
			}
		}
		Instance sourceInstance = null;
		if (target instanceof LinkInstance) {
			Instance from = ((LinkInstance) target).getFrom();
			if (from != null) {
				sourceInstance = InstanceUtil.getContext(from, true);
			}
		} else if (target instanceof LinkReference) {
			InstanceReference reference = ((LinkReference) target).getFrom();
			if (reference != null) {
				sourceInstance = InstanceUtil.getContext(reference.toInstance(), true);
			}
		}
		if (sourceInstance != null) {
			return constructRoleModel(sourceInstance, user, BaseRoles.CONSUMER, chainRuntimeSettings);
		}
		return constructRoleModel(BaseRoles.VIEWER);
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
	private boolean areUsersEqual(Serializable property, Resource targetUser) {
		return resourceService.areEqual(property, targetUser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Boolean filterInternal(Instance target, Resource resource, Role role,
			Set<Action> actions) {
		return Boolean.FALSE;
	}

}
