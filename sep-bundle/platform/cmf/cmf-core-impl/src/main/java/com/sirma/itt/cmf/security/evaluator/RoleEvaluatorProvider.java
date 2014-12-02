package com.sirma.itt.cmf.security.evaluator;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.enterprise.inject.spi.InjectionPoint;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.EvaluatorScope;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorType;

/**
 * Producer for concrete internal or external {@link RoleEvaluator} implementations.
 *
 * @author BBonev
 */
// @ApplicationScoped
public class RoleEvaluatorProvider {

	/** The role evaluators. */
	// @Inject
	// @Any
	private javax.enterprise.inject.Instance<RoleEvaluator<Instance>> roleEvaluators;

	/** The mapping. */
	private Map<String, RoleEvaluator<Instance>> mapping;

	/**
	 * Initialize the evaluator cache.
	 */
	// @PostConstruct
	public void initCache() {
		mapping = new LinkedHashMap<String, RoleEvaluator<Instance>>(25);
		// cache internal and external evaluators
		for (RoleEvaluator<Instance> evaluator : roleEvaluators) {
			RoleEvaluatorType annotation = evaluator.getClass().getAnnotation(
					RoleEvaluatorType.class);
			if (annotation == null) {
				// throw new CmfConfigurationException(RoleEvaluator.class
				// + " implementations must be annotated with " + RoleEvaluatorType.class);
				System.out.println(RoleEvaluator.class + " implementations must be annotated with "
						+ RoleEvaluatorType.class + " -> " + evaluator.getClass());
			} else {
				mapping.put(annotation.scope() + annotation.value(), evaluator);
			}
		}
	}

	/**
	 * Produce internal evaluator.
	 * 
	 * @param p
	 *            the injection point
	 * @return the role evaluator
	 */
	// @Produces
	// @RoleEvaluatorType(scope = EvaluatorScope.INTERNAL)
	public RoleEvaluator<Instance> produceInternalEvaluator(InjectionPoint p) {
		RoleEvaluatorType annotation = p.getAnnotated().getAnnotation(RoleEvaluatorType.class);
		String type = annotation.value();
		return mapping.get(EvaluatorScope.INTERNAL + type);
	}

	/**
	 * Produce external evaluator.
	 * 
	 * @param p
	 *            the injection point
	 * @return the role evaluator
	 */
	// @Produces
	// @RoleEvaluatorType(scope = EvaluatorScope.EXTERNAL)
	public RoleEvaluator<Instance> produceExternalEvaluator(InjectionPoint p) {
		RoleEvaluatorType annotation = p.getAnnotated().getAnnotation(RoleEvaluatorType.class);
		String type = annotation.value();
		return mapping.get(EvaluatorScope.EXTERNAL + type);
	}
}
