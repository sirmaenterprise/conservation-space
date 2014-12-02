/*
 *
 */
package com.sirma.itt.pm.security.evaluator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.resources.model.ResourceRole;
import com.sirma.itt.emf.security.EvaluatorScope;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.domain.ObjectTypesPm;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Evaluator class to handle {@link ProjectInstance} action permissions and user role evaluation.
 * 
 * @author BBonev
 */
@ApplicationScoped
@RoleEvaluatorType(value = ObjectTypesPm.PROJECT, scope = EvaluatorScope.INTERNAL)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 120)
public class ProjectRoleEvaluator extends BaseRoleEvaluator<ProjectInstance> {

	private static final List<Class<?>> SUPPORTED = Arrays
			.asList(new Class<?>[] { ProjectInstance.class });
	/** The resource service. */
	@Inject
	private ResourceService resourceService;
	@Inject
	private LinkService linkService;

	/** The case create. */
	private static final Action DELETE = new EmfAction(ActionTypeConstants.DELETE);
	private static final Action COMPLETE = new EmfAction(ActionTypeConstants.COMPLETE);

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>NOTE:</b>The method overrides default implementation to stop chain propagating.
	 */
	@Override
	public Pair<Role, RoleEvaluator<ProjectInstance>> evaluate(ProjectInstance target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {
		if (target == null) {
			return null;
		}
		if (isAdminOrSystemUser(resource)) {
			return constructRoleModel(SecurityModel.BaseRoles.ADMINISTRATOR);
		}
		// no need of chain currently
		Pair<Role, RoleEvaluator<ProjectInstance>> role = evaluateInternal(target, resource,
				settings);
		return role;
	}

	@Override
	public Boolean filterInternal(ProjectInstance target, Resource user, Role role,
			Set<Action> actions) {
		if (actions.isEmpty()) {
			return Boolean.FALSE;
		}

		if (!isInstanceInStates(target, PrimaryStates.SUBMITTED, PrimaryStates.DELETED)) {
			actions.remove(DELETE);
		}

		if (actions.contains(COMPLETE)) {
			boolean hasActive = false;
			// gets all children of the current project on the first level
			List<LinkReference> links = linkService.getSimpleLinksTo(target.toReference(),
					LinkConstants.PART_OF_URI);
			for (LinkReference childRef : links) {
				Instance child = childRef.getFrom().toInstance();
				// if not final state
				if (((child instanceof AbstractTaskInstance) || (child instanceof CaseInstance) || (child instanceof WorkflowInstanceContext))
						&& !isInstanceInStates(child, PrimaryStates.CANCELED,
								PrimaryStates.COMPLETED, PrimaryStates.DELETED)) {
					hasActive = true;
					break;
				}
			}
			if (hasActive) {
				actions.remove(COMPLETE);
			}
		}

		// any active children check

		return Boolean.TRUE;
	}

	@Override
	protected Pair<Role, RoleEvaluator<ProjectInstance>> evaluateInternal(ProjectInstance target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {

		ResourceRole resourceRole = resourceService.getResourceRole(target, resource);
		if (resourceRole != null) {
			return constructRoleModel(resourceRole.getRole());
		}
		// check visibility to decide is viewer or at least consumer
		if (ProjectProperties.Visbility.PUBLIC.getName().equals(
				target.getProperties().get(ProjectProperties.VISIBILITY))) {
			return constructRoleModel(SecurityModel.BaseRoles.CONSUMER);
		}
		return constructRoleModel(SecurityModel.BaseRoles.VIEWER);
	}

	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}
}
