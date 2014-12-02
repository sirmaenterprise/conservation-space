package com.sirma.itt.cmf.cache.extension;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheConfigurations;
import com.sirma.itt.emf.cache.CacheTransactionMode;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Extension to collect the required cache configuration. The information later can be used for
 * configuration validation.
 * 
 * @author BBonev
 */
public class CacheConfigurationExtension implements Extension {

	/**
	 * If the given file is found in the meta-inf folder then the configuration validation will be
	 * disabled! The validation will be performed but the server will not fail but only print the
	 * errors.
	 */
	public static final String DISABLE_CONFIGURATION_VALIDATION = "/META-INF/services/com.sirma.itt.emf.DisableCacheConfigurationValidation";

	/** The injected configuration. */
	private static Map<String, List<Type>> injectedConfiguration = new TreeMap<String, List<Type>>();

	/** The configurations. */
	private static Map<String, List<CacheConfiguration>> configurations = new TreeMap<String, List<CacheConfiguration>>();

	/** The disable configuration validation. */
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
			System.err.println("Disabled configuration validation!");
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
		if (type.isAnnotationPresent(CacheConfiguration.class)) {
			CacheConfiguration configuration = type.getAnnotation(CacheConfiguration.class);
			if (StringUtils.isNotNullOrEmpty(configuration.name())) {
				CollectionUtils.addValueToMap(injectedConfiguration, configuration.name(),
						type.getJavaClass());
				CollectionUtils.addValueToMap(configurations, configuration.name(), configuration);
			}
		} else if (type.isAnnotationPresent(CacheConfigurations.class)) {
			CacheConfigurations configs = type.getAnnotation(CacheConfigurations.class);
			if ((configs.value() != null) && (configs.value().length > 0)) {
				for (CacheConfiguration configuration : configs.value()) {
					if (StringUtils.isNotNullOrEmpty(configuration.name())) {
						CollectionUtils.addValueToMap(injectedConfiguration, configuration.name(),
								type.getJavaClass());
						CollectionUtils.addValueToMap(configurations, configuration.name(),
								configuration);
					}
				}
			}
		}
		// process the fields of all classes
		processConfiguredFields(type);
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
				if (StringUtils.isNullOrEmpty(config.name())) {
					Object value = null;
					try {
						Field field = annotatedField.getJavaMember();
						boolean isAccsessible = field.isAccessible();
						if (!isAccsessible) {
							field.setAccessible(true);
						}
						value = field.get(annotatedField.getDeclaringType().getJavaClass());
						if (!isAccsessible) {
							field.setAccessible(isAccsessible);
						}
					} catch (Exception e) {
					}
					if (value instanceof String) {
						configName = (String) value;
					}
				}

