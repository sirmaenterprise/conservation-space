package com.sirma.itt.cmf.security.evaluator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleEvaluatorType;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.Role;

/**
 * Evaluator class for section instances.
 *
 * @author BBonev
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypesCmf.SECTION)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 15)
public class SectionRoleEvaluator extends BaseRoleEvaluator<SectionInstance> implements
		RoleEvaluator<SectionInstance> {
	private static final List<Class<?>> SUPPORTED = Arrays.asList(new Class<?>[] { SectionInstance.class });
	/**
	 * Evaluate internal.
	 *
	 * @param target
	 *            the target
	 * @param resource
	 *            the resource
	 * @param settings
	 *            are the runtime settings to use
	 * @return the user role
	 */
	@Override
	protected Pair<Role, RoleEvaluator<SectionInstance>> evaluateInternal(SectionInstance target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {
		// if the has calculated role we will use it
		com.sirma.itt.emf.instance.model.Instance instance = target.getOwningInstance();
		RoleEvaluator<com.sirma.itt.emf.instance.model.Instance> parentEvaluator = roleEvaluatorManagerService
				.get().getRootEvaluator(instance);
		// if not calculated yet we should call to calculate the case role first
		Pair<Role, RoleEvaluator<com.sirma.itt.emf.instance.model.Instance>> evaluate = parentEvaluator
				.evaluate(instance, resource, chainRuntimeSettings);
		return new Pair<Role, RoleEvaluator<SectionInstance>>(evaluate.getFirst(), this);

	}

	@Override
	protected Boolean filterInternal(SectionInstance target, Resource resource, Role role,
			Set<Action> actions) {
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getContainer(SectionInstance target) {
		if (target != null) {
			return target.getContainer();
		}
		return null;
	}

	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED;
	}

}
