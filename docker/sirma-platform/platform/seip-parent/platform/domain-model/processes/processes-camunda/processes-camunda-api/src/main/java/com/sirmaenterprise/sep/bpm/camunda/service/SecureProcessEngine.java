package com.sirmaenterprise.sep.bpm.camunda.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

import org.camunda.bpm.engine.ProcessEngine;

/**
 * Annotation to secure a target that is ready to execute a {@link ProcessEngine} request.
 * 
 * @author bbanchev
 */
@Inherited
@Documented
@InterceptorBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SecureProcessEngine {
	/**
	 * Does explicitly client code needs available {@link ProcessEngine} or not
	 * 
	 * @return true if client does not explicitly requires {@link ProcessEngine}
	 */
	boolean notInitializedAccepted() default true;
}
