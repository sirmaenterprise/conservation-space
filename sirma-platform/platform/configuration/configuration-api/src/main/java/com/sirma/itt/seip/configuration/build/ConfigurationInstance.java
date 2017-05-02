package com.sirma.itt.seip.configuration.build;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.TypeConverterContext;

/**
 * Represents a single configuration object. It may represent a simple or complex configuration.
 *
 * @author BBonev
 */
public interface ConfigurationInstance extends Named {

	/**
	 * Returns true of the configuration is system.
	 *
	 * @return <code>true</code>, if is system
	 */
	boolean isSystemConfiguration();

	/**
	 * Checks if is shared configuration.
	 *
	 * @return true, if is shared
	 */
	boolean isSharedConfiguration();

	/**
	 * Gets the expected configuration type.
	 *
	 * @param <T>
	 *            the configuration value type
	 * @return the type
	 */
	<T> Class<T> getType();

	/**
	 * Gets the label that represents the given configuration
	 *
	 * @return the label
	 */
	String getLabel();

	/**
	 * Defines configuration instance that is dependent on other configurations.
	 *
	 * @return <code>true</code> if current configuration depends on other configurations
	 */
	default boolean isComplex() {
		return false;
	}

	/**
	 * Gets the annotation than represents the current instance. It should be {@link ConfigurationPropertyDefinition} or
	 * {@link ConfigurationGroupDefinition}.
	 *
	 * @return the annotation
	 */
	Annotation getAnnotation();

	/**
	 * Sub system id. Identifier that can be used to group configurations in logical groups. Default value is empty
	 * string.
	 *
	 * @return configuration sub system
	 */
	String getSubSystem();

	/**
	 * Configuration name alias. If leaved empty the configuration alias is the
	 * {@link #getSubSystem()}.{@link #getName()}. Configuration could not be inserted by alias. It's used only for
	 * filtering.
	 *
	 * @return configuration alias
	 */
	String getAlias();

	/**
	 * Get the java member that has the configuration defined. This is optional method if configuration is defined over
	 * field or method, if not it's free to return <code>null</code> .
	 *
	 * @return the containing java member or <code>null</code>
	 */
	default Member getDefinedOn() {
		return null;
	}

	/**
	 * Gets a converter name that can be used for configuration conversion. If not supported <code>null</code> may be
	 * returned. If non <code>null</code> value is returned a converter with the given name will be search and if found
	 * it will be used before any other. If not found general converter algorithm may be used instead.
	 *
	 * @return the name of the converter to use, optional.
	 */
	default String getConverter() {
		return null;
	}

	/**
	 * Checks if a configuration's value is sensitive. This configuration values will not be displayed as plain text
	 * anywhere and will not be viewable by non administrative account.
	 *
	 * @return true, if configuration value is sensitive
	 */
	default boolean isSensitive() {
		return false;
	}

	/**
	 * Checks if a configuration value represents a password. Password configurations are treated specially. <br>
	 * Default returned value is <code>false</code>.
	 *
	 * @return <code>true</code>, if a configuration value is password value.
	 */
	default boolean isPassword() {
		return false;
	}

	/**
	 * Creates the converter context that to be passed to converter methods to produce the actual configuration.
	 *
	 * @param configurationInstanceProvider
	 *            the configuration instance provider
	 * @param rawConfigurationAccessor
	 *            the raw configuration provider
	 * @param provider
	 *            the provider
	 * @return the type converter context
	 */
	TypeConverterContext createConverterContext(ConfigurationInstanceProvider configurationInstanceProvider,
			RawConfigurationAccessor rawConfigurationAccessor, ConfigurationProvider provider);
}
