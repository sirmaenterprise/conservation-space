package com.sirma.itt.cmf.cache.extension;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheConfigurationProvider;
import com.sirma.itt.seip.cache.CacheTransactionMode;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.LockIsolation;
import com.sirma.itt.seip.cache.Locking;
import com.sirma.itt.seip.cache.Transaction;
import com.sirma.itt.seip.cache.TransactionLocking;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Base provider for cache configurations. It is initialized by CDI extention that scans classes for
 * {@link CacheConfiguration} annotaions.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/08/2018
 */
@Singleton
public class AnnotationCacheConfigurationProvider implements CacheConfigurationProvider {

	private static Map<String, CacheConfiguration> parsedConfigurations;

	/**
	 * Initialize the class instance state. It's initialzed statically as axtensions does
	 * not support injections
	 *
	 * @param configurations the found configurations
	 */
	static void initialize(Map<String, List<CacheConfiguration>> configurations) {
		Map<String, CacheConfiguration> parsedConfigs = new TreeMap<>();
		List<String> configErrors = new LinkedList<>();
		for (Map.Entry<String, List<CacheConfiguration>> entry : configurations.entrySet()) {
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
	}

	/**
	 * Gets the configurations.
	 *
	 * @return the configurations
	 */
	@Override
	public Map<String, CacheConfiguration> getConfigurations() {
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
	 * The Class CacheConfigurationBinding.
	 */
	private static class CacheConfigurationBinding extends AnnotationLiteral<CacheConfiguration>
			implements CacheConfiguration {

		private static final long serialVersionUID = -6311663943850891524L;

		private final String name;

		private final transient Eviction eviction;

		private final transient Expiration expiration;

		private final transient Transaction transaction;

		private final transient Locking locking;

		private final transient Documentation doc;

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
		CacheConfigurationBinding(String name, Transaction transaction, Eviction eviction, Expiration expiration,
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
		public Expiration expiration() {
			return expiration;
		}

		@Override
		public Documentation doc() {
			return doc;
		}

		@Override
		public Eviction eviction() {
			return eviction;
		}

		@Override
		public Locking locking() {
			return locking;
		}

		@Override
		public Transaction transaction() {
			return transaction;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof CacheConfigurationBinding)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			CacheConfigurationBinding that = (CacheConfigurationBinding) o;
			return Objects.equals(name, that.name) &&
					Objects.equals(eviction, that.eviction) &&
					Objects.equals(expiration, that.expiration) &&
					Objects.equals(transaction, that.transaction) &&
					Objects.equals(locking, that.locking) &&
					Objects.equals(doc, that.doc);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), name, eviction, expiration, transaction, locking, doc);
		}
	}

	/**
	 * The Class EvictionBinding.
	 */
	private static class EvictionBinding extends AnnotationLiteral<Eviction>implements Eviction {

		private static final long serialVersionUID = -3672622852040146229L;

		private String strategy;
		private int maxEntries;

		/**
		 * Instantiates a new eviction binding.
		 *
		 * @param strategy
		 *            the strategy
		 * @param maxEntries
		 *            the max entries
		 */
		EvictionBinding(String strategy, int maxEntries) {
			this.strategy = strategy;
			this.maxEntries = maxEntries;
		}

		@Override
		public String strategy() {
			return strategy;
		}

		@Override
		public int maxEntries() {
			return maxEntries;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof EvictionBinding)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			EvictionBinding that = (EvictionBinding) o;
			return maxEntries == that.maxEntries &&
					Objects.equals(strategy, that.strategy);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), strategy, maxEntries);
		}
	}

	/**
	 * The Class ExpirationBinding.
	 */
	private static class ExpirationBinding extends AnnotationLiteral<Expiration>implements Expiration {

		private static final long serialVersionUID = -6106948943365925328L;

		private long maxIdle;
		private long interval;
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
		ExpirationBinding(long maxIdle, long interval, long lifespan) {
			this.maxIdle = maxIdle;
			this.interval = interval;
			this.lifespan = lifespan;
		}

		@Override
		public long maxIdle() {
			return maxIdle;
		}

		@Override
		public long interval() {
			return interval;
		}

		@Override
		public long lifespan() {
			return lifespan;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof ExpirationBinding)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			ExpirationBinding that = (ExpirationBinding) o;
			return maxIdle == that.maxIdle &&
					interval == that.interval &&
					lifespan == that.lifespan;
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), maxIdle, interval, lifespan);
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
		TransactionBinding(CacheTransactionMode mode, TransactionLocking locking, int stopTimeout) {
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

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof TransactionBinding)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			TransactionBinding that = (TransactionBinding) o;
			return stopTimeout == that.stopTimeout &&
					locking == that.locking &&
					mode == that.mode;
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), stopTimeout, locking, mode);
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
		LockingBinding(LockIsolation isolation, boolean striping, int acquireTimeout, int concurrencyLevel) {
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

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof LockingBinding)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			LockingBinding that = (LockingBinding) o;
			return striping == that.striping &&
					acquireTimeout == that.acquireTimeout &&
					concurrencyLevel == that.concurrencyLevel &&
					isolation == that.isolation;
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), isolation, striping, acquireTimeout, concurrencyLevel);
		}
	}

	/**
	 * The Class DocumentationBinding.
	 */
	private static class DocumentationBinding extends AnnotationLiteral<Documentation> implements Documentation {

		private static final long serialVersionUID = 4583545990808039736L;
		private String value;

		/**
		 * Instantiates a new documentation binding.
		 *
		 * @param value
		 *            the value
		 */
		DocumentationBinding(String value) {
			this.value = value;
		}

		@Override
		public String value() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof DocumentationBinding)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			DocumentationBinding that = (DocumentationBinding) o;
			return Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), value);
		}
	}
}
