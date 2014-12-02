package com.sirma.itt.cmf.security.evaluator;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.VIEWER;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;
import com.sirma.itt.emf.security.model.Role;

/**
 * Evaluates the permissions and actions for topic instance.
 *
 * @author bbanchev
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypes.TOPIC)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 130)
public class TopicRoleEvaluator extends BaseRoleEvaluator<TopicInstance> implements
		RoleEvaluator<TopicInstance> {
	private static final List<Class<?>> SUPPORTED = Arrays
			.asList(new Class<?>[] { TopicInstance.class });
	/** The case create. */
	static final Action DELETE_TOPIC = new EmfAction(ActionTypeConstants.STOP);

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/**
	 * {@inheritDoc}<br>
	 * Override the method since topic is not linked as owned in project/case/etc...
	 */
	@Override
	public Pair<Role, RoleEvaluator<TopicInstance>> evaluate(TopicInstance target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {
		if ((target == null) || (resource == null)) {
			return null;
		}
		if (isAdminOrSystemUser(resource)) {
			return constructRoleModel(SecurityModel.BaseRoles.ADMINISTRATOR);
		}
		Pair<Role, RoleEvaluator<TopicInstance>> role = evaluateInternal(target, resource, settings);
		return role;
	}

	/**
	 * Evaluate internal. If the resource is the creator - CREATOR role, otherwise either viewer or
	 * whatever is set on the instance topic is attached to.
	 *
	 * @param topic
	 *            the target
	 * @param resource
	 *            the resource to evaluate
	 * @param settings
	 *            are the runtime settings to use
	 * @return the user role
	 */
	@Override
	protected Pair<Role, RoleEvaluator<TopicInstance>> evaluateInternal(TopicInstance topic,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {

		// if he owns the topic
		if (resourceService.areEqual(resource, topic.getFrom())) {
			if (isRoleIrrelevant(settings, CREATOR)) {
				return constructRoleModel(CREATOR);
			}
		}
		// if there is permission for the parent
		InstanceReference owning = topic.getTopicAbout();
		InitializedInstance converted = typeConverter.convert(InitializedInstance.class, owning);
		Instance instance = converted.getInstance();
		if ((instance == null) || isInstanceInStates(instance, PrimaryStates.DELETED)) {
			return constructRoleModel(VIEWER);
		}
		// check the permissions over the commenented instance
		return constructRoleModel(instance, resource, VIEWER, chainRuntimeSettings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Boolean filterInternal(TopicInstance target, Resource resource, Role role,
			Set<Action> actions) {
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<Action> getCalculatedActions(TopicInstance target, Resource resource, Role role) {
		return getAllowedActions(target, role);
	}
}
