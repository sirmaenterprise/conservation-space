package com.sirma.itt.seip.configuration.cdi;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Destroyable;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationBuilder;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.convert.PropertyConverterProvider;
import com.sirma.itt.seip.configuration.event.ConfigurationReloadRequest;
import com.sirma.itt.seip.security.annotation.SecureObserver;

/**
 * CDI producer for {@link ConfigurationProperty} injections. It's also a {@link ConfigurationProvider} implementation.
 * The implementation stores all produced {@link ConfigurationProperty} instances in a local cache. The producer creates
 * a single property per definition.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ConfigurationProducer implements ConfigurationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** The collector. */
	@Inject
	private ConfigurationDefinitionCollector collector;

	/** The instance provider. */
	private ConfigurationInstanceProvider instanceProvider;

	/** The converter provider. */
	private PropertyConverterProvider converterProvider;

	/** The configuration builder. */
	@Inject
	private ConfigurationBuilder configurationBuilder;

	/** The raw configuration accessor. */
	@Inject
	private RawConfigurationAccessor rawConfigurationAccessor;

	/** The properties cache. */
	private Map<String, ConfigurationProperty<?>> propertiesCache = new HashMap<>(256);

	/** The cache lock. */
	private ReadWriteLock cacheLock = new ReentrantReadWriteLock();

	/**
	 * Produce any injectable property.
	 *
	 * @param <T>
	 *            the generic type
	 * @param point
	 *            the point
	 * @return the configuration property
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	@Produces
	@Configuration
	public <T> ConfigurationProperty<T> produceProperty(InjectionPoint point) throws ConfigurationException {
		ConfigurationInstance instance = getConfigInstance(point.getAnnotated());
		LOGGER.trace("Producing configuration {} for {}", instance.getName(), point.getMember());
		return getOrCreateProperty(instance);
	}

	/**
	 * Gets the configuration for integer.
	 *
	 * @param p
	 *            the injection point
	 * @return the configuration int
	 */
	@Produces
	@Configuration
	public Integer getConfigurationInt(InjectionPoint p) {
		return getConfigurationValue(Integer.class, p);
	}

	/**
	 * Gets the configuration for Long.
	 *
	 * @param p
	 *            the injection point
	 * @return the configuration long
	 */
	@Produces
	@Configuration
	public Long getConfigurationLong(InjectionPoint p) {
		return getConfigurationValue(Long.class, p);
	}

	/**
	 * Gets the configuration for double.
	 *
	 * @param p
	 *            the injection point
	 * @return the configuration double
	 */
	@Produces
	@Configuration
	public Double getConfigurationDouble(InjectionPoint p) {
		return getConfigurationValue(Double.class, p);
	}

	/**
	 * Gets a file based on the configured path. The configuration could be a file or folder
	 *
	 * @param p
	 *            the injection point
	 * @return the file detonated by the configured path or <code>null</code> if not directory
	 */
	@Produces
	@Configuration
	public File getConfigurationFile(InjectionPoint p) {
		return getConfigurationValue(File.class, p);
	}

	/**
	 * Gets a configuration property that has a list of values.
	 *
	 * @param p
	 *            the injection point
	 * @return the list configuration
	 */
	@Produces
	@Configuration
	@SuppressWarnings("unchecked")
	public Set<String> getListConfiguration(InjectionPoint p) {
		return getConfigurationValue(Set.class, p);
	}

	/**
	 * Gets the boolean configuration parameter.
	 *
	 * @param p
	 *            the p
	 * @return the boolean configuration
	 */
	@Produces
	@Configuration
	public Boolean getBooleanConfig(InjectionPoint p) {
		return getConfigurationValue(Boolean.class, p);
	}

	/**
	 * Gets the configuration value.
	 *
	 * @param <T>
	 *            the expected type
	 * @param type
	 *            the type class
	 * @param p
	 *            the injection point that need producing
	 * @return the configuration value
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	private <T> T getConfigurationValue(Class<T> type, InjectionPoint p) throws ConfigurationException {
		ConfigurationInstance configInstance = getConfigInstance(p.getAnnotated());
		ConfigurationProperty<Object> property = getOrCreateProperty(configInstance);
		if (property == null || !property.isSet() && p.getAnnotated().getBaseType().getClass().isPrimitive()) {
			throw new IllegalStateException("Configuration " + configInstance.getName()
					+ " is not configured! Cannot inject " + type.getName() + " value.");
		}
		if (!type.isAssignableFrom(configInstance.getType())) {
			throw new IllegalStateException(
					"Configuration " + configInstance.getName() + " is not the same type as requested! Defined type is "
							+ configInstance.getType().getName() + " and requested is " + type.getName());
		}
		return type.cast(property.get());
	}

	/**
	 * Gets the config instance.
	 *
	 * @param annotated
	 *            the annotated
	 * @return the config instance
	 */
	private ConfigurationInstance getConfigInstance(Annotated annotated) {
		Configuration configuration = annotated.getAnnotation(Configuration.class);
		String name = configuration.value();
		// if the configuration name is not defined then the name should be inferred from one of the
		// possible definitions as per specification
		if ("".equals(name)) {
			ConfigurationPropertyDefinition propertyDefinition = annotated
					.getAnnotation(ConfigurationPropertyDefinition.class);
			if (propertyDefinition != null) {
				name = propertyDefinition.name();
			} else {
				ConfigurationGroupDefinition groupDefinition = annotated
						.getAnnotation(ConfigurationGroupDefinition.class);
				if (groupDefinition != null) {
					name = groupDefinition.name();
				}
			}
		}
		if (name == null || name.isEmpty()) {
			throw new IllegalStateException("Invalid injection! Must specify name or configuration annotation!");
		}
		return getInstanceProvider().getConfiguration(name);
	}

	/**
	 * Gets the or create property.
	 *
	 * @param <T>
	 *            the generic type
	 * @param instance
	 *            the instance
	 * @return the or create property
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	@SuppressWarnings("unchecked")
	private <T> ConfigurationProperty<T> getOrCreateProperty(ConfigurationInstance instance)
			throws ConfigurationException {
		cacheLock.readLock().lock();
		ConfigurationProperty<?> cachedProperty = propertiesCache.get(instance.getName());
		try {
			if (cachedProperty != null) {
				return (ConfigurationProperty<T>) cachedProperty;
			}
		} finally {
			cacheLock.readLock().unlock();
		}
		// property was not found build it
		return buildProperty(instance);
	}

	/**
	 * Builds the property.
	 *
	 * @param <T>
	 *            the generic type
	 * @param instance
	 *            the instance
	 * @return the configuration property
	 */
	@SuppressWarnings("unchecked")
	private <T> ConfigurationProperty<T> buildProperty(final ConfigurationInstance instance) {
		cacheLock.writeLock().lock();
		try {
			// check if someone build the property before we get here
			if (propertiesCache.containsKey(instance.getName())) {
				return (ConfigurationProperty<T>) propertiesCache.get(instance.getName());
			}

			ConfigurationProperty<Object> buildProperty = configurationBuilder.buildProperty(instance,
					rawConfigurationAccessor, getConverterProvider(), this, getInstanceProvider());

			attachGroupChangeListeners(buildProperty, instance);
			propertiesCache.put(instance.getName(), buildProperty);

			return (ConfigurationProperty<T>) buildProperty;
		} catch (ConfigurationException e) {
			LOGGER.error("Failed to build configuration {} of type {}", instance.getName(), instance.getType(), e);
			throw e;
		} finally {
			cacheLock.writeLock().unlock();
		}
	}

	/*
	 * Link group configuration with it's sub properties so when sub property is changed than the group value will be
	 * cleared and rebuild
	 */
	private void attachGroupChangeListeners(ConfigurationProperty<Object> property, ConfigurationInstance instance) {
		if (instance.getAnnotation() instanceof ConfigurationGroupDefinition) {
			ConfigurationGroupDefinition groupDefinition = (ConfigurationGroupDefinition) instance.getAnnotation();
			String[] properties = groupDefinition.properties();
			for (int i = 0; i < properties.length; i++) {
				ConfigurationProperty<Object> configurationProperty = getProperty(properties[i]);
				configurationProperty.addConfigurationChangeListener(e -> property.valueUpdated());
			}
		}
	}

	/**
	 * Gets the property.
	 *
	 * @param <T>
	 *            the generic type
	 * @param name
	 *            the name
	 * @return the property
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	@Override
	public <T> ConfigurationProperty<T> getProperty(String name) throws ConfigurationException {
		ConfigurationInstance instance = getInstanceProvider().getConfiguration(name);
		if (instance == null) {
			throw new ConfigurationException("No such configuration is defined: " + name);
		}
		return getOrCreateProperty(instance);
	}

	/**
	 * Gets the names of the configurations that have been created until now.
	 *
	 * @return the produced configuration property names
	 */
	@Override
	public Set<String> getInstantiatedConfigurations() {
		cacheLock.readLock().lock();
		try {
			return Collections.unmodifiableSet(propertiesCache.keySet());
		} finally {
			cacheLock.readLock().unlock();
		}
	}

	/**
	 * Gets the instance provider.
	 *
	 * @return the provider
	 */
	@Produces
	@Singleton
	public ConfigurationInstanceProvider getInstanceProvider() {
		if (instanceProvider == null) {
			instanceProvider = collector.createInstanceProvider();
		}
		return instanceProvider;
	}

	/**
	 * Gets the converter provider.
	 *
	 * @return the converter
	 */
	@Produces
	@Singleton
	public PropertyConverterProvider getConverterProvider() {
		if (converterProvider == null) {
			converterProvider = collector.createConverterProvider();
		}
		return converterProvider;
	}

	/**
	 * On configuration reload.
	 *
	 * @param reloadRequest
	 *            the reload request
	 */
	@SecureObserver
	void onConfigurationReload(@Observes ConfigurationReloadRequest reloadRequest) {
		LOGGER.debug("Requested full configuration reload..");
		getInstanceProvider()
				.getAllInstances()
					.stream()
					.map(c -> getProperty(c.getName()))
					.forEach(p -> p.valueUpdated());
	}

	/**
	 * On shutdown.
	 */
	@PreDestroy
	void onShutdown() {
		cacheLock.writeLock().lock();
		try {
			propertiesCache.values().forEach(Destroyable::destroy);
		} finally {
			cacheLock.writeLock().unlock();
		}
	}
}
