package com.sirma.itt.emf.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ejb.TransactionAttributeType;
import javax.inject.Qualifier;

/**
 * Defines a transaction mode of a custom beans.
 * 
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface TransactionMode {

	/**
	 * The current transaction mode. Default value is {@link TransactionAttributeType#REQUIRED}
	 */
	TransactionAttributeType value() default TransactionAttributeType.REQUIRED;
}
