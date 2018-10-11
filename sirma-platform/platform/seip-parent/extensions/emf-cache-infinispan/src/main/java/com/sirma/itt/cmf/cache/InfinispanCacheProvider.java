package com.sirma.itt.cmf.cache;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheConfigurationProvider;
import com.sirma.itt.seip.cache.CacheProvider;
import com.sirma.itt.seip.cache.CacheProviderType;
import com.sirma.itt.seip.cache.CacheRegister;
import com.sirma.itt.seip.cache.SimpleCache;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Infinispan cache provider for CMF purposes.
 *
 * @author BBonev
 */
@CacheProviderType("infinispan")
@ManagedBean
public class InfinispanCacheProvider implements CacheProvider, Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String REGION_SUFFIX = "_REGION";
	private static final long serialVersionUID = 248236786921931624L;
	public static final String CACHE_JNDI_BASE = "java:jboss/infinispan/container/";
	/**
	 * Default cache region. <br>
	 *
	 * <pre>
	 * {@code
	 *  <local-cache name="DEFAULT_CACHE_REGION">
	 *     <transaction mode="NONE"/>
	 *     <eviction strategy="NONE" max-entries="-1"/>
	 *     <expiration max-idle="-1" interval="60000"/>
	 * </local-cache>}
	 * </xml>
	 * </pre>
	 */
	// @CacheConfiguration(eviction = @Eviction(maxEntries = -1) )
	public static final String DEFAULT_CACHE = "DEFAULT_CACHE_REGION";

	/** The default cache name taken from the configuration. */
	private String defaultCacheName = DEFAULT_CACHE;

	@Resource(lookup = CACHE_JNDI_BASE + SecurityContext.SYSTEM_TENANT)
	private EmbeddedCacheManager cacheContainer;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private CacheConfigurationProvider configurationProvider;

	@Inject
	private Instance<CacheRegister> cacheRegister;

	@Override
	public <K extends Serializable, V> SimpleCache<K, V> createCache() {
		return lookupCache(defaultCacheName, false);
	}

	@Override
	public <K extends Serializable, V> SimpleCache<K, V> createCache(String name) {
		return lookupCache(name, true);
	}

	private String buildCacheName(String name) {
		if (securityContext.isDefaultTenant()) {
			return name;
		}
		return securityContext.getCurrentTenantId() + "_" + name;
	}

	/**
	 * Lookup cache in the {@link EmbeddedCacheManager} name and creates a cache proxy. If the cache does not exists new
	 * will be registered.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param name
	 *            the name
	 * @param forTenant
	 *            if <code>true</code> the cache will be searched in the context of the current tenant if
	 *            <code>false</code> it will be searched as is
	 * @return the cache proxy
	 */
	private <K extends Serializable, V> SimpleCache<K, V> lookupCache(String name, boolean forTenant) {
		String cacheName = forTenant ? buildCacheName(name) : name;
		if (!cacheExists(cacheName) && registerCache(name)) {
			return lookupCache(name, forTenant);
		}
		// the method getCache always returns non null instance
		// even if the register fails this will return a cache for unlimited items
		return new InfinispanCache<>(cacheContainer.getCache(cacheName));
	}

	/**
	 * Checks a cache for the given name has a defined configuration or not
	 *
	 * @param name
	 *            the name of the cache
	 * @return <code>true</code> if cache with such name is defined in infinispan and <code>false</code> if not.
	 */
	private boolean cacheExists(String name) {
		return cacheContainer.getCacheConfiguration(name) != null;
	}

	private boolean registerCache(String name) {
		CacheConfiguration configuration = configurationProvider.getConfiguration(name);
		if (configuration == null && name.endsWith(REGION_SUFFIX)) {
			String nameWithoutRegionSuffix = name.substring(0, name.length() - REGION_SUFFIX.length());
			configuration = configurationProvider.getConfiguration(nameWithoutRegionSuffix);
		}
		boolean cacheRegistered = false;
		if (configuration != null && !cacheRegister.isUnsatisfied()) {
			cacheRegistered = cacheRegister.get().registerCache(configuration);
		}
		if (!cacheRegistered) {
			LOGGER.warn("Could not register configuration for cache {}. "
					+ "The returned cache will have inlimited size and no eviction policy!", name);
		}
		return cacheRegistered;
	}

}
