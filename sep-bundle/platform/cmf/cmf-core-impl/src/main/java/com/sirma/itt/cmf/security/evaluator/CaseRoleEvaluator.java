package com.sirma.itt.cmf.security.evaluator;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.COLLABORATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.VIEWER;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;
import com.sirma.itt.emf.security.model.Role;

/**
 * Predefines an interface for case role evaluator.
 *
 * @author BBonev
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypesCmf.CASE)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 105)
public class CaseRoleEvaluator extends BaseRoleEvaluator<CaseInstance> implements
		RoleEvaluator<CaseInstance> {

	private static final List<Class<?>> SUPPORTED = Arrays.asList(new Class<?>[] { CaseInstance.class });

	/** The case create. */
	static final Action CASE_CREATE = new EmfAction(ActionTypeConstants.CREATE_CASE);

	/** The Constant START_WORKFLOW. */
	static final Action START_WORKFLOW = new EmfAction(ActionTypeConstants.START_WORKFLOW);

	/** The Constant STANDALONE_TASK_CREATE. */
	static final Action CREATE_TASK = new EmfAction(ActionTypeConstants.CREATE_TASK);

	/** The workflow service. */
	@Inject
	private TaskService taskService;

	/**
	 * Evaluate internal.
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
	protected Pair<Role, RoleEvaluator<CaseInstance>> evaluateInternal(CaseInstance target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {

		// if case is deleted we cannot do anything
		if (isInstanceInStates(target, PrimaryStates.DELETED)) {
			return constructRoleModel(VIEWER);
		}

		Map<String, Serializable> properties = target.getProperties();

		// if he owns the case
		if (resourceService.areEqual(resource, properties.get(DefaultProperties.CREATED_BY))) {
			if (isRoleIrrelevant(settings, CREATOR)) {
				// contributor on its case == collaborator
				return constructRoleModel(CREATOR);
			}
		}
		// or has/had a task assigned to him
		if (taskService.hasUserTasks(target, resource.getIdentifier(), TaskState.IN_PROGRESS)) {
			return constructRoleModel(COLLABORATOR);
		} else if (taskService.hasUserTasks(target, resource.getIdentifier(), TaskState.COMPLETED)) {
			return constructRoleModel(CONSUMER);
		} else if (taskService.hasUserPooledTasks(target, resource.getIdentifier(),
				TaskState.IN_PROGRESS)) {
			return constructRoleModel(CONSUMER);
		}
		// go to next evaluator - dms or some higher module
		return constructRoleModel(VIEWER);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getContainer(CaseInstance target) {
		return target.getContainer();
	}

	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}

	@Override
	protected Boolean filterInternal(CaseInstance target, Resource resource, Role role,
			Set<Action> actions) {
		// TODO: add temporary calculated cached value to minimize the calls
		if (actions.contains(START_WORKFLOW)
				&& !instanceService.isChildAllowed(target, ObjectTypesCmf.WORKFLOW)) {
			actions.remove(START_WORKFLOW);
		}
		if (actions.contains(CREATE_TASK)
				&& !instanceService.isChildAllowed(target, ObjectTypesCmf.STANDALONE_TASK)) {
			actions.remove(CREATE_TASK);
		}
		return Boolean.FALSE;
	}

	@Override
	protected Set<Action> getCalculatedActions(CaseInstance target, Resource resource, Role role) {
		return getAllowedActions(target, role);
	}

}
