package com.sirma.itt.cmf.security.evaluator;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.Role;

/**
 * Evaluator implementation for workflow instances.
 *
 * @author BBonev
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypesCmf.WORKFLOW)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 60)
public class WorkflowRoleEvaluator extends BaseRoleEvaluator<WorkflowInstanceContext> implements
		RoleEvaluator<WorkflowInstanceContext> {
	private static final List<Class<?>> SUPPORTED = Arrays.asList(new Class<?>[] { WorkflowInstanceContext.class });
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<Role, RoleEvaluator<WorkflowInstanceContext>> evaluate(
			WorkflowInstanceContext target, Resource resource,
			final RoleEvaluatorRuntimeSettings settings) {
		if ((target == null) || (resource == null)) {
			return null;
		}
		if (isAdminOrSystemUser(resource)) {
			return constructRoleModel(SecurityModel.BaseRoles.ADMINISTRATOR);
		}
		return evaluateInternal(target, resource, settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pair<Role, RoleEvaluator<WorkflowInstanceContext>> evaluateInternal(
			WorkflowInstanceContext target, Resource resource,
			final RoleEvaluatorRuntimeSettings settings) {
		Map<String, Serializable> properties = target.getProperties();
		if (resourceService.areEqual(resource, properties.get(TaskProperties.CREATED_BY))
				|| resourceService.areEqual(resource, properties.get(TaskProperties.START_BY))) {
			if (isRoleIrrelevant(settings, SecurityModel.BaseRoles.CREATOR)) {
				return constructRoleModel(SecurityModel.BaseRoles.CREATOR);
			}
		}
		return constructRoleModel(CONSUMER);
	}

	@Override
	protected Boolean filterInternal(WorkflowInstanceContext target, Resource resource, Role role,
			Set<Action> actions) {
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getContainer(WorkflowInstanceContext target) {
		return target.getContainer();
	}

	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}

}
