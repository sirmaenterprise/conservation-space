package com.sirma.itt.seip.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

/**
 * Wrapping annotation for {@link CacheConfiguration}. This is intended to be used only on a {@link Type}
 *
 * @author BBonev
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface CacheConfigurations {

	/**
	 * The list of cache configurations.
	 *
	 * @return a list of cache configurations
	 */
	CacheConfiguration[]value();

}
