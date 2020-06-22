package com.sirma.itt.seip.configuration.cdi;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.GroupConfigurationInstance;
import com.sirma.itt.seip.configuration.PropertyConfigurationInstance;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationBuilder;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.configuration.build.DefaultConfigurationBuilder;
import com.sirma.itt.seip.configuration.build.DefaultRawConfigurationAccessor;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.convert.ChainingConverter;
import com.sirma.itt.seip.configuration.convert.ConfigurationValueConverter;
import com.sirma.itt.seip.configuration.convert.ConverterClassInstance;
import com.sirma.itt.seip.configuration.convert.ConverterMethodInstance;
import com.sirma.itt.seip.configuration.convert.PropertyConverterProvider;
import com.sirma.itt.seip.configuration.convert.TypeConverterContext;

/**
 * CDI extension that collects all defined configurations and property converters. At the end of the bean discovery a
 * validation is done to all found components.
 *
 * @author BBonev
 */
public class ConfigurationDefinitionCollector implements Extension {

	private static final Set<Class<?>> VETOED_BEANS = new HashSet<>(
			Arrays.asList(DefaultConfigurationBuilder.class, DefaultRawConfigurationAccessor.class));

	private Map<String, ConfigurationInstance> properties = new HashMap<>(256);
	private Map<Class<?>, ConfigurationValueConverter<?>> converters = new HashMap<>(128);
	private Map<String, List<Field>> usedConfigurations = new HashMap<>(256);

	/**
	 * Creates the instance provider.
	 *
	 * @return the configuration instance provider
	 */
	ConfigurationInstanceProvider createInstanceProvider() {
		return new ConfigurationInstanceProviderImplementation();
	}

