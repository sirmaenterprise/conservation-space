package com.sirma.itt.seip.resources.synchronization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.adapter.CMFUserService;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link DmsToSeipUserSynchronizationConfig}
 *
 * @author BBonev
 */
public class DmsToSeipUserSynchronizationConfigTest {

	@InjectMocks
	private DmsToSeipUserSynchronizationConfig userSyncConfig;

	@Mock
	private HashCalculator hashCalculator;
	@Mock
	protected ResourceService resourceService;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private CMFUserService cmfUserService;
	@Spy
	private InstanceProxyMock<CMFUserService> userService = new InstanceProxyMock<>(null);
	@Spy
	private SecurityContextManagerFake securityContextManager = new SecurityContextManagerFake();
	@Mock
	private DictionaryService dictionaryService;

	private EmfUser systemUser;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		userService.set(cmfUserService);

		systemUser = buildUser("system");
		securityContextManager.setSystemUser(systemUser);
		when(resourceService.loadByDbId(any(Serializable.class)))
				.then(a -> buildUser(a.getArgumentAt(0, Serializable.class).toString()));
	}

	@Test
	public void testSynchronization() throws Exception {
		when(hashCalculator.equalsByHash(buildUser("user3"), buildUser("user3"))).thenReturn(Boolean.FALSE);
		when(hashCalculator.equalsByHash(buildUser("user1"), buildUser("user1"))).thenReturn(Boolean.TRUE);

		when(cmfUserService.getAllUsers())
				.thenReturn(Arrays.asList(buildUser("user1"), buildUser("user2"), buildUser("user3")));

		when(resourceService.getAllResources(ResourceType.USER, null))
				.thenReturn(Arrays.asList(buildUser("user1"), buildUser("user4"), buildUser("user3")));

		SynchronizationResult<String, Resource> result = SynchronizationRunner.synchronize(userSyncConfig);
		assertNotNull(result);
		assertTrue(result.getModified().containsKey("user3"));
		assertTrue(result.getToAdd().containsKey("user2"));
		assertTrue(result.getToRemove().containsKey("user4"));

		verify(resourceService).deactivate(any(), any());
	}

	@Test
	public void testSynchronization_allowDelete() throws Exception {
		when(hashCalculator.equalsByHash(buildUser("user3"), buildUser("user3"))).thenReturn(Boolean.FALSE);
		when(hashCalculator.equalsByHash(buildUser("user1"), buildUser("user1"))).thenReturn(Boolean.TRUE);

		when(cmfUserService.getAllUsers())
				.thenReturn(Arrays.asList(buildUser("user1"), buildUser("user2"), buildUser("user3")));

		when(resourceService.getAllResources(ResourceType.USER, null))
				.thenReturn(Arrays.asList(buildUser("user1"), buildUser("user4"), buildUser("user3")));

		SynchronizationResult<String, Resource> result = SynchronizationRunner.synchronize(userSyncConfig,
				new SyncRuntimeConfiguration().allowDelete());
		assertNotNull(result);
		assertTrue(result.getModified().containsKey("user3"));
		assertTrue(result.getToAdd().containsKey("user2"));
		assertTrue(result.getToRemove().containsKey("user4"));

		verify(resourceService).delete(any(), any(), eq(true));
	}

	private static EmfUser buildUser(String name) {
		EmfUser user = new EmfUser(name);
		user.setId("emf:" + name);
		return user;
	}

	@Test
	public void testSynchronization_noData() throws Exception {

		when(cmfUserService.getAllUsers()).thenReturn(Collections.emptyList());

		when(resourceService.getAllResources(ResourceType.USER, null)).thenReturn(Arrays.asList(systemUser));

		SynchronizationResult<String, Resource> result = SynchronizationRunner.synchronize(userSyncConfig);
		assertNotNull(result);
		assertTrue(result.getModified().isEmpty());
		assertTrue(result.getToAdd().isEmpty());
		assertTrue(result.getToRemove().isEmpty());
	}
}
