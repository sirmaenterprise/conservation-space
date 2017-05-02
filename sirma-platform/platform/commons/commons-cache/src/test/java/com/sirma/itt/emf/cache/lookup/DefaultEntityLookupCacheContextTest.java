package com.sirma.itt.emf.cache.lookup;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.Serializable;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.cache.CacheProvider;
import com.sirma.itt.seip.cache.SimpleCache;
import com.sirma.itt.seip.cache.lookup.DefaultEntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAO;
import com.sirma.itt.seip.concurrent.locks.ContextualReadWriteLock;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * The Class DefaultEntityLookupCacheContextTest.
 *
 * @author BBonev
 */
@Test
public class DefaultEntityLookupCacheContextTest {

	/** The Constant SIMPLE_CACHE. */
	private static final String SIMPLE_CACHE = "test";

	/** The Constant LOOKUP_CACHE. */
	private static final String LOOKUP_CACHE = "cache";

	/** The cache provider. */
	@Mock
	CacheProvider cacheProvider;

	/** The cache. */
	@Mock
	SimpleCache<Serializable, Object> cache;

	@Mock
	private ContextualReadWriteLock lock;
	@Mock
	private ContextualReadWriteLock simpleCacheLock;

	/** The lookup. */
	@Mock
	EntityLookupCallbackDAO<Serializable, Object, Serializable> lookup;

	@Mock
	SecurityContext securityContext;

	/** The context. */
	@InjectMocks
	DefaultEntityLookupCacheContext context;

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		context.onInit();
		when(securityContext.getCurrentTenantId()).thenReturn(SecurityContext.SYSTEM_TENANT);
		when(cache.get(anyString())).thenReturn("value");
		Mockito.when(cacheProvider.createCache(anyString())).thenReturn(cache);
	}

	/**
	 * Test get cache.
	 */
	public void testGetCache() {
		Assert.assertNull(context.getCache(SIMPLE_CACHE, false));
		Assert.assertNotNull(context.getCache(SIMPLE_CACHE, true));
		Assert.assertNotNull(context.getCache(SIMPLE_CACHE, true));
		SimpleCache<Serializable, Object> simpleCache = context.getCache(SIMPLE_CACHE, true);
		simpleCache.put("test", "value");
		assertEquals(simpleCache.get("test"), "value");
		Mockito.verify(cacheProvider).createCache(anyString());
	}

	/**
	 * Test entity lookup cache.
	 */
	public void testEntityLookupCache() {
		Assert.assertFalse(context.containsCache(LOOKUP_CACHE));
		Assert.assertNull(context.getCache(LOOKUP_CACHE));
		Assert.assertNotNull(context.createCache(LOOKUP_CACHE, lookup));
		Assert.assertNotNull(context.getCache(LOOKUP_CACHE));
		Assert.assertTrue(context.containsCache(LOOKUP_CACHE));
	}

	/**
	 * Test get active.
	 */
	@Test(dependsOnMethods = { "testGetCache", "testEntityLookupCache" })
	public void testGetActive() {
		Assert.assertFalse(context.getActiveCaches().isEmpty());
	}

	/**
	 * Test on shutdown.
	 */
	@Test(dependsOnMethods = { "testGetCache",
			"testEntityLookupCache" }, expectedExceptions = IllegalStateException.class)
	public void testOnShutdown() {
		context.onShutdown();
		Assert.assertTrue(context.getActiveCaches().isEmpty());
	}

}
