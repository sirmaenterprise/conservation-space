package com.sirma.itt.seip.annotations.state;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.COLLABORATOR;
import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.CONSUMER;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.permissions.BaseRoleEvaluator;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;

/**
 * The AnnotationRoleEvaluator provides role evaluation and actions filtering .
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypes.OBJECT)
@Extension(target = RoleEvaluator.TARGET_NAME)
public class AnnotationRoleEvaluator extends BaseRoleEvaluator<Annotation> {

	/** The Constant SUPPORTED. */
	private static final List<Class> SUPPORTED = Arrays.asList(new Class<?>[] { Annotation.class });

	/**
	 * Some predefined action constants to be used when filtering actions
	 */
	protected static final Action REPLY_COMMENT = new EmfAction(ActionTypeConstants.REPLY_COMMENT);
	protected static final Action SUSPEND_COMMENT = new EmfAction(ActionTypeConstants.SUSPEND_COMMENT);
	protected static final Action RESTART_COMMENT = new EmfAction(ActionTypeConstants.RESTART_COMMENT);
	protected static final Action EDIT_COMMENT = new EmfAction(ActionTypeConstants.EDIT_COMMENT);

	private static final Set<Action> ANNOTATION_ACTIONS_REMOVED_IN_REPLY = new HashSet<>(
			Arrays.asList(REPLY_COMMENT, SUSPEND_COMMENT, RESTART_COMMENT));

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED;
	}

	@Override
	protected Boolean filterInternal(Annotation target, Resource resource, Role role, Set<Action> actions) {
		if (target.isReply()) {
			actions.removeAll(ANNOTATION_ACTIONS_REMOVED_IN_REPLY);
			if (target.getTopic() != null && areUsersEqual(target.getTopic().getCreatedBy(), resource)
					&& !areUsersEqual(target.getCreatedBy(), resource)) {
				actions.remove(EDIT_COMMENT);
			}
		}
		return Boolean.TRUE;
	}

	@Override
	protected Pair<Role, RoleEvaluator<Annotation>> evaluateInternal(Annotation target, Resource user,
			RoleEvaluatorRuntimeSettings settings) {

		if (areUsersEqual(target.getCreatedBy(), user)
				|| (target.getTopic() != null && areUsersEqual(target.getTopic().getCreatedBy(), user))) {
			return constructRoleModel(COLLABORATOR);
		}
		return constructRoleModel(CONSUMER);
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
	public boolean areUsersEqual(Serializable property, Serializable targetUser) {
		return resourceService.areEqual(property, targetUser);
	}

}
