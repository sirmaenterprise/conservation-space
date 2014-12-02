package com.sirma.itt.emf.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cache configuration annotation. If specified on a type then the {@link #name()} should be
 * provided with the list of required configurations.
 * 
 * @author BBonev
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface CacheConfigurations {

	/**
	 * The list of cache configurations.
	 */
	CacheConfiguration[] value();

}
