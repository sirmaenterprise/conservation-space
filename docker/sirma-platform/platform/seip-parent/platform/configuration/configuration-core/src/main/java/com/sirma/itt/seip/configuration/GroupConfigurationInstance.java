package com.sirma.itt.seip.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.convert.GroupContext;
import com.sirma.itt.seip.configuration.convert.TypeConverterContext;
import com.sirma.itt.seip.configuration.util.ReflectionHelper;

/**
 * Configuration instance implementation that handles group definitions
 *
 * @author BBonev
 */
public class GroupConfigurationInstance implements ConfigurationInstance {

	private final ConfigurationGroupDefinition groupDefinition;
	private final String name;
	private TypeConverterContext converterContext;
	private final Member definedOn;

	/**
	 * Instantiates a new group configuration instance.
	 *
	 * @param groupDefinition
	 *            the group definition
	 * @param definedOn
	 *            the defined on
	 */
	public GroupConfigurationInstance(ConfigurationGroupDefinition groupDefinition, Member definedOn) {
		this.groupDefinition = groupDefinition;
		this.definedOn = definedOn;
		name = ReflectionHelper.determineConfigName(groupDefinition.name(), definedOn);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isSystemConfiguration() {
		return groupDefinition.system();
	}

	@Override
	public boolean isSharedConfiguration() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<?> getType() {
		return groupDefinition.type();
	}

	@Override
	public String getLabel() {
		if ("".equals(groupDefinition.label())) {
			return getName();
		}
		return groupDefinition.label();
	}

	@Override
	public TypeConverterContext createConverterContext(ConfigurationInstanceProvider configurationInstanceProvider,
			RawConfigurationAccessor rawConfigurationAccessor, ConfigurationProvider provider) {
		if (converterContext == null) {
			converterContext = new GroupContext(this, groupDefinition, configurationInstanceProvider,
					rawConfigurationAccessor, provider);
		}
		return converterContext;
	}

	@Override
	public Annotation getAnnotation() {
		return groupDefinition;
	}

	@Override
	public boolean isComplex() {
		return true;
	}

	@Override
	public String getSubSystem() {
		return groupDefinition.subSystem();
	}

	@Override
	public String getAlias() {
		String alias = groupDefinition.alias();
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
	public boolean isSensitive() {
		return true;
	}

	@Override
	public String getConverter() {
		return org.apache.commons.lang.StringUtils.trimToNull(groupDefinition.converter());
	}
}
