package com.sirma.itt.seip.permissions.role;

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
	 * 
	 * @return evaluator type
	 */
	@Nonbinding
	String value() default "";
}
