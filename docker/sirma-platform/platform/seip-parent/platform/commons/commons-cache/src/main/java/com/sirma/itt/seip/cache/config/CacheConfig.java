package com.sirma.itt.seip.cache.config;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.util.Map;

import com.sirma.itt.seip.cache.CacheTransactionMode;
import com.sirma.itt.seip.cache.LockIsolation;
import com.sirma.itt.seip.cache.TransactionLocking;

/**
 * Represents a DTO with possible cache configurations passed via external configuration source
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 26/08/2018
 */
class CacheConfig {
	private Integer maxEntries;
	private String evictionStrategy;
	private CacheTransactionMode transactionMode;
	private TransactionLocking transactionLocking;
	private Long expirationIdleMillis;
	private Long lifespan;
	private Long expirationInterval;
	private LockIsolation lockIsolation;
	private Integer lockAcquireTimeout;
	private Integer concurrencyLevel;

	CacheConfig(Map<String, Object> configs) {
		maxEntries = readInt(configs, "max-entries");
		evictionStrategy = (String) configs.get("eviction-strategy");
		transactionMode = CacheTransactionMode.parse((String) configs.get("transaction-mode"));
		transactionLocking = TransactionLocking.parse((String) configs.get("transaction-locking"));
		expirationIdleMillis = readLong(configs, "expiration-idle-millis");
		lifespan = readLong(configs, "lifespan");
		expirationInterval = readLong(configs, "expiration-interval");
		lockIsolation = LockIsolation.parse((String) configs.get("lock-isolation"));
		lockAcquireTimeout = readInt(configs, "lock-acquire-timeout");
		concurrencyLevel = readInt(configs, "concurrency-level");
	}

	private Long readLong(Map<String, Object> configs, String longKey) {
		Object value = configs.get(longKey);
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		throw new IllegalArgumentException(
				"Expected value of type java.lang.Long but got " + value.getClass().getName());
	}

	private Integer readInt(Map<String, Object> configs, String intKey) {
		Object value = configs.get(intKey);
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		throw new IllegalArgumentException(
				"Expected value of type java.lang.Integer but got " + value.getClass().getName());
	}

	Integer getMaxEntries() {
		return maxEntries;
	}

	CacheConfig setMaxEntries(Integer maxEntries) {
		this.maxEntries = maxEntries;
		return this;
	}

	String getEvictionStrategy() {
		return evictionStrategy;
	}

	CacheConfig setEvictionStrategy(String evictionStrategy) {
		this.evictionStrategy = evictionStrategy;
		return this;
	}

	CacheTransactionMode getTransactionMode() {
		return transactionMode;
	}

	CacheConfig setTransactionMode(CacheTransactionMode transactionMode) {
		this.transactionMode = transactionMode;
		return this;
	}

	TransactionLocking getTransactionLocking() {
		return transactionLocking;
	}

	CacheConfig setTransactionLocking(TransactionLocking transactionLocking) {
		this.transactionLocking = transactionLocking;
		return this;
	}

	Long getExpirationIdleMillis() {
		return expirationIdleMillis;
	}

	CacheConfig setExpirationIdleMillis(Long expirationIdleMillis) {
		this.expirationIdleMillis = expirationIdleMillis;
		return this;
	}

	Long getLifespan() {
		return lifespan;
	}

	CacheConfig setLifespan(Long lifespan) {
		this.lifespan = lifespan;
		return this;
	}

	Long getExpirationInterval() {
		return expirationInterval;
	}

	CacheConfig setExpirationInterval(Long expirationInterval) {
		this.expirationInterval = expirationInterval;
		return this;
	}

	LockIsolation getLockIsolation() {
		return lockIsolation;
	}

	CacheConfig setLockIsolation(LockIsolation lockIsolation) {
		this.lockIsolation = lockIsolation;
		return this;
	}

	Integer getLockAcquireTimeout() {
		return lockAcquireTimeout;
	}

	CacheConfig setLockAcquireTimeout(Integer lockAcquireTimeout) {
		this.lockAcquireTimeout = lockAcquireTimeout;
		return this;
	}

	Integer getConcurrencyLevel() {
		return concurrencyLevel;
	}

	CacheConfig setConcurrencyLevel(Integer concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
		return this;
	}

	CacheConfig copyFrom(CacheConfig baseCacheConfig) {
		maxEntries = getOrDefault(maxEntries, baseCacheConfig.getMaxEntries());
		evictionStrategy = getOrDefault(evictionStrategy, baseCacheConfig.getEvictionStrategy());
		transactionMode = getOrDefault(transactionMode, baseCacheConfig.getTransactionMode());
		transactionLocking = getOrDefault(transactionLocking, baseCacheConfig.getTransactionLocking());
		expirationIdleMillis = getOrDefault(expirationIdleMillis, baseCacheConfig.getExpirationIdleMillis());
		lifespan = getOrDefault(lifespan, baseCacheConfig.getLifespan());
		expirationInterval = getOrDefault(expirationInterval, baseCacheConfig.getExpirationInterval());
		lockIsolation = getOrDefault(lockIsolation, baseCacheConfig.getLockIsolation());
		lockAcquireTimeout = getOrDefault(lockAcquireTimeout, baseCacheConfig.getLockAcquireTimeout());
		concurrencyLevel = getOrDefault(concurrencyLevel, baseCacheConfig.getConcurrencyLevel());
		return this;
	}

	@Override
	public String toString() {
		return new StringBuilder(300)
				.append("{")
				.append("maxEntries: ").append(maxEntries)
				.append(", evictionStrategy: '").append(evictionStrategy).append('\'')
				.append(", transactionMode: ").append(transactionMode)
				.append(", transactionLocking: ").append(transactionLocking)
				.append(", expirationIdleMillis: ").append(expirationIdleMillis)
				.append(", lifespan: ").append(lifespan)
				.append(", expirationInterval: ").append(expirationInterval)
				.append(", lockIsolation: ").append(lockIsolation)
				.append(", lockAcquireTimeout: ").append(lockAcquireTimeout)
				.append(", concurrencyLevel: ").append(concurrencyLevel)
				.append('}')
				.toString();
	}
}
