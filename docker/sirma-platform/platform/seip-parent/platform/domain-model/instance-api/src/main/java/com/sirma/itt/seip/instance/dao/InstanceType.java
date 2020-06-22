package com.sirma.itt.seip.instance.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Defines a instance type qualifier for identifying different service implementations for different instance types.
 *
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER })
public @interface InstanceType {

	/**
	 * Type of the instance to identify.
	 * 
	 * @return instance category
	 */
	String type();
}
