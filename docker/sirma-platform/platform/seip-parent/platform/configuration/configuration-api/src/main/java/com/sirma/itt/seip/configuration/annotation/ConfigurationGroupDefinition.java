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
 * Definition for a complex configuration object. The group configuration will be injected as normal configuration
 * object but will be produced using more than one configuration properties.
 * <p>
 * Usage notes: <br>
 * The configuration names is suggested to be a string constants annotated with {@link ConfigurationPropertyDefinition}.
 * <br>
 * The group configuration could be defined on a constant or directly over the injectable field.<br>
 * The complex injected object could be represented by an interfaces if needed. The configuration converted should
 * produce the same type.<br>
 * Use group configuration when required properties are at least 2.<br>
 * If all references properties are marked as system then the group configuration will be considered as system and will
 * have only single instance.
 *
 * @author BBonev
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD, PARAMETER, METHOD })
public @interface ConfigurationGroupDefinition {

	/**
	 * Name of the configuration to inject or to be referred. It's optional if defined over a non <code>null</code>
	 * {@link String} field or constant.
	 *
	 * @return group configuration name
	 */
	String name() default "";

	/**
	 * Properties that composes the configuration object. The names should match {@link ConfigurationPropertyDefinition}
	 * names (specified or inferred). If used a configuration name that is not defined a deployment error will be
	 * thrown.
	 *
	 * @return the list of dependent properties that are part of this group
	 */
	String[]properties();

	/**
	 * The expected configuration type. The configuration value returned will be of the defined type. A configuration
	 * converter method will be called in order to convert the raw values to the provided type. The application will not
	 * start if there is a configuration that requests a particular type and there is no configuration converter to
	 * produce the value.
	 *
	 * @return the configuration type
	 * @see ConfigurationConverter
	 */
	Class<?>type() default Object.class;

	/**
	 * The defined string will be used for configuration visualization. It could be a bundle key or text to be returned.
	 * If not defined the configuration group name will be used.
	 *
	 * @return brief configuration description
	 */
	String label() default "";

	/**
	 * If configuration is marked as system it will have a single value for all tenants if tenant module is present.
	 * Tenant administrator will not be allowed to modify system property. Only system administrator is allowed to
	 * modify application instance wide configuration.
	 * 
	 * @return if the group is system or per tenant scoped
	 */
	boolean system() default false;

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
	 * @return optional group alias
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
}
