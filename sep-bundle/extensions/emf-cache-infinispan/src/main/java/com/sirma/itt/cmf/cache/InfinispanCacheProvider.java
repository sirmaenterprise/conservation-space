package com.sirma.itt.cmf.cache;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.CacheConfigurationException;

import com.sirma.itt.cmf.cache.documentation.CacheApplicationDocumentationExtension;
import com.sirma.itt.cmf.cache.extension.CacheConfigurationExtension;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheProvider;
import com.sirma.itt.emf.cache.CacheProviderType;
import com.sirma.itt.emf.cache.SimpleCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Infinispan cache provider for CMF purposes.
 *
 * @author BBonev
 */
@CacheProviderType("infinispan")
public class InfinispanCacheProvider implements CacheProvider, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 248236786921931624L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(InfinispanCacheProvider.class);

	/** The trace. */
	private static boolean trace = LOGGER.isTraceEnabled();

	/** The Constant CACHE_JNDI_BASE. */
	public static final String CACHE_JNDI_BASE = "java:jboss/infinispan/";

	/** The base JNDI name for the cache configuration. */
	public static final String DEFAULT_CACHE_JNDI = "cmf";

	/** Default cache region. */
	public static final String DEFAULT_CACHE = "DEFAULT_CACHE_REGION";

	/** The cache JNDI taken from the configuration. */
	@Inject
	@Config(name = InfinispanConfig.CACHE_INFINISPAN_JNDI, defaultValue = DEFAULT_CACHE_JNDI)
	private String cacheJndis;

	/** The default cache name taken from the configuration. */
	@Inject
	@Config(name = InfinispanConfig.CACHE_INFINISPAN_DEFAULT_CACHE, defaultValue = DEFAULT_CACHE)
	private String defaultCacheName;

	/** The development mode. */
	@Inject
	@Config(name = EmfConfigurationProperties.APPLICATION_MODE_DEVELOPEMENT, defaultValue = "false")
	private Boolean developmentMode;

	/** The set of enabled JNDI names to work with. */
	private Set<String> cacheJndi;

	/** The initial context. */
	private InitialContext initialContext;

	/** The configuration extension. */
	@Inject
	private CacheConfigurationExtension configurationExtension;

	/** The detected configurations. */
	private Map<String, String> detectedConfigurations;

	/**
	 * Initialize configuration.
	 */
	@PostConstruct
	public void initializeConfiguration() {
		String[] split = cacheJndis.split("\\s*,\\s*");
		cacheJndi = CollectionUtils.createHashSet(split.length);
		cacheJndi.addAll(Arrays.asList(split));

		LOGGER.info("Started cache configuration validation...");
		Map<String, CacheConfiguration> configurations = configurationExtension.getConfigurations();
		detectedConfigurations = CollectionUtils.createLinkedHashMap(configurations.size());

		Map<String, CacheConfiguration> missingConfigurations = new LinkedHashMap<String, CacheConfiguration>();
		for (Entry<String, List<Type>> entry : configurationExtension.getInjectedConfiguration()
				.entrySet()) {
			SimpleCache<?, ?> cache = null;
			String prefferedContainer = configurations.get(entry.getKey()).container();
			String key = entry.getKey();
			if (!key.endsWith(EntityLookupCacheContext.REGION_SUFFIX)) {
				key += EntityLookupCacheContext.REGION_SUFFIX;
			}
			if (StringUtils.isNotNullOrEmpty(prefferedContainer)) {
				cache = lookupCache(prefferedContainer, key);
			}
			if (cache == null) {
				for (String jndi : cacheJndi) {
					if (EqualsHelper.nullSafeEquals(jndi, prefferedContainer, true)) {
						continue;
					}
					cache = lookupCache(jndi, key);
					if (cache != null) {
						detectedConfigurations.put(key, jndi);
						break;
					}
				}
			} else {
				detectedConfigurations.put(key, prefferedContainer);
			}
			if (cache == null) {
				missingConfigurations.put(entry.getKey(), configurations.get(entry.getKey()));
			}
		}
		if (!missingConfigurations.isEmpty()) {
			LOGGER.error("Detected not configured named caches: \n\n"
					+ missingConfigurations.entrySet() + "\n");
			LOGGER.warn("Update your cache configuration with the following EXAMPLE configuration."
					+ "\n\n\tNOTE: THIS IS NOT A PRODUCTION CONFIGURATION!\n\n"
					+ CacheApplicationDocumentationExtension
							.generateConfiguration(missingConfigurations));
			if (!developmentMode.booleanValue()) {
				if (!configurationExtension.isDisableConfigurationValidation()) {
					throw new EmfConfigurationException("Missing cache configurations: "
							+ missingConfigurations.entrySet());
				}
			}
		} else {
			LOGGER.info("All cache configurations are valid!");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K extends Serializable, V> SimpleCache<K, V> createCache() {
		return createCache(defaultCacheName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K extends Serializable, V> SimpleCache<K, V> createCache(String name) {
		SimpleCache<K, V> cache = null;
		String prefferedContainer = detectedConfigurations.get(name);
		if (prefferedContainer != null) {
			cache = lookupCache(prefferedContainer, name);
		}
		if (cache == null) {
			for (String jndi : cacheJndi) {
				if (EqualsHelper.nullSafeEquals(jndi, prefferedContainer, true)) {
					continue;
				}
				cache = lookupCache(jndi, name);
				if (cache != null) {
					break;
				}
			}
		}
		if (cache == null) {
			if (defaultCacheName.equals(name)) {
				throw new CacheConfigurationException("Default cache '" + defaultCacheName
						+ "' not found!");
			}
			LOGGER.warn("Named cache " + name + " not configured will return the default cache");
			return createCache();
		}
		return cache;
	}

	/**
	 * Lookup cache by JNDI name and creates a cache proxy.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param jndi
	 *            the JNDI suffix
	 * @param name
	 *            the name
	 * @return the cache proxy
	 */
	@SuppressWarnings("unchecked")
	private <K extends Serializable, V> SimpleCache<K, V> lookupCache(String jndi, String name) {
		// if (!cacheManager.isAmbiguous() && !cacheManager.isUnsatisfied()) {
		// return new InfinispanCache<K, V>((Cache<K, V>)
		// cacheManager.get().getCache(name, true));
		// }

		String jndiName = CACHE_JNDI_BASE + jndi + (jndi.endsWith("/") ? "" : "/") + name;
		if (trace) {
			LOGGER.trace("Going to lookup for cache region with JNDI name: " + jndiName);
		}
		try {
			Object lookup = getInitialContext().lookup(jndiName);
			// for some reason the returned instance is not a
			// org.infinispan.Cache instance so we use a java.util.Map interface
			// to access the cache
			// UPDATE: the reason of not returning the correct instance is that the infinispan
			// library need to be added into the class path via manifest.mf entry:
			// Dependencies: org.infinispan export
			if (lookup instanceof Cache) {
				if (trace) {
					LOGGER.trace("Created cache proxy for cache region " + jndiName
							+ " using org.infinispan.Cache");
				}
				return new InfinispanCache<K, V>((Cache<K, V>) lookup);
			} else if (lookup instanceof Map) {
				if (trace) {
					LOGGER.trace("Created cache proxy for cache region " + jndiName
							+ " using java.util.Map");
				}
				return new MapCacheProxy<K, V>((Map<K, V>) lookup);
			}
			LOGGER.warn("Cache not found for region " + name
					+ " due to no suitable cache proxy found. Cache configuration for " + jndiName
					+ " not found!");
			return null;
		} catch (NamingException e) {
			LOGGER.warn("Cache not found for region " + name
					+ " due to missing cache configuration for " + jndiName + " -> "
					+ e.getMessage());
			return null;
		}
	}

	/**
	 * Getter method for initialContext.
	 *
	 * @return the initialContext
	 */
	public InitialContext getInitialContext() {
		if (initialContext == null) {
			try {
				initialContext = new InitialContext();
			} catch (NamingException e) {
				throw new EmfConfigurationException(e);
			}
		}
		return initialContext;
	}

}
