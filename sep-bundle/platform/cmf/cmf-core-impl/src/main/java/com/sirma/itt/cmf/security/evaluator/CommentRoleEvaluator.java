package com.sirma.itt.cmf.security.evaluator;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.VIEWER;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;
import com.sirma.itt.emf.security.model.Role;

/**
 * Evaluates the permissions and actions for comment instance.
 *
 * @author bbanchev
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypes.COMMENT)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 135)
public class CommentRoleEvaluator extends BaseRoleEvaluator<CommentInstance> implements
		RoleEvaluator<CommentInstance> {
	private static final List<Class<?>> SUPPORTED = Arrays.asList(new Class<?>[] { CommentInstance.class });
	/** The case create. */
	static final Action NEW_REPLAY = new EmfAction(ActionTypeConstants.TOPIC_REPLY);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<Role, RoleEvaluator<CommentInstance>> evaluate(CommentInstance target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {
		if ((target == null) || (resource == null)) {
			return null;
		}
		Pair<Role, RoleEvaluator<CommentInstance>> role = evaluateInternal(target, resource,
				settings);
		return role;
	}

	/**
	 * Evaluate internal. If the resource is the creator - CREATOR role, otherwise either viewer or
	 * whatever is set on the topic
	 *
	 * @param target
	 *            the target
	 * @param resource
	 *            the resource to evaluate
	 * @param settings
	 *            are the runtime settings to use
	 * @return the user role
	 */
	@Override
	protected Pair<Role, RoleEvaluator<CommentInstance>> evaluateInternal(CommentInstance target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {

		// if he owns the comment
		if (resourceService.areEqual(resource, target.getFrom())) {
			return constructRoleModel(CREATOR);
		}
		TopicInstance topicInstance = target.getTopic();
		// also search for creator
		return constructRoleModel(topicInstance, resource, VIEWER, settings);
	}

	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Boolean filterInternal(CommentInstance target, Resource resource, Role role,
			Set<Action> actions) {
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<Action> getCalculatedActions(CommentInstance target, Resource resource, Role role) {
		return getAllowedActions(target, role);
	}

}
