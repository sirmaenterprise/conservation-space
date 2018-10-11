package com.sirma.itt.seip.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sirma.itt.seip.annotation.Documentation;

/**
 * Cache configuration annotation. If specified on a type then the {@link #name()} should be provided with the list of
 * required configurations. If specified on a CONSTANT field then the field value should be string and the string should
 * be the name of the required configuration if not then the {@link #name()} value will be used if present.
 *
 * @author BBonev
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface CacheConfiguration {

	/**
	 * Name of the required cache configuration. This will uniquely identify the different cache instances. Optional if
	 * used on a string constant field. If so the value of the constant will be used as a name identifier as is.
	 * 
	 * @return the cache name
	 */
	@Documentation("Name of the required cache configuration. This will uniquely identify the different cache instances.")
	String name() default "";

	/**
	 * Sets the cache transaction configurations. Default is no transaction.
	 * 
	 * @return transaction mode
	 */
	@Documentation("Sets the cache transaction configurations. Default is no transaction.")
	Transaction transaction() default @Transaction
	;

	/**
	 * Eviction configuration.
	 * 
	 * @return the eviction policy
	 */
	@Documentation("Eviction configuration.")
	Eviction eviction() default @Eviction
	;

	/**
	 * Expiration configuration.
	 * 
	 * @return the expiration policy
	 */
	@Documentation("Expiration configuration.")
	Expiration expiration() default @Expiration
	;

	/**
	 * Cache locking. Default is no locking
	 * 
	 * @return the locking strategy
	 */
	Locking locking() default @Locking
	;

	/**
	 * Some documentation for the current cache configuration
	 * 
	 * @return optional documentation
	 */
	@Documentation("Documentation for the current cache configuration")
	Documentation doc() default @Documentation("")
	;

}
