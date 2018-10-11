package com.sirma.itt.cmf.cache.extension;

import java.lang.invoke.MethodHandles;
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
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheConfigurationProvider;
import com.sirma.itt.seip.cache.CacheConfigurations;
import com.sirma.itt.seip.cache.CacheTransactionMode;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.LockIsolation;
import com.sirma.itt.seip.cache.Locking;
import com.sirma.itt.seip.cache.Transaction;
import com.sirma.itt.seip.cache.TransactionLocking;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Extension to collect the required cache configuration. The information later can be used for configuration
 * validation.
 *
 * @author BBonev
 */
@Singleton
public class CacheConfigurationExtension implements Extension, CacheConfigurationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/**
	 * If the given file is found in the meta-inf folder then the configuration validation will be disabled! The
	 * validation will be performed but the server will not fail but only print the errors.
	 */
	public static final String DISABLE_CONFIGURATION_VALIDATION = "/META-INF/services/com.sirma.itt.emf.DisableCacheConfigurationValidation";

	/** The injected configuration. */
	private Map<String, List<Type>> injectedConfiguration = new TreeMap<>();

	private Map<String, List<CacheConfiguration>> configurations = new TreeMap<>();
	private Map<String, CacheConfiguration> parsedConfigurations;

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
				} else {
					// missing name configuration
				}
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
	@Override
	public Map<String, CacheConfiguration> getConfigurations() {
		if (parsedConfigurations != null) {
			return parsedConfigurations;
		}
		Map<String, CacheConfiguration> parsedConfigs = new TreeMap<>();
		List<String> configErrors = new LinkedList<>();
		for (Entry<String, List<CacheConfiguration>> entry : configurations.entrySet()) {
			Transaction transaction = null;
			Eviction eviction = null;
			Expiration expiration = null;
			String documentation = null;
			Locking locking = null;
			for (CacheConfiguration configuration : entry.getValue()) {
				transaction = mergeTransaction(configErrors, entry.getKey(), transaction, configuration);
				locking = mergeLocking(configErrors, entry.getKey(), locking, configuration);
				eviction = mergeEviction(configErrors, entry.getKey(), eviction, configuration);
				expiration = mergeExpiration(expiration, configuration);
				documentation = mergeDocumantion(documentation, configuration);
			}
			parsedConfigs.put(entry.getKey(), new CacheConfigurationBinding(entry.getKey(), transaction, eviction,
					expiration, locking, new DocumentationBinding(documentation)));
		}
		if (!configErrors.isEmpty()) {
			throw new ConfigurationException("Invalid cache configurations defined:\n" + configErrors);
		}
		parsedConfigurations = parsedConfigs;
		return parsedConfigurations;
	}

	@Override
	public CacheConfiguration getConfiguration(String name) {
		return getConfigurations().get(name);
	}

	private static String mergeDocumantion(String current, CacheConfiguration configuration) {
		String documentation = current;

		if (configuration.doc() != null) {

			if (documentation == null) {
				documentation = configuration.doc().value();
			} else if (!documentation.contains(configuration.doc().value())) {
				documentation += System.lineSeparator() + configuration.doc().value();
			}
		}

		return documentation;
	}

	private static Expiration mergeExpiration(Expiration current, CacheConfiguration configuration) {
		Expiration expiration = current;
		if (expiration == null) {
			expiration = configuration.expiration();
		} else if (configuration.expiration() != null) {
			expiration = new ExpirationBinding(Math.max(expiration.maxIdle(), configuration.expiration().maxIdle()),
					Math.max(expiration.interval(), configuration.expiration().interval()),
					configuration.expiration().lifespan());
		}
		return expiration;
	}

	private static Eviction mergeEviction(List<String> configErrors, String name, Eviction current,
			CacheConfiguration configuration) {
		Eviction eviction = current;
		if (eviction == null) {
			eviction = configuration.eviction();
		} else if (configuration.eviction() != null) {
			if (!EqualsHelper.nullSafeEquals(eviction.strategy(), configuration.eviction().strategy(), true)) {
				configErrors.add(name + " has more then one eviction strategy definitions: " + eviction.strategy()
						+ ", " + configuration.eviction().strategy() + System.lineSeparator());
			}
			int max = Math.max(eviction.maxEntries(), configuration.eviction().maxEntries());
			eviction = new EvictionBinding(eviction.strategy(), max);
		}
		return eviction;
	}

	private static Locking mergeLocking(List<String> configErrors, String name, Locking current,
			CacheConfiguration configuration) {
		Locking locking = current;
		if (locking == null) {
			locking = configuration.locking();
		} else if (configuration.locking() != null) {
			if (!EqualsHelper.nullSafeEquals(locking.isolation(), configuration.locking().isolation())) {
				configErrors.add(name + " has more then one locking isolation definitions: " + locking.isolation()
						+ ", " + configuration.locking().isolation() + System.lineSeparator());
			}
			if (locking.striping() != configuration.locking().striping()) {
				configErrors.add(name + " has different lock striping definitions: " + locking.striping() + ", "
						+ configuration.locking().striping() + System.lineSeparator());
			}
			int maxAcquireTimeout = Math.max(locking.acquireTimeout(), configuration.locking().acquireTimeout());
			int maxConcurrencyLevel = Math.max(locking.concurrencyLevel(), configuration.locking().concurrencyLevel());
			locking = new LockingBinding(locking.isolation(), locking.striping(), maxAcquireTimeout,
					maxConcurrencyLevel);
		}
		return locking;
	}

	private static Transaction mergeTransaction(List<String> configErrors, String name, Transaction current,
			CacheConfiguration configuration) {
		Transaction transaction = current;
		if (transaction == null) {
			transaction = configuration.transaction();
		} else if (configuration.transaction() != null) {
			if (!EqualsHelper.nullSafeEquals(transaction.mode(), configuration.transaction().mode())) {
				configErrors.add(name + " has more then one transaction mode definitions: " + transaction.mode() + ", "
						+ configuration.transaction().mode() + System.lineSeparator());
			}
			if (!EqualsHelper.nullSafeEquals(transaction.locking(), configuration.transaction().locking())) {
				configErrors.add(name + " has more then one transaction locking definitions: " + transaction.locking()
						+ ", " + configuration.transaction().locking() + System.lineSeparator());
			}
			int max = Math.max(transaction.stopTimeout(), configuration.transaction().stopTimeout());
			transaction = new TransactionBinding(transaction.mode(), transaction.locking(), max);
		}
		return transaction;
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

		private static final long serialVersionUID = -6311663943850891524L;

		private final String name;

		private final Eviction eviction;

		private final Expiration expiration;

		private final Transaction transaction;

		private final Locking locking;

		private final Documentation doc;

		/**
		 * Instantiates a new cache configuration binding.
		 *
		 * @param name
		 *            the name
		 * @param transaction
		 *            the transaction
		 * @param eviction
		 *            the eviction
		 * @param expiration
		 *            the expiration
		 * @param locking
		 *            the locking
		 * @param doc
		 *            the doc
		 */
		public CacheConfigurationBinding(String name, Transaction transaction, Eviction eviction, Expiration expiration,
				Locking locking, Documentation doc) {
			this.name = name;
			this.transaction = transaction;
			this.eviction = eviction;
			this.expiration = expiration;
			this.locking = locking;
			this.doc = doc;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public Eviction eviction() {
			return eviction;
		}

		@Override
		public Expiration expiration() {
			return expiration;
		}

		@Override
		public Documentation doc() {
			return doc;
		}

		@Override
		public Transaction transaction() {
			return transaction;
		}

		@Override
		public Locking locking() {
			return locking;
		}
	}

	/**
	 * The Class EvictionBinding.
	 */
	private static class EvictionBinding extends AnnotationLiteral<Eviction>implements Eviction {

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
	private static class ExpirationBinding extends AnnotationLiteral<Expiration>implements Expiration {

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
	 * The Class TransactionBinding.
	 *
	 * @author BBonev
	 */
	private static class TransactionBinding extends AnnotationLiteral<Transaction>implements Transaction {
		private static final long serialVersionUID = 8157520544037557996L;
		private final int stopTimeout;
		private final TransactionLocking locking;
		private final CacheTransactionMode mode;

		/**
		 * Instantiates a new transaction binding.
		 *
		 * @param mode
		 *            the mode
		 * @param locking
		 *            the locking
		 * @param stopTimeout
		 *            the stop timeout
		 */
		public TransactionBinding(CacheTransactionMode mode, TransactionLocking locking, int stopTimeout) {
			this.mode = mode;
			this.locking = locking;
			this.stopTimeout = stopTimeout;
		}

		@Override
		public CacheTransactionMode mode() {
			return mode;
		}

		@Override
		public TransactionLocking locking() {
			return locking;
		}

		@Override
		public int stopTimeout() {
			return stopTimeout;
		}
	}

	/**
	 * The Class LockingBinding.
	 *
	 * @author BBonev
	 */
	private static class LockingBinding extends AnnotationLiteral<Locking>implements Locking {
		private static final long serialVersionUID = 8360307535562411515L;
		private final LockIsolation isolation;
		private final boolean striping;
		private final int acquireTimeout;
		private final int concurrencyLevel;

		/**
		 * Instantiates a new locking binding.
		 *
		 * @param isolation
		 *            the isolation
		 * @param striping
		 *            the striping
		 * @param acquireTimeout
		 *            the acquire timeout
		 * @param concurrencyLevel
		 *            the concurrency level
		 */
		public LockingBinding(LockIsolation isolation, boolean striping, int acquireTimeout, int concurrencyLevel) {
			this.isolation = isolation;
			this.striping = striping;
			this.acquireTimeout = acquireTimeout;
			this.concurrencyLevel = concurrencyLevel;
		}

		@Override
		public LockIsolation isolation() {
			return isolation;
		}

		@Override
		public boolean striping() {
			return striping;
		}

		@Override
		public int acquireTimeout() {
			return acquireTimeout;
		}

		@Override
		public int concurrencyLevel() {
			return concurrencyLevel;
		}

	}

	/**
	 * The Class DocumentationBinding.
	 */
	private static class DocumentationBinding extends AnnotationLiteral<Documentation>implements Documentation {

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
