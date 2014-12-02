package com.sirma.itt.emf.provider.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier used to narrow the recipients when firing provider registry register events
 * 
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface ProviderRegistryEvent {

	/**
	 * The type of the registry event
	 */
	String value() default "";
}
