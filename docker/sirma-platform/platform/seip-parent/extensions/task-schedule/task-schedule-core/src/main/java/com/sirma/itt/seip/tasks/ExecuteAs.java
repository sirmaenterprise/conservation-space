/**
 *
 */
package com.sirma.itt.seip.tasks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import com.sirma.itt.seip.tasks.RunAs;

/**
 * Defines a qualifier filter for security context initialization.
 *
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface ExecuteAs {

	/**
	 * Define the security context to execute the schedule operation.
	 */
	RunAs value() default RunAs.DEFAULT;
}
