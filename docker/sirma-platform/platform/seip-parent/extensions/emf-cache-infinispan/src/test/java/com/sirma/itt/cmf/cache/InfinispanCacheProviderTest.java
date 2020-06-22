package com.sirma.itt.cmf.cache;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheConfigurationProvider;
import com.sirma.itt.seip.cache.CacheRegister;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Test for {@link InfinispanCacheProvider}
 *
 * @author BBonev
 */
public class InfinispanCacheProviderTest {

	private static final String TENANT_ID = "tenant.com";
	private static final String EXISTING_CACHE = "EXISTING_CACHE";
	private static final String NON_EXISTING_CACHE = "NON_EXISTING_CACHE";

	@InjectMocks
	private InfinispanCacheProvider cacheProvider;

	@Mock
	private EmbeddedCacheManager cacheContainer;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private CacheConfigurationProvider configurationProvider;
	@Mock
	private CacheRegister register;
	@Spy
	private InstanceProxyMock<CacheRegister> cacheRegister = new InstanceProxyMock<>(null);

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		cacheRegister.set(register);

		when(securityContext.isDefaultTenant()).thenReturn(Boolean.FALSE);
		when(securityContext.getCurrentTenantId()).thenReturn(TENANT_ID);

		when(cacheContainer.getCacheConfiguration(TENANT_ID + "_" + EXISTING_CACHE))
				.thenReturn(mock(Configuration.class));
		when(cacheContainer.getCacheConfiguration(TENANT_ID + "_" + NON_EXISTING_CACHE)).thenReturn(null,
				mock(Configuration.class));

		when(cacheContainer.getCache(anyString())).then(a -> {
			Cache<?, ?> cache = mock(Cache.class);
			when(cache.getAdvancedCache()).thenReturn(mock(AdvancedCache.class));
			return cache;
		});
	}

	@Test
	public void getDefaultCache() throws Exception {
		assertNotNull(cacheProvider.createCache());
		verify(cacheContainer).getCache(InfinispanCacheProvider.DEFAULT_CACHE);
	}

	@Test
	public void getCache() throws Exception {
		assertNotNull(cacheProvider.createCache(EXISTING_CACHE));
		verify(cacheContainer).getCache(TENANT_ID + "_" + EXISTING_CACHE);
	}

	@Test
	public void getCache_nonRegistred() throws Exception {
		when(configurationProvider.getConfiguration(NON_EXISTING_CACHE)).thenReturn(mock(CacheConfiguration.class));
		when(register.registerCache(any(CacheConfiguration.class))).thenReturn(Boolean.TRUE);

		assertNotNull(cacheProvider.createCache(NON_EXISTING_CACHE));
		verify(register).registerCache(any(CacheConfiguration.class));
		verify(cacheContainer).getCache(TENANT_ID + "_" + NON_EXISTING_CACHE);
	}

	@Test
	public void getCache_nonRegistred_failToRegister() throws Exception {
		assertNotNull(cacheProvider.createCache(NON_EXISTING_CACHE));
		verify(register, never()).registerCache(any(CacheConfiguration.class));
		verify(cacheContainer).getCache(TENANT_ID + "_" + NON_EXISTING_CACHE);
	}
}
