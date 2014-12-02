package com.sirma.itt.cmf.security.evaluator;

import static com.sirma.itt.emf.security.SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.cmf.constants.allowed_action.AllowedActionType;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.emf.domain.Pair;
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
import com.sirma.itt.emf.security.model.RoleIdentifier;

/**
 * Evaluator implementation for tasks instances.
 *
 * @author BBonev
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypesCmf.WORKFLOW_TASK)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 50)
public class TaskRoleEvaluator extends BaseRoleEvaluator<TaskInstance> implements
		RoleEvaluator<TaskInstance> {
	private static final List<Class<?>> SUPPORTED = Arrays
			.asList(new Class<?>[] { TaskInstance.class });
	/** The reassign. */
	static final Action REASSIGN = new EmfAction(AllowedActionType.REASSIGN_TASK.getType());
	/** The start progress. */
	static final Action START_PROGRESS = new EmfAction(
			AllowedActionType.TASK_START_PROGRESS.getType());

	/** The Constant SUBTASK_CREATE. */
	static final Action SUBTASK_CREATE = new EmfAction(AllowedActionType.SUBTASK_CREATE.getType());
	/** The hold. */
	static final Action HOLD = new EmfAction(AllowedActionType.TASK_HOLD.getType());
	/** The claim. */
	static final Action CLAIM = new EmfAction(AllowedActionType.TASK_CLAIM.getType());
	private static final List<Action> CLAIMABLE = Collections.singletonList(CLAIM);
	/** The release. */
	static final Action RELEASE = new EmfAction(AllowedActionType.TASK_RELEASE.getType());
	static final Action CANCEL = new EmfAction(AllowedActionType.STOP.getType());
	/** Standalone task edit. */
	public static final Action EDIT = new EmfAction(AllowedActionType.EDIT_TASK.getType());

	/** The Constant preferredRoles. */
	private static final RoleIdentifier[] PREFERRED_ROLES = new RoleIdentifier[] {
			SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE, SecurityModel.ActivitiRoles.ASSIGNEE };

	private static final Logger LOGGER = Logger.getLogger(TaskRoleEvaluator.class);

	/** The task service. */
	@Inject
	private TaskService taskService;

	@Override
	protected RoleIdentifier[] getPreferredHighPriorityRoles() {
		return PREFERRED_ROLES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<Role, RoleEvaluator<TaskInstance>> evaluate(TaskInstance target, Resource resource,
			RoleEvaluatorRuntimeSettings settings) {
		if ((target == null) || (resource == null)) {
			return null;
		}
		Pair<Role, RoleEvaluator<TaskInstance>> role = evaluateInternal(target, resource, settings);
		Pair<Role, RoleEvaluator<TaskInstance>> highestRole = calculateHighestRoleFromChain(target,
				resource, role, settings, getPreferredHighPriorityRoles());
		return highestRole;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pair<Role, RoleEvaluator<TaskInstance>> evaluateInternal(TaskInstance target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {
		Map<String, Serializable> properties = target.getProperties();
		com.sirma.itt.emf.instance.model.Instance parent = target.getContext();
		if (parent == null) {
			parent = target.getOwningInstance();
		}
		// if he owns the task
		if (resourceService.areEqual(resource, properties.get(TaskProperties.TASK_OWNER))) {
			return constructRoleModel(SecurityModel.ActivitiRoles.ASSIGNEE);
		}
		if (parent == null) {
			LOGGER.warn("Passed task instance without parent context: " + target);
		} else if (resourceService.areEqual(resource,
				parent.getProperties().get(WorkflowProperties.CREATED_BY))
				|| resourceService.areEqual(resource,
						parent.getProperties().get(WorkflowProperties.STARTED_BY))) {
			// if the task is although pooled to the creator
			if (taskService.isClaimable(target, resource.getIdentifier())) {
				return constructRoleModel(POSSIBLE_ASSIGNEE);
			}
			return constructRoleModel(CREATOR);
		}

		if (taskService.isReleasable(target, resource.getIdentifier())) {
			return constructRoleModel(SecurityModel.ActivitiRoles.ASSIGNEE);
		}
		if (taskService.isClaimable(target, resource.getIdentifier())) {
			return constructRoleModel(POSSIBLE_ASSIGNEE);
		}
		// as final check if it admin
		if (isAdminOrSystemUser(resource)) {
			return constructRoleModel(SecurityModel.BaseRoles.ADMINISTRATOR);
		}
		return constructRoleModel(CONSUMER);
	}

	@Override
	protected Boolean filterInternal(TaskInstance target, Resource resource, Role role,
			Set<Action> actions) {

		Map<String, Serializable> properties = target.getProperties();

		Serializable owner = properties.get(TaskProperties.TASK_OWNER);
		if (SecurityModel.ActivitiRoles.POSSIBLE_ASSIGNEE.equals(role.getRoleId())
				|| (owner == null)) {
			actions.retainAll(CLAIMABLE);
			return Boolean.FALSE;
		}
		actions.remove(CLAIM);

		// assignee role
		String resourceId = resource.getIdentifier();
		if (resourceId.equals(owner)) {
			actions.remove(CANCEL);
			// REVIEW: this state checks should be removed
			if (!taskService.isReleasable(target, resourceId)) {
				actions.remove(RELEASE);
			}
		} else {
			actions.remove(START_PROGRESS);
			actions.remove(RELEASE);
		}
		// if the action survived till now lets check if there is something to create at all
		if ((actions.contains(SUBTASK_CREATE) && !instanceService.isChildAllowed(target,
				ObjectTypesCmf.STANDALONE_TASK))) {
			actions.remove(SUBTASK_CREATE);
		}

		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getContainer(TaskInstance target) {
		if (target.getContext() != null) {
			return target.getContext().getContainer();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Class<TaskInstance> allowedClass() {
		return TaskInstance.class;
	}

	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}

}
