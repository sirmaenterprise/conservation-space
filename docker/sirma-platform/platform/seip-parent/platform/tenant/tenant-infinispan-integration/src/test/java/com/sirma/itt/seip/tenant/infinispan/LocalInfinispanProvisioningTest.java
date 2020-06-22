package com.sirma.itt.seip.tenant.infinispan;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheConfigurationProvider;
import com.sirma.itt.seip.cache.CacheRegister;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;

/**
 * Test for {@link LocalInfinispanProvisioning}
 *
 * @author BBonev
 */
public class LocalInfinispanProvisioningTest {

	private static final String TENANT_ID = "tenant.com";
	private static final TenantInfo TENANT = new TenantInfo(TENANT_ID);

	@CacheConfiguration(name = "test")
	static final String CACHE_CONFIG = "test";

	@InjectMocks
	private LocalInfinispanProvisioning provisioning;

	@Mock
	private CacheConfigurationProvider configurationExtension;
	
	@Mock
	private CacheRegister cacheRegister;
	
	@Spy
	private SecurityContextManager contextManager = new SecurityContextManagerFake();

	@Before
	public void beforeMethod() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(configurationExtension.getConfigurations())
				.thenReturn(Collections.singletonMap(CACHE_CONFIG, getConfiguration()));
	}

	@Test
	@SuppressWarnings("boxing")
	public void provisionCaches() throws Exception {
		when(cacheRegister.registerCaches(anyCollection())).thenReturn(Boolean.TRUE);

		provisioning.provision(TENANT);
		verify(cacheRegister).registerCaches(anyCollection());
	}

	@Test
	@SuppressWarnings("boxing")
	public void provisionCaches_fail() throws Exception {
		when(cacheRegister.registerCaches(anyCollection())).thenReturn(Boolean.FALSE);

		provisioning.provision(TENANT);
		verify(cacheRegister).registerCaches(anyCollection());
	}

	@Test
	@SuppressWarnings("boxing")
	public void rollbackCaches() throws Exception {
		when(cacheRegister.registerCaches(anyCollection())).thenReturn(Boolean.TRUE);

		provisioning.deleteCaches(TENANT);
		verify(cacheRegister).unregisterCaches(Arrays.asList(CACHE_CONFIG));
	}

	@Test
	public void rollbackCaches_noCachesAddedBefore() throws Exception {
		when(configurationExtension.getConfigurations()).thenReturn(Collections.EMPTY_MAP);

		provisioning.deleteCaches(TENANT);
		verify(cacheRegister, never()).unregisterCaches(Arrays.asList(CACHE_CONFIG));
	}

	@Test
	@SuppressWarnings("boxing")
	public void rollbackCaches_defaultTenant() throws Exception {
		when(cacheRegister.registerCaches(anyCollection())).thenReturn(Boolean.TRUE);
		TenantInfo defaultTenant = new TenantInfo(SecurityContext.DEFAULT_TENANT);

		provisioning.deleteCaches(defaultTenant);
		verify(cacheRegister, Mockito.times(1)).unregisterCaches(Arrays.asList(CACHE_CONFIG));
	}

	private static CacheConfiguration getConfiguration() throws NoSuchFieldException {
		return LocalInfinispanProvisioningTest.class
					.getDeclaredField("CACHE_CONFIG")
					.getAnnotation(CacheConfiguration.class);
	}
}