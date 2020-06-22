package com.sirma.itt.seip.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.convert.PropertyContext;
import com.sirma.itt.seip.configuration.convert.TypeConverterContext;
import com.sirma.itt.seip.configuration.util.ReflectionHelper;

/**
 * Configuration instance implementation that represents a single property configuration definition.
 *
 * @author BBonev
 */
public class PropertyConfigurationInstance implements ConfigurationInstance {

	private final ConfigurationPropertyDefinition propertyDefinition;
	private final String name;
	private TypeConverterContext converterContext;
	private final Member definedOn;

	/**
	 * Instantiates a new property configuration instance.
	 *
	 * @param propertyDefinition
	 *            the property definition
	 * @param definedOn
	 *            the defined on
	 */
	public PropertyConfigurationInstance(ConfigurationPropertyDefinition propertyDefinition, Member definedOn) {
		this.propertyDefinition = propertyDefinition;
		this.definedOn = definedOn;
		name = ReflectionHelper.determineConfigName(propertyDefinition.name(), definedOn);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isSystemConfiguration() {
		return propertyDefinition.system();
	}

	@Override
	public boolean isSharedConfiguration() {
		return propertyDefinition.shared();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<?> getType() {
		return propertyDefinition.type();
	}

	@Override
	public String getLabel() {
		if ("".equals(propertyDefinition.label())) {
			return getName();
		}
		return propertyDefinition.label();
	}

	@Override
	public TypeConverterContext createConverterContext(ConfigurationInstanceProvider configurationInstanceProvider,
			RawConfigurationAccessor rawConfigurationAccessor, ConfigurationProvider provider) {
		if (converterContext == null) {
			converterContext = new PropertyContext(this, propertyDefinition.defaultValue(), rawConfigurationAccessor);
		}
		return converterContext;
	}

	@Override
	public Annotation getAnnotation() {
		return propertyDefinition;
	}

	@Override
	public String getSubSystem() {
		return propertyDefinition.subSystem();
	}

	@Override
	public String getAlias() {
		String alias = propertyDefinition.alias();
		if (StringUtils.isNotBlank(alias)) {
			return alias;
		}
		if (StringUtils.isBlank(getSubSystem())) {
			return "";
		}
		return getSubSystem() + "." + getName();
	}

	@Override
	public Member getDefinedOn() {
		return definedOn;
	}

	@Override
	public String getConverter() {
		return org.apache.commons.lang.StringUtils.trimToNull(propertyDefinition.converter());
	}

	@Override
	public boolean isSensitive() {
		return propertyDefinition.sensitive();
	}

	@Override
	public boolean isPassword() {
		return propertyDefinition.password();
	}
}
