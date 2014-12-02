package com.sirma.itt.cmf.security.evaluator;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.VIEWER;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.forum.model.ImageAnnotation;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.Role;

/**
 * Evaluates the permissions and actions for image annotation instance.
 * 
 * @author BBonev
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypes.IMAGE_ANNOTATION)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 137)
public class ImageAnnoationRoleEvaluator extends BaseRoleEvaluator<ImageAnnotation> implements
		RoleEvaluator<ImageAnnotation> {

	private static final List<Class<?>> SUPPORTED = Arrays
			.asList(new Class<?>[] { ImageAnnotation.class });

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<Role, RoleEvaluator<ImageAnnotation>> evaluate(ImageAnnotation target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {
		if ((target == null) || (resource == null)) {
			return null;
		}
		Pair<Role, RoleEvaluator<ImageAnnotation>> role = evaluateInternal(target, resource,
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
	protected Pair<Role, RoleEvaluator<ImageAnnotation>> evaluateInternal(ImageAnnotation target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {

		// if he owns the comment
		TopicInstance topic = target.getTopic();
		if ((topic != null) && resource.getIdentifier().equals(topic.getFrom())) {
			return constructRoleModel(CREATOR);
		}
		// also search for creator
		return constructRoleModel(topic, resource, VIEWER, settings);
	}

	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Boolean filterInternal(ImageAnnotation target, Resource resource, Role role,
			Set<Action> actions) {
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<Action> getCalculatedActions(ImageAnnotation target, Resource resource, Role role) {
		return getAllowedActions(target, role);
	}

}
