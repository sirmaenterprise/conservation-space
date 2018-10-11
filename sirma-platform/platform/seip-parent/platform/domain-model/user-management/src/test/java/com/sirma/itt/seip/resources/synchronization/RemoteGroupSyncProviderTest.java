package com.sirma.itt.seip.resources.synchronization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

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
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.RemoteStoreException;
import com.sirma.itt.seip.resources.RemoteUserStoreAdapter;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
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
 * Test for {@link RemoteGroupSyncProvider}
 *
 * @author BBonev
 */
public class RemoteGroupSyncProviderTest {

	@InjectMocks
	private RemoteGroupSyncProvider groupSyncConfig;

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

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(resourceService.loadByDbId(any(Serializable.class)))
				.then(a -> buildGroup(a.getArgumentAt(0, Serializable.class).toString()));

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
		// for groups currently we do not have any properties which to synchronize from the remote store, but still...
		EmfGroup group3 = buildGroup("group3");
		group3.add(ResourceProperties.EMAIL, "group@mail.com");
		when(remoteService.getAllGroups())
				.thenReturn(Arrays.asList(buildGroup("group1"), buildGroup("group2"), group3));

		when(resourceService.getAllGroups())
				.thenReturn(Arrays.asList(buildGroup("group1"), buildGroup("group4"), buildGroup("group3")));

		SynchronizationResult<String, Resource> result = SynchronizationRunner.synchronize(groupSyncConfig);
		assertNotNull(result);
		assertTrue(result.getModified().containsKey("group3"));
		assertTrue(result.getToAdd().containsKey("group2"));
		assertTrue(result.getToRemove().containsKey("group4"));

		verify(resourceService).deactivate(any(), any());
	}

	private static EmfGroup buildGroup(String name) {
		EmfGroup group = new EmfGroup(name, "");
		group.setId("emf:" + name);
		return group;
	}

	@Test
	public void testSynchronization_noData() throws Exception {

		when(remoteService.getAllGroups()).thenReturn(Collections.emptyList());

		when(resourceService.getAllGroups()).thenReturn(Collections.emptyList());

		SynchronizationResult<String, Resource> result = SynchronizationRunner.synchronize(groupSyncConfig);
		assertNotNull(result);
		assertTrue(result.getModified().isEmpty());
		assertTrue(result.getToAdd().isEmpty());
		assertTrue(result.getToRemove().isEmpty());
	}

	@Test
	public void shouldHaveName() {
		assertEquals(RemoteGroupSyncProvider.NAME, groupSyncConfig.getName());
	}

	@Test(expected = SynchronizationException.class)
	public void shouldThrowException_When_RemoteStoreCommunicationFails() throws Exception {
		when(remoteService.getAllGroups()).thenThrow(new RemoteStoreException(""));
		when(resourceService.getAllGroups()).thenReturn(Arrays.asList(buildGroup("group1")));

		SynchronizationRunner.synchronize(groupSyncConfig);
	}
}
