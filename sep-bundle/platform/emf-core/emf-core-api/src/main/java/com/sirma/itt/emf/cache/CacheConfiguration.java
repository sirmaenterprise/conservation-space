package com.sirma.itt.emf.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sirma.itt.emf.util.Documentation;

/**
 * Cache configuration annotation. If specified on a type then the {@link #name()} should be
 * provided with the list of required configurations. If specified on a CONSTANT field then the
 * field value should be string and the string should be the name of the required configuration if
 * not then the {@link #name()} value will be used if present.
 * 
 * @author BBonev
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface CacheConfiguration {

	/**
	 * Name of the required cache configuration. This will uniquely identify the different cache
	 * instances. Optional if used on a string constant field. If so the value of the constant will
	 * be used as a name identifier as is.
	 */
	@Documentation("Name of the required cache configuration. This will uniquely identify the different cache instances.")
	String name() default "";

	/**
	 * Preferred cache container. If not present all containers will be searched for the cache name.
	 */
	@Documentation("Preferred cache container. If not present all containers will be searched for the cache name.")
	String container() default "";

	/**
	 * Sets the cache transaction mode to one of NONE, NON_XA, NON_DURABLE_XA, FULL_XA.
	 */
	@Documentation("Sets the cache transaction mode to one of NONE, NON_XA, NON_DURABLE_XA, FULL_XA.")
	CacheTransactionMode transaction() default CacheTransactionMode.NONE;

	/**
	 * Eviction configuration.
	 */
	@Documentation("Eviction configuration.")
	Eviction eviction() default @Eviction;

	/**
	 * Expiration configuration.
	 */
	@Documentation("Expiration configuration.")
	Expiration expiration() default @Expiration;

	/**
	 * Some documentation for the current cache configuration
	 */
	@Documentation("Documentation for the current cache configuration")
	Documentation doc() default @Documentation("");

}
