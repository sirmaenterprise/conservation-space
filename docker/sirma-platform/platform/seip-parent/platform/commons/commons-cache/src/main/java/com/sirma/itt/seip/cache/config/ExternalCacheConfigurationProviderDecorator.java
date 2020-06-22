package com.sirma.itt.seip.cache.config;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.interceptor.Interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Decorator for {@link CacheConfigurationProvider} that adds external configuration integration for cache provisioning.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 26/08/2018
 * @see SystemCacheConfigurationLoader
 */
@Singleton
@Decorator
@Priority(Interceptor.Priority.APPLICATION)
public abstract class ExternalCacheConfigurationProviderDecorator implements CacheConfigurationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Delegate
	@Inject
	private CacheConfigurationProvider delegate;

	@Inject
	private SystemCacheConfigurationLoader provider;

	@Inject
	private SecurityContext securityContext;

	private Map<String, CacheConfiguration> cacheConfigurationMapping;

	@Override
	public CacheConfiguration getConfiguration(String name) {
		return this.getConfigurations().get(name);
	}

	@Override
	public Map<String, CacheConfiguration> getConfigurations() {
		if (cacheConfigurationMapping == null) {
			cacheConfigurationMapping = delegate.getConfigurations().entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey,
							entry -> wrapConfiguration(entry.getKey(), entry.getValue())));
		}
		return cacheConfigurationMapping;
	}

	private CacheConfiguration wrapConfiguration(String name, CacheConfiguration value) {
		Optional<CacheConfig> externalConfig = provider.findConfig(securityContext.getCurrentTenantId(), name);
		if (externalConfig.isPresent()) {
			LOGGER.info("Found external cache configuration about {}: {}", name, externalConfig.get());
			return new CacheConfigurationProxy(name, value, externalConfig.get());
		}
		return value;
	}

	private static class CacheConfigurationProxy extends AnnotationLiteral<CacheConfiguration>
			implements CacheConfiguration {

		private final String name;

		private final transient Eviction eviction;

		private final transient Expiration expiration;

		private final transient Transaction transaction;

		private final transient Locking locking;

		private final transient Documentation doc;

		CacheConfigurationProxy(String name, CacheConfiguration delegate, CacheConfig config) {
			this.name = name;
			this.transaction = new TransactionProxy(delegate.transaction(), config);
			this.eviction = new EvictionProxy(delegate.eviction(), config);
			this.expiration = new ExpirationProxy(delegate.expiration(), config);
			this.locking = new LockingProxy(delegate.locking(), config);
			this.doc = delegate.doc();
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

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof CacheConfigurationProxy)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			CacheConfigurationProxy that = (CacheConfigurationProxy) o;
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

	private static class EvictionProxy extends AnnotationLiteral<Eviction> implements Eviction {

		private final transient Eviction delegate;
		private final transient CacheConfig config;

		EvictionProxy(Eviction delegate, CacheConfig config) {
			this.delegate = delegate;
			this.config = config;
		}

		@Override
		public String strategy() {
			return getOrDefault(config.getEvictionStrategy(), delegate.strategy());
		}

		@Override
		public int maxEntries() {
			return getOrDefault(config.getMaxEntries(), delegate.maxEntries());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof EvictionProxy)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			EvictionProxy that = (EvictionProxy) o;
			return Objects.equals(delegate, that.delegate) &&
					Objects.equals(config, that.config);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), delegate, config);
		}
	}

	private static class ExpirationProxy extends AnnotationLiteral<Expiration> implements Expiration {

		private final transient Expiration delegate;
		private final transient CacheConfig config;

		ExpirationProxy(Expiration delegate, CacheConfig config) {
			this.delegate = delegate;
			this.config = config;
		}

		@Override
		public long maxIdle() {
			return getOrDefault(config.getExpirationIdleMillis(), delegate.maxIdle());
		}

		@Override
		public long interval() {
			return getOrDefault(config.getExpirationInterval(), delegate.interval());
		}

		@Override
		public long lifespan() {
			return getOrDefault(config.getLifespan(), delegate.lifespan());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof ExpirationProxy)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			ExpirationProxy that = (ExpirationProxy) o;
			return Objects.equals(delegate, that.delegate) &&
					Objects.equals(config, that.config);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), delegate, config);
		}
	}

	/**
	 * The Class TransactionBinding.
	 *
	 * @author BBonev
	 */
	private static class TransactionProxy extends AnnotationLiteral<Transaction> implements Transaction {
		private final transient Transaction delegate;
		private final transient CacheConfig config;

		TransactionProxy(Transaction delegate, CacheConfig config) {
			this.delegate = delegate;
			this.config = config;
		}

		@Override
		public CacheTransactionMode mode() {
			return getOrDefault(config.getTransactionMode(), delegate.mode());
		}

		@Override
		public TransactionLocking locking() {
			return getOrDefault(config.getTransactionLocking(), delegate.locking());
		}

		@Override
		public int stopTimeout() {
			return delegate.stopTimeout();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof TransactionProxy)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			TransactionProxy that = (TransactionProxy) o;
			return Objects.equals(delegate, that.delegate) &&
					Objects.equals(config, that.config);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), delegate, config);
		}
	}

	private static class LockingProxy extends AnnotationLiteral<Locking> implements Locking {

		private final transient Locking delegate;
		private final transient CacheConfig config;

		LockingProxy(Locking delegate, CacheConfig config) {
			this.delegate = delegate;
			this.config = config;
		}

		@Override
		public LockIsolation isolation() {
			return getOrDefault(config.getLockIsolation(), delegate.isolation());
		}

		@Override
		public boolean striping() {
			return delegate.striping();
		}

		@Override
		public int acquireTimeout() {
			return getOrDefault(config.getLockAcquireTimeout(), delegate.acquireTimeout());
		}

		@Override
		public int concurrencyLevel() {
			return getOrDefault(config.getConcurrencyLevel(), delegate.concurrencyLevel());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof LockingProxy)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			LockingProxy that = (LockingProxy) o;
			return Objects.equals(delegate, that.delegate) &&
					Objects.equals(config, that.config);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), delegate, config);
		}
	}
}

