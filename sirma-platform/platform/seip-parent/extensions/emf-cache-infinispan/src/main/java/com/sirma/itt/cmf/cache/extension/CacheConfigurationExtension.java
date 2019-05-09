package com.sirma.itt.cmf.cache.extension;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheConfigurations;
import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Extension to collect the required cache configuration. The information later can be used for configuration
 * validation.
 *
 * @author BBonev
 */
public class CacheConfigurationExtension implements Extension {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/**
	 * If the given file is found in the meta-inf folder then the configuration validation will be disabled! The
	 * validation will be performed but the server will not fail but only print the errors.
	 */
	public static final String DISABLE_CONFIGURATION_VALIDATION = "/META-INF/services/com.sirma.itt.emf.DisableCacheConfigurationValidation";

	/** The injected configuration. */
	private Map<String, List<Type>> injectedConfiguration = new TreeMap<>();

	private Map<String, List<CacheConfiguration>> configurations = new TreeMap<>();

	/** The disable configuration validation. */
	private boolean disableConfigurationValidation = false;

	/**
	 * Before bean discovery.
	 *
	 * @param discovery
	 *            the discovery
	 */
	void beforeBeanDiscovery(@Observes BeforeBeanDiscovery discovery) {
		// check if we have disabled the configuration validation
		URL url = getClass().getClassLoader().getResource(DISABLE_CONFIGURATION_VALIDATION);
		disableConfigurationValidation = url != null;
		if (disableConfigurationValidation) {
			LOGGER.warn("Disabled configuration validation!");
		}
	}

	/**
	 * Process annotated type.
	 *
	 * @param <D>
	 *            the generic type
	 * @param pat
	 *            the pat
	 */
	<D> void processAnnotatedType(@Observes ProcessAnnotatedType<D> pat) {
		AnnotatedType<D> type = pat.getAnnotatedType();
		if (type.isAnnotationPresent(CacheConfiguration.class)) {
			CacheConfiguration configuration = type.getAnnotation(CacheConfiguration.class);
			if (StringUtils.isNotBlank(configuration.name())) {
				CollectionUtils.addValueToMap(injectedConfiguration, configuration.name(), type.getJavaClass());
				CollectionUtils.addValueToMap(configurations, configuration.name(), configuration);
			}
		} else if (type.isAnnotationPresent(CacheConfigurations.class)) {
			CacheConfigurations configs = type.getAnnotation(CacheConfigurations.class);
			if (configs.value() != null && configs.value().length > 0) {
				for (CacheConfiguration configuration : configs.value()) {
					if (StringUtils.isNotBlank(configuration.name())) {
						CollectionUtils.addValueToMap(injectedConfiguration, configuration.name(), type.getJavaClass());
						CollectionUtils.addValueToMap(configurations, configuration.name(), configuration);
					}
				}
			}
		}
		// process the fields of all classes
		processConfiguredFields(type);
	}

	void afterBeanDiscovery(@Observes AfterBeanDiscovery beanDiscovery) {
		AnnotationCacheConfigurationProvider.initialize(configurations);
	}

	/**
	 * Process configured fields.
	 *
	 * @param <X>
	 *            the generic type
	 * @param at
	 *            the at
	 */
	private <X> void processConfiguredFields(AnnotatedType<X> at) {
		Set<AnnotatedField<? super X>> fields = at.getFields();

		for (AnnotatedField<? super X> annotatedField : fields) {
			CacheConfiguration config = annotatedField.getAnnotation(CacheConfiguration.class);
			if (config != null) {
				String configName = config.name();
				if (StringUtils.isBlank(config.name())) {
					Object value = getFieldValue(annotatedField);
					if (value instanceof String) {
						configName = (String) value;
					}
				}

				if (StringUtils.isNotBlank(configName)) {
					CollectionUtils.addValueToMap(configurations, configName, config);
					CollectionUtils.addValueToMap(injectedConfiguration, configName,
							annotatedField.getDeclaringType().getJavaClass());
				}
				// else missing name configuration
			}
		}
	}

	/**
	 * Gets the field value.
	 *
	 * @param <X>
	 *            the generic type
	 * @param annotatedField
	 *            the annotated field
	 * @return the field value
	 */
	private static <X> Object getFieldValue(AnnotatedField<? super X> annotatedField) {
		Field field = annotatedField.getJavaMember();
		try {
			field.setAccessible(true);
			return field.get(annotatedField.getDeclaringType().getJavaClass());
		} catch (Exception e) {
			LOGGER.trace("Failed to access field {} due to ", field, e);
		}
		return null;
	}

	/**
	 * Getter method for disableConfigurationValidation.
	 *
	 * @return the disableConfigurationValidation
	 */
	public boolean isDisableConfigurationValidation() {
		return disableConfigurationValidation;
	}
}
