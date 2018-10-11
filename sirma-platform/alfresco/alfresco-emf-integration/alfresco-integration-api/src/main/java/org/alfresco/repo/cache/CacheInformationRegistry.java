package org.alfresco.repo.cache;

/**
 * The CacheInformationRegistry is responsible to handle statistic information about the configured
 * caches in the system.
 */
public interface CacheInformationRegistry {

	/**
	 * The Enum CacheStatus.
	 */
	enum CacheStatus {

		/** The cache full. */
		FULL,
		/** General info. */
		INFO,
		/** Put info. */
		PUT;
	}

	/**
	 * Register the cache to the service.
	 *
	 * @param cache
	 *            the cache to register
	 * @param cacheName
	 *            the cache id
	 */
	public void register(TransactionalCache<?, ?> cache, String cacheName);

	/**
	 * Register the cache to the service.
	 *
	 * @param cache
	 *            the cache to register
	 * @param cacheName
	 *            the cache id
	 */
	public void register(SimpleCache<?, ?> cache, String cacheName);

	/**
	 * Indicate some status for the cache. Might provide additional information
	 *
	 * @param id
	 *            the cache id
	 * @param status
	 *            the status
	 * @param additionalMessage
	 *            is some additional message
	 */
	public void indicate(String id, CacheStatus status, String additionalMessage);

}
