package com.sirma.itt.seip.configuration.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Configuration injection qualifier. The annotation should be used over field that is of concrete injectable type or
 * configuration wrapper object.
 * <p>
 * If used over concrete field then single non tenant aware configuration property will be injected that will not be
 * dynamically changed during application lifetime without application restart or creating the component. <br>
 * If used over configuration wrapper object {@link ConfigurationProperty} then the caller will be provided with dynamic
 * configuration support that is tenant aware if any. It could also register for notifications when the value has been
 * changed.
 *
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, FIELD, PARAMETER, METHOD })
public @interface Configuration {

	/**
	 * Define the name of configuration that need to be injected.
	 * 
	 * @return the configuration id
	 */
	@Nonbinding
	String value() default "";
}
