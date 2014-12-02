package com.sirma.itt.emf.extensions;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.interceptor.Interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.annotation.Optional;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.ReflectionUtils;

/**
 * CDI extension that collects all classes annotated with {@link Documentation} annotation.
 *
 * @author BBonev
 */
public class DocumentationExtension implements Extension {

	/**
	 * If the given file is found in the meta-inf folder then the configuration validation will be
	 * disabled! The validation will be performed but the server will not fail but only print the
	 * errors.
	 */
	public static final String DISABLE_CONFIGURATION_VALIDATION = "/META-INF/services/com.sirma.itt.emf.DisableConfigurationValidation";

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentationExtension.class);
	/**
	 * Comparator for sorting classes by fill class name.
	 *
	 * @author BBonev
	 */
	private static class ClassNameComparator implements Comparator<Class<?>> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(Class<?> o1, Class<?> o2) {
			return o1.getCanonicalName().compareToIgnoreCase(o2.getCanonicalName());
		}
	}

	/** The documented classes. */
	private static Set<Class<?>> documentedClasses = new TreeSet<Class<?>>(
			new ClassNameComparator());

	/** The injected configuration. */
	private static Map<String, List<AnnotatedField<?>>> injectedConfiguration = new TreeMap<String, List<AnnotatedField<?>>>();

	private boolean disableConfigurationValidation = false;

	/**
	 * Before bean discovery.
	 * 
	 * @param discovery
	 *            the discovery
	 */
	public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery discovery) {
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
	public <D> void processAnnotatedType(@Observes ProcessAnnotatedType<D> pat) {
		AnnotatedType<D> type = pat.getAnnotatedType();
		if (type.isAnnotationPresent(Documentation.class)) {
			documentedClasses.add(type.getJavaClass());
		} else if (type.isAnnotationPresent(Interceptor.class)) {
			processConfiguredFields(type);
		}
	}

	/**
	 * Validate configurations.
	 *
	 * @param discovery
	 *            the discovery
	 */
	public void validateConfigurations(@Observes AfterBeanDiscovery discovery) {
		List<Class<?>> classes = getTypedClasses(Configuration.class);
		if (classes.isEmpty() && !injectedConfiguration.isEmpty()) {
			addDeploymentError(discovery,
					"No configuration documentation found for " + injectedConfiguration.keySet());
		}

		Set<String> checked = new HashSet<String>();

		// check all configuration classes to have proper documentation
		for (Class<?> configClass : classes) {
			Field[] fields = configClass.getFields();
			for (Field field : fields) {
				Documentation documentation = field.getAnnotation(Documentation.class);
				Object fieldValue = ReflectionUtils.getFieldValue(field, configClass);
				// if the constant value is null then is not a valid constant due to it cannot be
				// used in annotation
				if (fieldValue == null) {
					continue;
				}
				if ((documentation == null) || documentation.value().isEmpty()) {
					addDeploymentError(discovery,
							"Found defined configuartion that is not documented: "
							+ configClass + "." + field.getName() + "=" + fieldValue);
				}
				List<AnnotatedField<?>> list = injectedConfiguration.get(fieldValue);
				if (list == null) {
					if (field.getAnnotation(Optional.class) == null) {
						addDeploymentError(discovery, "Found defined configuartion that is not used: "
								+ configClass + "." + field.getName() + "=" + fieldValue);
					} else {
						LOGGER.info(
								"Found defined configuartion (marked as optional) that is not used: {}.{}={}",
								configClass, field.getName(), fieldValue);
					}
				} else {
					checked.add(fieldValue.toString());
				}
			}
		}

		Set<String> all = new HashSet<String>(injectedConfiguration.keySet());
		all.removeAll(checked);
		if (!all.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			for (String string : all) {
				List<AnnotatedField<?>> list = injectedConfiguration.get(string);
				for (AnnotatedField<?> annotatedField : list) {
					Field field = annotatedField.getJavaMember();
					builder.append("\n").append(annotatedField.getDeclaringType().getJavaClass())
							.append(".").append(field.getName());
				}
			}
			addDeploymentError(discovery,
					"Found configuration that is not defined in any configuration file and is not documented: "
							+ builder.toString());
		}
	}

	/**
	 * Adds the deployment error. If configuration validation is disabled then then the error will
	 * only be printed to the log file.
	 * 
	 * @param discovery
	 *            the discovery
	 * @param errorMsg
	 *            the error message to add or print
	 */
	private void addDeploymentError(AfterBeanDiscovery discovery, String errorMsg) {
		if (!disableConfigurationValidation) {
			discovery.addDefinitionError(new EmfConfigurationException(errorMsg));
		} else {
			LOGGER.warn(errorMsg);
		}
	}

	/**
	 * Process configuration injection point.
	 *
	 * @param <X>
	 *            the generic type
	 * @param pit
	 *            the pit
	 */
	public <X> void processConfigurationInjectionPoint(@Observes ProcessInjectionTarget<X> pit) {
		processConfiguredFields(pit.getAnnotatedType());
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
			Config config = annotatedField.getAnnotation(Config.class);
			if (config != null) {
				CollectionUtils.addValueToMap(injectedConfiguration, config.name(), annotatedField);
			}
		}
	}

	/**
	 * Gets a list of classes that are annotated with {@link Documentation} annotation and implement
	 * the given interface or extend the given class.
	 *
	 * @param <T>
	 *            the generic type
	 * @param clazz
	 *            the clazz
	 * @return the typed classes
	 */
	public <T> List<Class<?>> getTypedClasses(Class<T> clazz) {
		if (clazz == null) {
			return Collections.emptyList();
		}
		List<Class<?>> list = new LinkedList<Class<?>>();
		for (Class<?> c : documentedClasses) {
			if (clazz.isAssignableFrom(c)) {
				list.add(c);
			}
		}
		return list;
	}

	/**
	 * Gets all registered classes.
	 *
	 * @return the all classes
	 */
	public List<Class<?>> getAllClasses() {
		return Collections.unmodifiableList(new LinkedList<Class<?>>(documentedClasses));
	}

}
