package com.sirma.itt.seip.cache.config;

import static java.util.Collections.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;

import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Loader for infinispan cache configurations file. The file location should be passed as JVM argument like <br>
 * -D{@value #INFINISPAN_CACHE_CONFIG}=path-to-config-file.yaml. The accepted configuration format is YAML.
 * The file format is structured tenant.id&gt;cache_name&gt;configuration: value
 * <pre><code>
 *         system.tenant:
 *           cache_name_1:
 *             concurrency-level: 10
 *             eviction-strategy: UNORDERED
 *             expiration-idle-millis: 100
 *             expiration-interval: 111
 *             lifespan: 100
 *             lock-acquire-timeout: 100
 *             lock-isolation: READ_COMMITTED
 *             max-entries: 20
 *             transaction-locking: PESSIMISTIC
 *             transaction-mode: FULL_XA
 *
 *           cache_name_2:
 *             concurrency-level: 10
 *             eviction-strategy: UNORDERED
 *             expiration-idle-millis: 100
 *             expiration-interval: 111
 *             lifespan: 100
 *             lock-acquire-timeout: 100
 *             lock-isolation: READ_COMMITTED
 *             max-entries: 20
 *             transaction-locking: PESSIMISTIC
 *             transaction-mode: FULL_XA
 *
 *         some.tenant:
 *           cache_name_1:
 *             max-entries: 5000
 *             transaction-mode: FULL_XA
 *
 *           cache_name_2:
 *             max-entries: 2000
 *     </code>
 * </pre>
 * The configurations support inheritance from a tenant with name system.tenant. It such tenant is defined and has
 * configuration that is not defined in a concrete tenant it will be used from there. In the example above the
 * configuration {@code lock-isolation} for {@code cache_name_2} in {@code some.tenant} is {@code READ_COMMITTED} event
 * it's not defined
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 26/08/2018
 */
@Singleton
class SystemCacheConfigurationLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String INFINISPAN_CACHE_CONFIG = "infinispan.config";

	private Map<String, Map<String, CacheConfig>> configurations;
	private Map<String, CacheConfig> baseConfig;

	/**
	 * Find a configuration for the given tenant and cache name. If there is not explicitly defined cache name it will
	 * be taken from the base tenant config if any. If no such tenant is configured it will be taken from the base
	 * tenant if any.
	 *
	 * @param tenant the tenant to look for
	 * @param cacheName the cache name to resolve
	 * @return the found configuration if any
	 */
	Optional<CacheConfig> findConfig(String tenant, String cacheName) {
		Map<String, CacheConfig> tenantConfigurations = getConfigurations().get(tenant);
		if (tenantConfigurations == null) {
			return Optional.ofNullable(baseConfig.get(cacheName));
		}
		CacheConfig cacheConfig = tenantConfigurations.get(cacheName);
		if (cacheConfig == null) {
			cacheConfig = baseConfig.get(cacheName);
		}
		return Optional.ofNullable(cacheConfig);
	}

	private Map<String, Map<String, CacheConfig>> getConfigurations() {
		if (configurations != null) {
			return configurations;
		}
		String configLocation = System.getProperty(INFINISPAN_CACHE_CONFIG);
		if (StringUtils.isBlank(configLocation)) {
			LOGGER.info(
					"No external cache configuration found. To configure pass file location as -D{}=path-to-config.yaml",
					INFINISPAN_CACHE_CONFIG);
			configurations = Collections.emptyMap();
			baseConfig = Collections.emptyMap();
			return emptyMap();
		}
		File configFile = new File(configLocation);
		if (configFile.isFile() && configFile.canRead()) {
			LOGGER.info("Reading cache configurations from {}", configLocation);
			try (FileInputStream configStream = new FileInputStream(configFile)) {
				Yaml yaml = new Yaml();
				yaml.setBeanAccess(BeanAccess.FIELD);
				Map<String, Map<String, Map<String, Object>>> readConfig = yaml.loadAs(configStream, Map.class);
				buildConfigurations(readConfig);
			} catch (IOException | YAMLException e) {
				LOGGER.warn("Could not load cache config file: {}. Will use default configurations", configLocation, e);
				configurations = Collections.emptyMap();
				baseConfig = Collections.emptyMap();
			}
		} else {
			LOGGER.warn("Configuration -D{}={} does not point to a file", INFINISPAN_CACHE_CONFIG, configLocation);
			configurations = Collections.emptyMap();
			baseConfig = Collections.emptyMap();
		}
		return configurations;
	}

	private void buildConfigurations(Map<String, Map<String, Map<String, Object>>> readConfig) {
		configurations = new HashMap<>();
		readConfig.forEach((tenantId, tenantConfigs) -> tenantConfigs.forEach(
				(cacheName, cacheConfig) -> addConfig(tenantId, cacheName, cacheConfig)));

		baseConfig = resolveBaseConfig();
		copyBaseConfigToAllOther();
	}

	private void copyBaseConfigToAllOther() {
		configurations.entrySet()
				.stream()
				.filter(e -> !SecurityContext.isSystemTenant(e.getKey()))
				.map(Map.Entry::getValue)
				.forEach(tenantConfig -> tenantConfig.replaceAll(copyConfigFromBase()));
	}

	private Map<String, CacheConfig> resolveBaseConfig() {
		return configurations.getOrDefault(SecurityContext.SYSTEM_TENANT, new HashMap<>());
	}

	private BiFunction<String, CacheConfig, CacheConfig> copyConfigFromBase() {
		return (cacheName, config) -> {
			CacheConfig baseCacheConfig = baseConfig.computeIfAbsent(cacheName, cn -> new CacheConfig(emptyMap()));
			return config.copyFrom(baseCacheConfig);
		};
	}

	private void addConfig(String tenant, String cacheName, Map<String, Object> config) {
		configurations.computeIfAbsent(tenant, t -> new HashMap<>()).put(cacheName, new CacheConfig(config));
	}
}
