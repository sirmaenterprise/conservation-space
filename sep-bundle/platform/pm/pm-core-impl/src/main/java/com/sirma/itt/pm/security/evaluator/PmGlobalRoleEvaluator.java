package com.sirma.itt.pm.security.evaluator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.SecurityModel.BaseRoles;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * The PmGlobalRoleEvaluator is supportable evaluator, that calculates the role in project as
 * resource allocation for any instance in it. Overrides dms extension as data might be in collision
 *
 * @author bbanchev
 */
@Extension(target = RoleEvaluator.TARGET_NAME, order = 200, priority = 1)
@ApplicationScoped
public class PmGlobalRoleEvaluator extends BaseRoleEvaluator<Instance> {
	/** The logger. */
	@Inject
	private Logger logger;
	/** The trace. */
	private boolean trace;

	private static final List<Class<?>> SUPPORTED = Arrays
			.asList(new Class<?>[] { Instance.class });
	/** The resource service. */
	@Inject
	protected ResourceService resourceService;

	/**
	 * Initialize the evaluator.
	 */
	@PostConstruct
	protected void initialize() {
		trace = logger.isTraceEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pair<Role, RoleEvaluator<Instance>> evaluateInternal(Instance target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {
		Instance rootInstance = null;
		if (target instanceof ProjectInstance) {
			rootInstance = target;
		} else {
			rootInstance = InstanceUtil.getRootInstance(target, true);
			if (target.equals(rootInstance)) {
				if (logger.isDebugEnabled()) {
					logger.error("For target: "
							+ (target != null ? target.getId() : "null")
							+ " root is ("
							+ (rootInstance != null ? rootInstance.getId() + "/"
									+ rootInstance.getClass().getName() + ")" : "null")
							+ "! Return viewer as default role!");
					return constructRoleModel(BaseRoles.VIEWER);
				}
			}
		}
		if (trace) {
			logger.trace("For target: "
					+ (target != null ? target.getId() : "null")
					+ " root is ("
					+ (rootInstance != null ? rootInstance.getId() + "/"
							+ rootInstance.getClass().getName() + ")" : "null"));
		}
		// get the role in the project
		return constructRoleModel(rootInstance, resource, BaseRoles.VIEWER, chainRuntimeSettings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Boolean filterInternal(Instance target, Resource resource, Role role,
			Set<Action> actions) {
		Instance project = null;
		if (target instanceof ProjectInstance) {
			project = target;
		} else {
			project = InstanceUtil.getRootInstance(target, true);
		}
		if (isInstanceInStates(project, PrimaryStates.DELETED, PrimaryStates.CANCELED,
				PrimaryStates.COMPLETED)) {
			actions.clear();
			return Boolean.TRUE;
		}
		// no knowledge what to do
		return null;
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
	public boolean canHandle(Object target) {
		return true;
	}
}