				if (StringUtils.isNotNullOrEmpty(configName)) {
					CollectionUtils.addValueToMap(configurations, configName, config);
					CollectionUtils.addValueToMap(injectedConfiguration, configName, annotatedField
							.getDeclaringType().getJavaClass());
				} else {
					// missing name configuration
				}
			}
		}
	}

	/**
	 * Getter method for injectedConfiguration.
	 *
	 * @return the injectedConfiguration
	 */
	public Map<String, List<Type>> getInjectedConfiguration() {
		return injectedConfiguration;
	}

	/**
	 * Gets the configurations.
	 * 
	 * @return the configurations
	 */
	public Map<String, CacheConfiguration> getConfigurations() {
		Map<String, CacheConfiguration> parsedConfigs = new TreeMap<String, CacheConfiguration>();
		List<String> configErrors = new LinkedList<>();
		for (Entry<String, List<CacheConfiguration>> entry : configurations.entrySet()) {
			String container = null;
			CacheTransactionMode transactionMode = null;
			Eviction eviction = null;
			Expiration expiration = null;
			String documentation = null;
			for (CacheConfiguration configuration : entry.getValue()) {
				if (container == null) {
					container = configuration.container();
				} else if (!EqualsHelper.nullSafeEquals(container, configuration.container(), true)) {
					configErrors
							.add(entry.getKey() + " has more then one container definitions: "
									+ container + ", " + configuration.container()
									+ System.lineSeparator());
				}
				if (transactionMode == null) {
					transactionMode = configuration.transaction();
				} else if ((configuration.transaction() != null)
						&& (transactionMode != configuration.transaction())) {
					configErrors.add(entry.getKey()
							+ " has more then one transaction mode definitions: " + transactionMode
							+ ", " + configuration.transaction() + System.lineSeparator());
				}
				if (eviction == null) {
					eviction = configuration.eviction();
				} else if (configuration.eviction() != null) {
					if (!EqualsHelper.nullSafeEquals(eviction.strategy(), configuration.eviction()
							.strategy(), true)) {
						configErrors.add(entry.getKey()
								+ " has more then one eviction strategy definitions: "
								+ eviction.strategy() + ", " + configuration.eviction().strategy()
								+ System.lineSeparator());
					}
					int max = Math
							.max(eviction.maxEntries(), configuration.eviction().maxEntries());
					eviction = new EvictionBinding(eviction.strategy(), max);
				}
				if (expiration == null) {
					expiration = configuration.expiration();
				} else if (configuration.expiration() != null) {
					expiration = new ExpirationBinding(Math.max(expiration.maxIdle(), configuration
							.expiration().maxIdle()), Math.max(expiration.interval(), configuration
							.expiration().interval()), configuration.expiration().lifespan());
				}
				if (configuration.doc() != null) {
					if (documentation == null) {
						documentation = configuration.doc().value();
					} else {
						if (!documentation.contains(configuration.doc().value())) {
							documentation += System.lineSeparator() + configuration.doc().value();
						}
					}
				}
			}
			parsedConfigs.put(entry.getKey(), new CacheConfigurationBinding(entry.getKey(),
					container, transactionMode, eviction, expiration, new DocumentationBinding(
							documentation)));
		}
		if (!configErrors.isEmpty()) {
			throw new EmfConfigurationException("Invalid cache configurations defined:\n"
					+ configErrors);
		}
		return parsedConfigs;
	}

	/**
	 * Getter method for disableConfigurationValidation.
	 *
	 * @return the disableConfigurationValidation
	 */
	public boolean isDisableConfigurationValidation() {
		return disableConfigurationValidation;
	}

	/**
	 * The Class CacheConfigurationBinding.
	 */
	private static class CacheConfigurationBinding extends AnnotationLiteral<CacheConfiguration>
			implements CacheConfiguration {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -6311663943850891524L;

		/** The name. */
		private String name;

		/** The container. */
		private String container;

		/** The transaction mode. */
		private CacheTransactionMode transactionMode;

		/** The eviction. */
		private Eviction eviction;

		/** The expiration. */
		private Expiration expiration;

		/** The doc. */
		private Documentation doc;

		/**
		 * Instantiates a new cache configuration binding.
		 * 
		 * @param name
		 *            the name
		 * @param container
		 *            the container
		 * @param transactionMode
		 *            the transaction mode
		 * @param eviction
		 *            the eviction
		 * @param expiration
		 *            the expiration
		 * @param doc
		 *            the doc
		 */
		public CacheConfigurationBinding(String name, String container,
				CacheTransactionMode transactionMode, Eviction eviction, Expiration expiration,
				Documentation doc) {
			this.name = name;
			this.container = container;
			this.transactionMode = transactionMode;
			this.eviction = eviction;
			this.expiration = expiration;
			this.doc = doc;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String name() {
			return name;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String container() {
			return container;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public CacheTransactionMode transaction() {
			return transactionMode;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Eviction eviction() {
			return eviction;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Expiration expiration() {
			return expiration;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Documentation doc() {
			return doc;
		}
	}

	/**
	 * The Class EvictionBinding.
	 */
	private static class EvictionBinding extends AnnotationLiteral<Eviction> implements Eviction {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -3672622852040146229L;

		/** The strategy. */
		private String strategy;

		/** The max entries. */
		private int maxEntries;

		/**
		 * Instantiates a new eviction binding.
		 * 
		 * @param strategy
		 *            the strategy
		 * @param maxEntries
		 *            the max entries
		 */
		public EvictionBinding(String strategy, int maxEntries) {
			this.strategy = strategy;
			this.maxEntries = maxEntries;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String strategy() {
			return strategy;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int maxEntries() {
			return maxEntries;
		}

	}

	/**
	 * The Class ExpirationBinding.
	 */
	private static class ExpirationBinding extends AnnotationLiteral<Expiration> implements
			Expiration {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -6106948943365925328L;

		/** The max idle. */
		private long maxIdle;

		/** The interval. */
		private long interval;

		/** The lifespan. */
		private long lifespan;

		/**
		 * Instantiates a new expiration binding.
		 * 
		 * @param maxIdle
		 *            the max idle
		 * @param interval
		 *            the interval
		 * @param lifespan
		 *            the lifespan
		 */
		public ExpirationBinding(long maxIdle, long interval, long lifespan) {
			this.maxIdle = maxIdle;
			this.interval = interval;
			this.lifespan = lifespan;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long maxIdle() {
			return maxIdle;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long interval() {
			return interval;
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		public long lifespan() {
			return lifespan;
		}
	}

	/**
	 * The Class DocumentationBinding.
	 */
	private static class DocumentationBinding extends AnnotationLiteral<Documentation> implements Documentation {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = 4583545990808039736L;
		/** The value. */
		private String value;

		/**
		 * Instantiates a new documentation binding.
		 * 
		 * @param value
		 *            the value
		 */
		public DocumentationBinding(String value) {
			this.value = value;
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		public String value() {
			return value;
		}

	}
}
