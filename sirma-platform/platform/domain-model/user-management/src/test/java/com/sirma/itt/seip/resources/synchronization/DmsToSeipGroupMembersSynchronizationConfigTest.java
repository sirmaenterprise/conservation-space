package com.sirma.itt.seip.resources.synchronization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.adapter.CMFGroupService;
import com.sirma.itt.seip.resources.synchronization.DmsToSeipGroupMembersSynchronizationConfig.GroupInfo;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link DmsToSeipGroupMembersSynchronizationConfig}
 *
 * @author BBonev
 */
public class DmsToSeipGroupMembersSynchronizationConfigTest {

	private static final Group GROUP_1 = buildGroup("group1");
	private static final Group GROUP_2 = buildGroup("group2");
	private static final Group GROUP_3 = buildGroup("group3");
	private static final Group GROUP_4 = buildGroup("group4");

	@InjectMocks
	private DmsToSeipGroupMembersSynchronizationConfig synchronization;

	@Mock
	private ResourceService resourceService;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Spy
	private InstanceProxyMock<CMFGroupService> groupService = new InstanceProxyMock<>(null);
	@Mock
	private CMFGroupService cmfGroupService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		groupService.set(cmfGroupService);

		when(resourceService.getAllResources(ResourceType.GROUP, null))
				.thenReturn(Arrays.asList(GROUP_1, GROUP_2, GROUP_3, GROUP_4));

		when(resourceService.findResource(GROUP_2.getId())).thenReturn(GROUP_2);
		when(resourceService.findResource(GROUP_3.getId())).thenReturn(GROUP_3);
		when(resourceService.findResource(GROUP_4.getId())).thenReturn(GROUP_4);
	}

	@Test
	public void runSynchronization() throws Exception {
		// current local state of the mapping
		when(resourceService.getContainedResources(GROUP_1.getId()))
				.thenReturn(Arrays.asList(buildGroup("1"), buildGroup("2"), buildGroup("3")));
		when(resourceService.getContainedResources(GROUP_2.getId()))
				.thenReturn(Arrays.asList(buildGroup("4"), buildGroup("2"), buildGroup("5")));
		when(resourceService.getContainedResources(GROUP_3.getId()))
				.thenReturn(Arrays.asList(buildGroup("5"), buildGroup("6")));
		when(resourceService.getContainedResources(GROUP_4.getId())).thenReturn(Arrays.asList(buildGroup("8")));

		// incoming changes
		when(cmfGroupService.getUsersInAuthority(GROUP_1)).thenReturn(Arrays.asList("1", "2", "3"));
		when(cmfGroupService.getUsersInAuthority(GROUP_2)).thenReturn(Arrays.asList("2", "3"));
		when(cmfGroupService.getUsersInAuthority(GROUP_3)).thenReturn(Arrays.asList("4", "5", "6", "7"));
		when(cmfGroupService.getUsersInAuthority(GROUP_4)).thenReturn(Collections.emptyList());

		SynchronizationRunner.synchronize(synchronization);

		verify(resourceService).modifyMembers(GROUP_4, new HashSet<>(), asSet("8"));
		verify(resourceService).modifyMembers(GROUP_3, asSet("4", "7"), new HashSet<>());
		verify(resourceService).modifyMembers(GROUP_2, asSet("3"), asSet("4", "5"));
	}

	@Test
	public void groupInfoEquals() {
		GroupInfo info1 = new GroupInfo();

		assertTrue(info1.equals(info1));

		info1.groupName = "name1";
		GroupInfo info2 = new GroupInfo();

		assertFalse(info1.equals(info2));

		info2.groupName = "name1";
		assertTrue(info1.equals(info2));
		info1.memberNames.add("member");
		assertFalse(info1.equals(info2));
		info2.memberNames.add("member");
		assertTrue(info1.equals(info2));

		assertFalse(info1.equals(new Object()));
	}

	@Test
	public void groupInfoHashCode() {
		GroupInfo info1 = new GroupInfo();

		assertEquals(info1.hashCode(), info1.hashCode());

		info1.groupName = "name1";
		GroupInfo info2 = new GroupInfo();

		assertNotEquals(info1.hashCode(), info2.hashCode());

		info2.groupName = "name1";
		assertEquals(info1.hashCode(), info2.hashCode());
		info1.memberNames.add("member");
		assertNotEquals(info1.hashCode(), info2.hashCode());
		info2.memberNames.add("member");
		assertEquals(info1.hashCode(), info2.hashCode());
	}

	private static Set<String> asSet(String... strings) {
		return new HashSet<>(Arrays.asList(strings));
	}

	private static Group buildGroup(String id) {
		EmfGroup group = new EmfGroup();
		group.setName(id);
		group.setId("emf:" + id);
		return group;
	}
}
