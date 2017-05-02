package com.sirma.itt.emf.audit.observer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

/**
 * Annotation used on methods, types which will perform operations only if the audit log is enabled. Use this if you are
 * going to observe any audit-related events.
 * <p>
 * This should be used only on observer methods or on void methods.
 *
 * @author nvelkov
 */
@InterceptorBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
	// nothing to add
}
