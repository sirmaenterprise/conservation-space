/**
 *
 */
package com.sirma.itt.seip.configuration.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Definition of a single configuration property. Could be defined over a {@link String} constant that could be used for
 * configuration identifier or over a injectable field but in this case then the configuration name should be present in
 * the annotation.
 *
 * @author BBonev
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD, PARAMETER, METHOD })
public @interface ConfigurationPropertyDefinition {

	/**
	 * The configuration key. Optional if defined over non null string field or constant.
	 * 
	 * @return configuration property name
	 */
	String name() default "";

	/**
	 * If configuration is marked as system it will have a single value for all tenants if tenant module is present.
	 * Tenant administrator will not be allowed to modify system property. Only system administrator is allowed to
	 * modify application instance wide configuration.
	 * 
	 * @return if configuration is with system or tenant scope
	 */
	boolean system() default false;

	/**
	 * Configuration marked for shared will be distributed over all cluster nodes if clustering is configured or
	 * supported. Not shared configurations are valid only for the current deployment node. By default all
	 * configurations are shared.
	 * 
	 * @return if configuration is shared between nodes
	 */
	boolean shared() default true;

	/**
	 * Default configuration value. The value should be in raw format. It will be converted to proper type before
	 * injection. This value will be returned if the configuration does not have a value or is invalid. If default value
	 * is also <code>null</code> or empty <code>null</code> will be injected. If injected via configuration wrapper
	 * object then the object will be not <code>null</code> but the returned value will be.
	 * 
	 * @return a default value
	 */
	String defaultValue() default "";

	/**
	 * The expected configuration type. The configuration value returned will be of the defined type. A configuration
	 * converter method will be called in order to convert the raw value to the provided type. The application will not
	 * start if there is a configuration that requests a particular type and there is no configuration converter to
	 * produce the value.
	 *
	 * @return the type of the configuration
	 * @see ConfigurationConverter
	 */
	Class<?>type() default String.class;

	/**
	 * The defined string will be used for configuration visualization. It could be a bundle key or text to be returned.
	 * If not defined the configuration key will be used.
	 * 
	 * @return brief configuration description
	 */
	String label() default "";

	/**
	 * Sub system id. Identifier that can be used to group configurations in logical groups. Default value is empty
	 * string.
	 * 
	 * @return optional sub system identifier
	 */
	String subSystem() default "";

	/**
	 * Configuration name alias. If leaved empty the configuration alias is the {@link #subSystem()}.{@link #name()}.
	 * Configuration could not be inserted by alias. It's used only for filtering.
	 * 
	 * @return optional configuration alias
	 */
	String alias() default "";

	/**
	 * Defines an optional name of a converter to be used for value converting. This field is optional and is intended
	 * for use when multiple configurations with specific conversion are defined. If not defined then generic property
	 * converter resolver algorithm may be used.
	 *
	 * @return the name of the converter to use, Optional.
	 */
	String converter() default "";

	/**
	 * Marks a configuration's value as sensitive. This configuration values will not be displayed as plain text
	 * anywhere and will not be viewable by non administrative account.
	 *
	 * @return true, if configuration value is sensitive
	 */
	boolean sensitive() default false;

	/**
	 * Checks if a configuration value represents a password. Password configurations are treated specially. <br>
	 * Default returned value is <code>false</code>.
	 *
	 * @return <code>true</code>, if a configuration value is password value.
	 */
	boolean password() default false;
}
