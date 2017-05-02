/**
 *
 */
package com.sirma.itt.seip.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

import com.sirma.itt.seip.security.util.SecureEvent;

/**
 * Secure interceptor that may initialize automatically the security context for methods that accept {@link SecureEvent}
 * . This is useful for asynchronous event observers.
 *
 * @author BBonev
 */
@Inherited
@Documented
@InterceptorBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureObserver {
	// nothing to define
}
