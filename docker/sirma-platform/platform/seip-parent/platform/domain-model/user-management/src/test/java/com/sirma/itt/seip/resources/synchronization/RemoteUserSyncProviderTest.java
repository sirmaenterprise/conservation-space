package com.sirma.itt.seip.resources.synchronization;

import static org.junit.Assert.assertEquals;
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

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.RemoteStoreException;
import com.sirma.itt.seip.resources.RemoteUserStoreAdapter;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationException;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link RemoteUserSyncProvider}
 *
 * @author BBonev
 */
public class RemoteUserSyncProviderTest {

	@InjectMocks
	private RemoteUserSyncProvider userSyncConfig;

	@Mock
	protected ResourceService resourceService;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private RemoteUserStoreAdapter remoteService;
	@Spy
	private SecurityContextManagerFake securityContextManager = new SecurityContextManagerFake();
	@Mock
	private DefinitionService definitionService;

	private EmfUser systemUser;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		systemUser = buildUser("system");
		securityContextManager.setSystemUser(systemUser);
		when(resourceService.loadByDbId(any(Serializable.class)))
				.then(a -> buildUser(a.getArgumentAt(0, Serializable.class).toString()));

		DefinitionMock definitionMock = new DefinitionMock();
		PropertyDefinitionMock propertyDefinitionMock = new PropertyDefinitionMock();
		propertyDefinitionMock.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.TEXT));
		propertyDefinitionMock.setDmsType("urn:email");
		propertyDefinitionMock.setName(ResourceProperties.EMAIL);
		definitionMock.setFields(Arrays.asList(propertyDefinitionMock));
		when(definitionService.getInstanceDefinition(any(Instance.class))).thenReturn(definitionMock);
	}

	@Test
	public void testSynchronization() throws Exception {
		EmfUser user3 = buildUser("user3");
		user3.add(ResourceProperties.EMAIL, "user@mail.com");
		when(remoteService.getAllUsers())
				.thenReturn(Arrays.asList(buildUser("user1"), buildUser("user2"), user3));

		when(resourceService.getAllUsers())
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
		EmfUser user3 = buildUser("user3");
		user3.add(ResourceProperties.EMAIL, "user@mail.com");
		when(remoteService.getAllUsers())
				.thenReturn(Arrays.asList(buildUser("user1"), buildUser("user2"), user3));

		when(resourceService.getAllUsers())
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

		when(remoteService.getAllUsers()).thenReturn(Collections.emptyList());

		when(resourceService.getAllUsers()).thenReturn(Arrays.asList(systemUser));

		SynchronizationResult<String, Resource> result = SynchronizationRunner.synchronize(userSyncConfig);
		assertNotNull(result);
		assertTrue(result.getModified().isEmpty());
		assertTrue(result.getToAdd().isEmpty());
		assertTrue(result.getToRemove().isEmpty());
	}

	@Test
	public void shouldHaveName() {
		assertEquals(RemoteUserSyncProvider.NAME, userSyncConfig.getName());
	}

	@Test(expected = SynchronizationException.class)
	public void shouldThrowException_When_RemoteStoreCommunicationFails() throws Exception {
		when(remoteService.getAllUsers()).thenThrow(new RemoteStoreException(""));
		when(resourceService.getAllUsers()).thenReturn(Arrays.asList(systemUser));

		SynchronizationRunner.synchronize(userSyncConfig);
	}
}
