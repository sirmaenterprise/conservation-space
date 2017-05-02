package com.sirma.itt.seip.resources.synchronization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
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
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.adapter.CMFGroupService;
import com.sirma.itt.seip.synchronization.SynchronizationResult;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link DmsToSeipGroupSynchronizationConfig}
 *
 * @author BBonev
 */
public class DmsToSeipGroupSynchronizationConfigTest {

	@InjectMocks
	private DmsToSeipGroupSynchronizationConfig groupSyncConfig;

	@Mock
	private HashCalculator hashCalculator;
	@Mock
	protected ResourceService resourceService;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private CMFGroupService cmfGroupService;
	@Spy
	private InstanceProxyMock<CMFGroupService> groupService = new InstanceProxyMock<>(null);
	@Spy
	private SecurityContextManagerFake securityContextManager = new SecurityContextManagerFake();
	@Mock
	private DictionaryService dictionaryService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		groupService.set(cmfGroupService);
		when(resourceService.loadByDbId(any(Serializable.class)))
				.then(a -> buildGroup(a.getArgumentAt(0, Serializable.class).toString()));
	}

	@Test
	public void testSynchronization() throws Exception {
		when(hashCalculator.equalsByHash(buildGroup("group3"), buildGroup("group3")))
				.thenReturn(Boolean.FALSE);
		when(hashCalculator.equalsByHash(buildGroup("group1"), buildGroup("group1")))
				.thenReturn(Boolean.TRUE);

		when(cmfGroupService.getAllGroups())
				.thenReturn(Arrays.asList(buildGroup("group1"), buildGroup("group2"), buildGroup("group3")));

		when(resourceService.getAllResources(ResourceType.GROUP, null))
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

		when(cmfGroupService.getAllGroups()).thenReturn(Collections.emptyList());

		when(resourceService.getAllResources(ResourceType.GROUP, null)).thenReturn(Collections.emptyList());

		SynchronizationResult<String, Resource> result = SynchronizationRunner.synchronize(groupSyncConfig);
		assertNotNull(result);
		assertTrue(result.getModified().isEmpty());
		assertTrue(result.getToAdd().isEmpty());
		assertTrue(result.getToRemove().isEmpty());
	}
}
