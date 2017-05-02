package com.sirma.itt.seip.tenant.infinispan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;

import org.jboss.dmr.ModelNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.wildfly.WildflyControllerService;

/**
 * Test for {@link WildflyInfinispanCacheRegister}
 *
 * @author BBonev
 */
public class WildflyInfinispanCacheRegisterTest {

	private static final String TENANT_ID = "tenant.com";

	@CacheConfiguration(name = "test")
	static final String CACHE_CONFIG = "test";

	@InjectMocks
	private WildflyInfinispanCacheRegister cacheRegister;
	@Mock
	private WildflyControllerService controller;
	@Mock
	private SecurityContext securityContext;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void registerCache_noContainer() throws Exception {
		mockTenant(TENANT_ID);
		mockSuccessIf(isAddOperation());
		mockFailIf(isResourceReadFor(WildflyInfinispanCacheRegister.CACHE_CONTAINER));
		mockFailIf(isResourceReadFor(WildflyInfinispanCacheRegister.DEFAULT_CACHE));
		mockFailIf(isResourceReadFor("tenant.com_test"));

		assertTrue(cacheRegister.registerCache(getConfiguration()));
	}

	@Test
	public void registerCache_noDefaultCache() throws Exception {
		mockTenant(TENANT_ID);
		mockSuccessIf(isAddOperation());
		mockSuccessIf(isResourceReadFor(WildflyInfinispanCacheRegister.CACHE_CONTAINER));
		mockFailIf(isResourceReadFor(WildflyInfinispanCacheRegister.DEFAULT_CACHE));
		mockFailIf(isResourceReadFor("tenant.com_test"));

		assertTrue(cacheRegister.registerCache(getConfiguration()));
	}

	@Test
	public void registerCache() throws Exception {
		mockTenant(TENANT_ID);
		mockSuccessIf(isAddOperation());
		mockSuccessIf(isResourceReadFor(WildflyInfinispanCacheRegister.CACHE_CONTAINER));
		mockSuccessIf(isResourceReadFor(WildflyInfinispanCacheRegister.DEFAULT_CACHE));
		mockFailIf(isResourceReadFor("tenant.com_test"));

		assertTrue(cacheRegister.registerCache(getConfiguration()));
	}

	@Test
	public void registerCache_defaultTenant() throws Exception {
		mockTenant(SecurityContext.DEFAULT_TENANT);
		mockSuccessIf(isAddOperation());
		mockSuccessIf(isResourceReadFor(WildflyInfinispanCacheRegister.CACHE_CONTAINER));
		mockSuccessIf(isResourceReadFor(WildflyInfinispanCacheRegister.DEFAULT_CACHE));
		mockFailIf(isResourceReadFor(CACHE_CONFIG));

		assertTrue(cacheRegister.registerCache(getConfiguration()));
	}

	@Test
	public void registerCache_alreadyRegistered() throws Exception {
		mockTenant(TENANT_ID);
		mockSuccessIf(isAddOperation());
		mockSuccessIf(isResourceReadFor(WildflyInfinispanCacheRegister.CACHE_CONTAINER));
		mockSuccessIf(isResourceReadFor(WildflyInfinispanCacheRegister.DEFAULT_CACHE));
		mockSuccessIf(isResourceReadFor("tenant.com_test"));

		assertTrue(cacheRegister.registerCache(getConfiguration()));
	}

	@Test(expected = TenantCreationException.class)
	public void registerCache_failToAdd() throws Exception {
		mockTenant(TENANT_ID);
		mockFailIf(isAddOperation());
		mockSuccessIf(isResourceReadFor(WildflyInfinispanCacheRegister.CACHE_CONTAINER));
		mockSuccessIf(isResourceReadFor(WildflyInfinispanCacheRegister.DEFAULT_CACHE));
		mockFailIf(isResourceReadFor("tenant.com_test"));

		assertFalse(cacheRegister.registerCache(getConfiguration()));
	}

	@Test
	public void registerCache_CommunicationException() throws Exception {
		mockTenant(TENANT_ID);
		mockExceptionIf(isAddOperation());
		mockSuccessIf(isResourceReadFor(WildflyInfinispanCacheRegister.CACHE_CONTAINER));
		mockSuccessIf(isResourceReadFor(WildflyInfinispanCacheRegister.DEFAULT_CACHE));
		mockFailIf(isResourceReadFor("tenant.com_test"));

		assertFalse(cacheRegister.registerCache(getConfiguration()));
	}

	@Test
	public void unregisterCache() throws Exception {
		mockTenant(TENANT_ID);
		mockSuccessIf(isRemoveOperation());

		assertTrue(cacheRegister.unregisterCache(CACHE_CONFIG));
	}

	@Test(expected = TenantCreationException.class)
	public void unregisterCache_failed() throws Exception {
		mockTenant(TENANT_ID);
		mockFailIf(isRemoveOperation());

		cacheRegister.unregisterCache(CACHE_CONFIG);
	}

	@Test
	public void unregisterCache_CommunicationError() throws Exception {
		mockTenant(TENANT_ID);
		mockExceptionIf(isRemoveOperation());

		assertFalse(cacheRegister.unregisterCache(CACHE_CONFIG));
	}

	private void mockSuccessIf(Predicate<ModelNode> predicate) throws RollbackedException {
		ModelNode node = mock(ModelNode.class);
		ModelNode outcome = mock(ModelNode.class);
		when(node.get("outcome")).thenReturn(outcome);
		when(outcome.asString()).thenReturn("success");
		when(controller.execute(argThat(CustomMatcher.of(predicate)))).thenReturn(node);
	}

	private void mockFailIf(Predicate<ModelNode> predicate) throws RollbackedException {
		ModelNode node = mock(ModelNode.class);
		ModelNode outcome = mock(ModelNode.class);
		when(node.get("outcome")).thenReturn(outcome);
		when(outcome.asString()).thenReturn("failed");
		ModelNode description = mock(ModelNode.class);
		when(node.get("failure-description")).thenReturn(description);
		when(description.asString()).thenReturn("failed");
		when(controller.execute(argThat(CustomMatcher.of(predicate)))).thenReturn(node);
	}

	@SuppressWarnings("unchecked")
	private void mockExceptionIf(Predicate<ModelNode> predicate) throws RollbackedException {
		when(controller.execute(argThat(CustomMatcher.of(predicate)))).thenThrow(RollbackedException.class);
	}

	private void mockTenant(String tenantId) {
		when(securityContext.getCurrentTenantId()).thenReturn(tenantId);
	}

	private static Predicate<ModelNode> isAddOperation() {
		return node -> {
			if (node == null) {
				return false;
			}
			String nodeText = node.toString();
			return nodeText.contains("\"add\"");
		};
	}

	private static Predicate<ModelNode> isRemoveOperation() {
		return node -> {
			if (node == null) {
				return false;
			}
			String nodeText = node.toString();
			return nodeText.contains("\"remove\"");
		};
	}

	private static Predicate<ModelNode> isResourceReadFor(String resourceName) {
		return node -> {
			if (node == null) {
				return false;
			}
			String nodeText = node.toString();
			return nodeText.contains("read-resource") && nodeText.contains(resourceName);
		};
	}

	private static CacheConfiguration getConfiguration() throws NoSuchFieldException {
		return WildflyInfinispanCacheRegisterTest.class
				.getDeclaredField("CACHE_CONFIG")
					.getAnnotation(CacheConfiguration.class);
	}
}
