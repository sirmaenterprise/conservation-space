package com.sirma.itt.seip.cache.config;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheConfigurationProvider;
import com.sirma.itt.seip.cache.CacheTransactionMode;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.LockIsolation;
import com.sirma.itt.seip.cache.Locking;
import com.sirma.itt.seip.cache.Transaction;
import com.sirma.itt.seip.cache.TransactionLocking;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for {@link ExternalCacheConfigurationProviderDecorator}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 26/08/2018
 */
public class ExternalCacheConfigurationProviderDecoratorTest {

	@CacheConfiguration(name = "full_cache",
			transaction = @Transaction(mode = CacheTransactionMode.FULL_XA, locking = TransactionLocking.PESSIMISTIC),
			locking = @Locking(isolation = LockIsolation.READ_COMMITTED, concurrencyLevel = 16, acquireTimeout = 2000),
			eviction = @Eviction(strategy = "LRU", maxEntries = 5000),
			expiration = @Expiration(maxIdle = 200000, interval = 30000, lifespan = 50000))
	static final String FULL_CONFIG_CACHE = "full_cache";

	@CacheConfiguration(name = "default_cache")
	static final String MINIMAL_CONFIG_CACHE = "default_cache";

	@CacheConfiguration(name = "same_cache")
	static final String SOME_CACHE = "same_cache";

	private static final String TEST_TENANT = "test.tenant";

	@InjectMocks
	private ExternalCacheConfigurationProviderDecorator decorator = new ExternalCacheConfigurationProviderDecorator() {};

	@Mock
	private CacheConfigurationProvider delegate;
	@Spy
	private SystemCacheConfigurationLoader provider;
	@Mock
	private SecurityContext securityContext;

	@Before
	public void setUp() throws Exception {
		provider = new SystemCacheConfigurationLoader();
		MockitoAnnotations.initMocks(this);
		Map<String, CacheConfiguration> configurationMap = new HashMap<>();
		configurationMap.put(FULL_CONFIG_CACHE, getCacheConfigInstance("FULL_CONFIG_CACHE"));
		configurationMap.put(MINIMAL_CONFIG_CACHE, getCacheConfigInstance("MINIMAL_CONFIG_CACHE"));
		configurationMap.put(SOME_CACHE, getCacheConfigInstance("SOME_CACHE"));
		when(delegate.getConfiguration(anyString())).then(a -> configurationMap.get(a.getArgumentAt(0, String.class)));
		when(delegate.getConfigurations()).thenReturn(configurationMap);
	}

	private void inTenant(String tenantId) {
		when(securityContext.getCurrentTenantId()).thenReturn(tenantId);
	}

	@Test
	public void getConfiguration_shouldReturn_ConfigurationDataWhenPresent() throws Exception {
		inTenant(SecurityContext.SYSTEM_TENANT);
		withConfig("cache-config.yaml");

		CacheConfiguration configuration = decorator.getConfiguration(FULL_CONFIG_CACHE);
		assertNotNull(configuration);
		assertEquals(20, configuration.eviction().maxEntries());
	}

	@Test
	public void getConfiguration_shouldReturn_ConfigurationDataEventWhenOverriddenInBase() throws Exception {
		inTenant(TEST_TENANT);
		withConfig("cache-config.yaml");

		CacheConfiguration configuration = decorator.getConfiguration(FULL_CONFIG_CACHE);
		assertNotNull(configuration);
		assertEquals(50, configuration.eviction().maxEntries());
	}

	@Test
	public void getConfiguration_shouldReturn_ConfigurationDataFromBaseWhenNotDefined() throws Exception {
		inTenant(TEST_TENANT);
		withConfig("cache-config.yaml");

		CacheConfiguration configuration = decorator.getConfiguration(MINIMAL_CONFIG_CACHE);
		assertNotNull(configuration);
		assertEquals(10, configuration.locking().concurrencyLevel());
		assertEquals(Eviction.EvictionStrategy.UNORDERED, configuration.eviction().strategy());
		assertEquals(100, configuration.expiration().maxIdle());
		assertEquals(111, configuration.expiration().interval());
		assertEquals(100, configuration.locking().acquireTimeout());
		assertEquals(LockIsolation.READ_COMMITTED, configuration.locking().isolation());
		assertEquals(TransactionLocking.PESSIMISTIC, configuration.transaction().locking());
		assertEquals(CacheTransactionMode.FULL_XA, configuration.transaction().mode());
	}

	@Test
	public void getConfiguration_shouldReturn_DefaultConfigIfCacheNotConfigured() throws Exception {
		inTenant(TEST_TENANT);
		withConfig("cache-config.yaml");

		CacheConfiguration configuration = decorator.getConfiguration(SOME_CACHE);
		assertNotNull(configuration);
		assertEquals(32, configuration.locking().concurrencyLevel());
	}

	@Test
	public void getConfiguration_shouldReturn_DefaultConfigIfConfigurationPointsToInvalidFile() throws Exception {
		inTenant(TEST_TENANT);
		withConfig("invalid-config.properties");

		CacheConfiguration configuration = decorator.getConfiguration(SOME_CACHE);
		assertNotNull(configuration);
		assertEquals(32, configuration.locking().concurrencyLevel());
	}

	@Test
	public void getConfiguration_shouldReturn_DefaultConfigIfConfigurationDoesNotExists() throws Exception {
		inTenant(TEST_TENANT);
		System.setProperty(SystemCacheConfigurationLoader.INFINISPAN_CACHE_CONFIG, "some-non-existing-config.yaml");

		CacheConfiguration configuration = decorator.getConfiguration(SOME_CACHE);
		assertNotNull(configuration);
		assertEquals(32, configuration.locking().concurrencyLevel());
	}

	@Test
	public void getConfiguration_shouldWorkWithout_externalConfig() throws Exception {
		inTenant(TEST_TENANT);
		withConfig(null);

		CacheConfiguration configuration = decorator.getConfiguration(MINIMAL_CONFIG_CACHE);
		assertNotNull(configuration);
		assertEquals(32, configuration.locking().concurrencyLevel());
	}

	@Test
	public void getConfigurations() throws Exception {
	}

	private CacheConfiguration getCacheConfigInstance(String fieldName) {
		try {
			Field field = getClass().getDeclaredField(fieldName);
			return field.getAnnotation(CacheConfiguration.class);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		return null;
	}

	private void withConfig(String file) {
		if (file == null) {
			System.setProperty(SystemCacheConfigurationLoader.INFINISPAN_CACHE_CONFIG, "");
		} else {
			System.setProperty(SystemCacheConfigurationLoader.INFINISPAN_CACHE_CONFIG, getConfigLocation(file));
		}
	}

	private String getConfigLocation(String relativeConfigPath) {
		String contents = ResourceLoadUtil.loadResource(getClass(), relativeConfigPath);
		File file = new File(UUID.randomUUID().toString() + ".yaml");
		try {
			try (FileOutputStream outputStream = new FileOutputStream(file)) {
				IOUtils.write(contents, outputStream);
			}
			file.deleteOnExit();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		return file.getAbsolutePath();
	}

}
