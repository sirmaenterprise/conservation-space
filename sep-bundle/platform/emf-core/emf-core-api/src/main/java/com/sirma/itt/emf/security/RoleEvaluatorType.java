package com.sirma.itt.emf.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Defines a qualifier for role evaluator
 *
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE })
public @interface RoleEvaluatorType {

	/**
	 * Returns the concrete type of the evaluator
	 */
	@Nonbinding
	String value() default "";

	/**
	 * Scope of the evaluator. The default scope is
	 * {@link EvaluatorScope#INTERNAL}
	 */
	EvaluatorScope scope() default EvaluatorScope.INTERNAL;

}