	/**
	 * Creates the converter provider.
	 *
	 * @return the property converter provider
	 */
	PropertyConverterProvider createConverterProvider() {
		return new PropertyConverterProvider() {

			@Override
			public Set<Class<?>> getSupportedTypes() {
				return converters.keySet();
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T> ConfigurationValueConverter<T> getConverter(Class<T> resultType) {
				return (ConfigurationValueConverter<T>) converters.get(resultType);
			}

			@Override
			public Optional<ConfigurationValueConverter<?>> getConverter(String converterName) {
				return StringUtils.isBlank(converterName) ?
						Optional.empty() :
						converters.values().stream()
								.filter(converter -> nullSafeEquals(converter.getName(), converterName)).findAny();
			}
		};
	}

	/**
	 * Veto default beans. Scan the annotated type for configuration definitions, injections and converters.
	 *
	 * @param <X>
	 *            the generic type
	 * @param annotatedType
	 *            the annotated type
	 */
	<X> void onAnnotatedType(@Observes ProcessAnnotatedType<X> annotatedType, BeanManager beanManager) {
		// veto default beans if nothing else is defined at the end we will add them
		veto(annotatedType);

		for (AnnotatedField<? super X> annotatedField : annotatedType.getAnnotatedType().getFields()) {
			ConfigurationInstance instance = collectPropertyConfigurationInfo(annotatedField);
			if (instance == null) {
				instance = collectGroupConfigurationInfo(annotatedField);
			}
			collectUsedConfigurations(instance, annotatedField.getAnnotation(Configuration.class), annotatedField);
		}

		for (AnnotatedMethod<? super X> annotatedMethod : annotatedType.getAnnotatedType().getMethods()) {
			collectTypeConverterInfo(annotatedMethod, beanManager);

			ConfigurationInstance instance = collectPropertyConfigurationInfo(annotatedMethod);
			if (instance == null) {
				collectGroupConfigurationInfo(annotatedMethod);
			}
		}

		if (ConfigurationValueConverter.class.isAssignableFrom(annotatedType.getAnnotatedType().getJavaClass())) {
			collectTypeConverterInfo(annotatedType.getAnnotatedType(), beanManager);
		}
	}

	/**
	 * Veto the given class if match
	 *
	 * @param <X>
	 *            the generic type
	 * @param annotatedType
	 *            the annotated type
	 */
	private static <X> void veto(ProcessAnnotatedType<X> annotatedType) {
		if (VETOED_BEANS.contains(annotatedType.getAnnotatedType().getJavaClass())) {
			annotatedType.veto();
		}
	}

	private <X> void collectUsedConfigurations(ConfigurationInstance instance, Configuration annotation,
			AnnotatedField<? super X> annotatedField) {
		if (annotation == null) {
			return;
		}
		if ("".equals(annotation.value())) {
			if (instance == null) {
				throw new IllegalArgumentException("The injectable configuration " + annotatedField.getJavaMember()
						+ " does not define a configuration name and does not have a configuration definition. "
						+ "Define name or add configuration definition annotation!");
			}
			CollectionUtils.addValueToMap(usedConfigurations, instance.getName(), annotatedField.getJavaMember());
		} else {
			CollectionUtils.addValueToMap(usedConfigurations, annotation.value(), annotatedField.getJavaMember());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <X> void collectTypeConverterInfo(AnnotatedMethod<X> annotatedType, BeanManager beanManager) {
		ConfigurationConverter annotation = annotatedType.getAnnotation(ConfigurationConverter.class);
		if (annotation == null) {
			return;
		}
		ConfigurationValueConverter instance = new ConverterMethodInstance(
				annotatedType.getDeclaringType().getJavaClass(), annotatedType.getJavaMember(), beanManager);
		registerConverter(instance);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <X> void collectTypeConverterInfo(AnnotatedType<X> annotatedType, BeanManager beanManager) {
		ConfigurationConverter annotation = annotatedType.getAnnotation(ConfigurationConverter.class);
		if (annotation == null) {
			return;
		}
		ConfigurationValueConverter instance = new ConverterClassInstance(annotatedType.getJavaClass(), beanManager);
		registerConverter(instance);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void registerConverter(ConfigurationValueConverter<?> instance) {
		ConfigurationValueConverter<?> converter = converters.get(instance.getType());
		if (converter == null) {
			converters.put(instance.getType(), instance);
		} else if (converter instanceof ChainingConverter) {
			((ChainingConverter) converter).addConverter(instance);
		} else {
			ChainingConverter chainingConverter = new ChainingConverter(converter.getType(), converter);
			chainingConverter.addConverter(instance);
			converters.put(chainingConverter.getType(), chainingConverter);
		}
	}

	private <X> ConfigurationInstance collectPropertyConfigurationInfo(AnnotatedMember<? super X> annotatedMember) {
		ConfigurationPropertyDefinition annotation = annotatedMember
				.getAnnotation(ConfigurationPropertyDefinition.class);
		if (annotation == null) {
			return null;
		}
		ConfigurationInstance instance = new PropertyConfigurationInstance(annotation, annotatedMember.getJavaMember());

		registerInstance(instance, annotatedMember);
		return instance;
	}

	private <X> ConfigurationInstance collectGroupConfigurationInfo(AnnotatedMember<? super X> annotatedMember) {
		ConfigurationGroupDefinition annotation = annotatedMember.getAnnotation(ConfigurationGroupDefinition.class);
		if (annotation == null) {
			return null;
		}
		if (annotation.properties().length == 0) {
			throw new IllegalArgumentException(
					ConfigurationGroupDefinition.class.getName() + " is requiered to define at least one property!");
		}
		ConfigurationInstance instance = new GroupConfigurationInstance(annotation, annotatedMember.getJavaMember());

		registerInstance(instance, annotatedMember);
		return instance;
	}

	private <X> void registerInstance(ConfigurationInstance instance, AnnotatedMember<? super X> member) {
		ConfigurationInstance currentConfig = properties.get(instance.getName());
		if (currentConfig != null && !isDuplicateAllowed((ConfigurationInstanceProxy) currentConfig, member)) {
			throw new IllegalStateException(
					String.format("Configuration %s already defined in %s and cannot register the new found in %s!",
							instance.getName(),
							((ConfigurationInstanceProxy) currentConfig).getMember().getDeclaringClass(),
							member.getJavaMember().getDeclaringClass()));
		}
		properties.put(instance.getName(), new ConfigurationInstanceProxy(instance, member.getJavaMember()));
	}

	private static <X> boolean isDuplicateAllowed(ConfigurationInstanceProxy instanceProxy,
			AnnotatedMember<? super X> annotated) {
		Class<?> declaringClass = annotated.getJavaMember().getDeclaringClass();
		Class<?> processedType = instanceProxy.getMember().getDeclaringClass();
		return declaringClass.equals(processedType);
	}

	/**
	 * Do some validation at the end and add default bean implementations if needed.
	 *
	 * @param afterBeanDiscovery
	 *            the after bean discovery
	 */
	void onAfterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager manager) {
		Set<String> configurations = new HashSet<>(usedConfigurations.keySet());
		configurations.removeAll(properties.keySet());

		if (!configurations.isEmpty()) {
			afterBeanDiscovery.addDefinitionError(new ConfigurationException(
					"Found configurations for injection that are not defined: " + configurations));
			return;
		}

		verifyUsedConfigurationsAreInTheSameTypeAsDefined(properties, usedConfigurations, afterBeanDiscovery);

		for (Entry<String, ConfigurationInstance> entry : properties.entrySet()) {
			Class<Object> type = entry.getValue().getType();
			if (type.isEnum()) {
				if (isNotValidEnumConfiguration(type)) {
					afterBeanDiscovery.addDefinitionError(new ConfigurationException(
							"No enum converter found for configuration " + entry.getKey() + " and requested type of "
									+ type));
				}
			} else if (!converters.containsKey(type)) {
				afterBeanDiscovery.addDefinitionError(new ConfigurationException(
						"No converter found for configuration " + entry.getKey() + " and requested type of " + type));
			}
		}

		properties = Collections.unmodifiableMap(properties);
		converters = Collections.unmodifiableMap(converters);

		// register default configuration builder if no other is present
		if (manager.getBeans(ConfigurationBuilder.class).isEmpty()) {
			afterBeanDiscovery.addBean(new DefaultConfigurationBuilderBean(manager));
		}
		// register default raw configuration accessor if no other is present
		if (manager.getBeans(RawConfigurationAccessor.class).isEmpty()) {
			afterBeanDiscovery.addBean(new DefaultRawConfigurationAccessorBean(manager));
		}
	}

	private boolean isNotValidEnumConfiguration(Class<Object> type) {
		// for enums check if Enum is parent of the defined configuration type
		return converters.keySet().stream().noneMatch(converter -> converter.isAssignableFrom(type));
	}

	static void verifyUsedConfigurationsAreInTheSameTypeAsDefined(Map<String, ConfigurationInstance> instances,
			Map<String, List<Field>> uses, AfterBeanDiscovery afterBeanDiscovery) {
		uses.forEach((name, fields) -> {
			// subclasses will have the same uses
			Set<String> formattedUses = getUses(fields);
			if (formattedUses.size() > 1) {
				afterBeanDiscovery.addDefinitionError(new ConfigurationException(
						"Configuration " + name + " is used more than once " + formattedUses));
				return;
			}
			// verify if configuration is not defined in one class and used in another
			verifyNotUsedInOtherType(instances, afterBeanDiscovery, name, fields);
		});
	}

	private static void verifyNotUsedInOtherType(Map<String, ConfigurationInstance> instances,
			AfterBeanDiscovery afterBeanDiscovery, String name, List<Field> fields) {
		ConfigurationInstance instance = instances.get(name);
		fields.forEach(field -> {
			if (isNotDefinedInSameHierarchy(instance, field)) {
				afterBeanDiscovery.addDefinitionError(new ConfigurationException("Configuration " + name
						+ " is defined in " + instance.getDefinedOn().getDeclaringClass() + " and is used in "
						+ field.getDeclaringClass() + " and they are not from the same class hierarchy!"));
			}
		});
	}

	private static boolean isNotDefinedInSameHierarchy(ConfigurationInstance instance, Field field) {
		return instance.getDefinedOn() != null
				&& !(instance.getDefinedOn().getDeclaringClass().isAssignableFrom(field.getDeclaringClass())
						|| field.getDeclaringClass().isAssignableFrom(instance.getDefinedOn().getDeclaringClass()));
	}

	private static Set<String> getUses(List<Field> value) {
		return value.stream().map(jm -> jm.getDeclaringClass() + "." + jm.getName()).collect(Collectors.toSet());
	}

	/**
	 * Proxy implementation to the collected configurations.
	 *
	 * @author BBonev
	 */
	final class ConfigurationInstanceProviderImplementation implements ConfigurationInstanceProvider {
		@Override
		public ConfigurationInstance getConfiguration(String name) {
			return properties.get(name);
		}

		@Override
		public Set<String> getRegisteredConfigurations() {
			return properties.keySet();
		}

		@Override
		public Collection<ConfigurationInstance> getAllInstances() {
			return new LinkedList<>(properties.values());
		}

		@Override
		public Collection<ConfigurationInstance> getFiltered(Predicate<ConfigurationInstance> filter) {
			return properties.values().stream().filter(filter).collect(Collectors.toList());
		}
	}

	/**
	 * Defines a proxy that has additional info stored
	 *
	 * @author BBonev
	 */
	static class ConfigurationInstanceProxy implements ConfigurationInstance {

		private final ConfigurationInstance target;
		private final Member member;

		/**
		 * Instantiates a new configuration instance proxy.
		 *
		 * @param target
		 *            the target
		 * @param member
		 *            the member
		 */
		ConfigurationInstanceProxy(ConfigurationInstance target, Member member) {
			this.target = target;
			this.member = member;
		}

		@Override
		public String getName() {
			return target.getName();
		}

		@Override
		public boolean isSystemConfiguration() {
			return target.isSystemConfiguration();
		}

		@Override
		public boolean isSharedConfiguration() {
			return target.isSharedConfiguration();
		}

		@Override
		public <T> Class<T> getType() {
			return target.getType();
		}

		@Override
		public String getLabel() {
			return target.getLabel();
		}

		@Override
		public Annotation getAnnotation() {
			return target.getAnnotation();
		}

		@Override
		public boolean isComplex() {
			return target.isComplex();
		}

		@Override
		public TypeConverterContext createConverterContext(ConfigurationInstanceProvider configurationInstanceProvider,
				RawConfigurationAccessor rawConfigurationAccessor, ConfigurationProvider provider) {
			return target.createConverterContext(configurationInstanceProvider, rawConfigurationAccessor, provider);
		}

		Member getMember() {
			return member;
		}

		@Override
		public String getSubSystem() {
			return target.getSubSystem();
		}

		@Override
		public String getAlias() {
			return target.getAlias();
		}

		@Override
		public String getConverter() {
			return target.getConverter();
		}

		@Override
		public boolean isSensitive() {
			return target.isSensitive();
		}

		@Override
		public boolean isPassword() {
			return target.isPassword();
		}
	}

}
