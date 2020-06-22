package com.sirma.itt.seip.resources.synchronization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

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

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.RemoteStoreException;
import com.sirma.itt.seip.resources.RemoteUserStoreAdapter;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link RemoteGroupMembersSyncProvider}
 *
 * @author BBonev
 */
public class RemoteGroupMembersSyncProviderTest {

	private static final Group GROUP_1 = buildGroup("group1");
	private static final Group GROUP_2 = buildGroup("group2");
	private static final Group GROUP_3 = buildGroup("group3");
	private static final Group GROUP_4 = buildGroup("group4");

	@InjectMocks
	private RemoteGroupMembersSyncProvider synchronization;

	@Mock
	private ResourceService resourceService;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private RemoteUserStoreAdapter remoteService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(resourceService.getAllGroups())
				.thenReturn(Arrays.asList(GROUP_1, GROUP_2, GROUP_3, GROUP_4));

		when(resourceService.findResource(GROUP_2.getId())).thenReturn(GROUP_2);
		when(resourceService.findResource(GROUP_3.getId())).thenReturn(GROUP_3);
		when(resourceService.findResource(GROUP_4.getId())).thenReturn(GROUP_4);

		when(resourceService.getAllUsers())
				.thenReturn(Arrays.asList(buildUser("regularuser")));
	}

	@Test
	public void runSynchronization() throws Exception {

		when(remoteService.isGroupInGroupSupported()).thenReturn(Boolean.TRUE);
		// current local state of the mapping
		when(resourceService.getContainedResources(GROUP_1.getId()))
				.thenReturn(Arrays.asList(buildGroup("1"), buildGroup("2"), buildGroup("3")));
		when(resourceService.getContainedResources(GROUP_2.getId()))
				.thenReturn(Arrays.asList(buildGroup("4"), buildGroup("2"), buildGroup("5")));
		when(resourceService.getContainedResources(GROUP_3.getId()))
				.thenReturn(Arrays.asList(buildGroup("5"), buildGroup("6")));
		when(resourceService.getContainedResources(GROUP_4.getId())).thenReturn(Arrays.asList(buildGroup("8")));

		// incoming changes
		when(remoteService.getUsersInGroup(GROUP_1.getName())).thenReturn(Arrays.asList("1", "2", "3"));
		when(remoteService.getUsersInGroup(GROUP_2.getName())).thenReturn(Arrays.asList("2", "3"));
		when(remoteService.getUsersInGroup(GROUP_3.getName())).thenReturn(Arrays.asList("4", "5", "6", "7"));
		when(remoteService.getUsersInGroup(GROUP_4.getName())).thenReturn(Collections.emptyList());

		SynchronizationRunner.synchronize(synchronization);

		verify(resourceService).modifyMembers(GROUP_4, new HashSet<>(), asSet("8"));
		verify(resourceService).modifyMembers(GROUP_3, asSet("4", "7"), new HashSet<>());
		verify(resourceService).modifyMembers(GROUP_2, asSet("3"), asSet("4", "5"));
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void runSynchronization_shouldFailIfCannotAccessRemoteStore() throws Exception {

		when(remoteService.isGroupInGroupSupported()).thenReturn(Boolean.TRUE);
		// current local state of the mapping
		when(resourceService.getContainedResources(GROUP_1.getId()))
				.thenReturn(Arrays.asList(buildGroup("1"), buildGroup("2"), buildGroup("3")));
		when(resourceService.getContainedResources(GROUP_2.getId()))
				.thenReturn(Arrays.asList(buildGroup("4"), buildGroup("2"), buildGroup("5")));
		when(resourceService.getContainedResources(GROUP_3.getId()))
				.thenReturn(Arrays.asList(buildGroup("5"), buildGroup("6")));
		when(resourceService.getContainedResources(GROUP_4.getId())).thenReturn(Arrays.asList(buildGroup("8")));

		// incoming changes
		when(remoteService.getUsersInGroup(GROUP_1.getName())).thenReturn(Arrays.asList("1", "2", "3"));
		when(remoteService.getUsersInGroup(GROUP_2.getName())).thenThrow(RemoteStoreException.class);

		SynchronizationRunner.synchronize(synchronization);
	}

	@Test
	public void groupInfoEquals() {
		GroupInfo info1 = new GroupInfo();

		assertTrue(info1.equals(info1));

		info1.setGroupName("name1");
		GroupInfo info2 = new GroupInfo();

		assertFalse(info1.equals(info2));

		info2.setGroupName("name1");
		assertTrue(info1.equals(info2));
		info1.addMember("member");
		assertFalse(info1.equals(info2));
		info2.addMember("member");
		assertTrue(info1.equals(info2));

		assertFalse(info1.equals(new Object()));
	}

	@Test
	public void groupInfo_Should_PerformCaseInsensitiveOperations() {
		GroupInfo externalGroupInfo = new GroupInfo();
		externalGroupInfo.setGroupName("test");
		externalGroupInfo.addMember("regularuser");

		GroupInfo localGroupInfo = new GroupInfo();
		localGroupInfo.setGroupName("test");
		localGroupInfo.addMember("RegularUser");

		assertTrue(externalGroupInfo.equals(localGroupInfo));
	}

	@Test
	public void groupInfoHashCode() {
		GroupInfo info1 = new GroupInfo();

		assertEquals(info1.hashCode(), info1.hashCode());

		info1.setGroupName("name1");
		GroupInfo info2 = new GroupInfo();

		assertNotEquals(info1.hashCode(), info2.hashCode());

		info2.setGroupName("name1");
		assertEquals(info1.hashCode(), info2.hashCode());
		info1.addMember("member");
		assertNotEquals(info1.hashCode(), info2.hashCode());
		info2.addMember("member");
		assertEquals(info1.hashCode(), info2.hashCode());
	}

	@Test
	public void shouldHaveName() {
		assertEquals(RemoteGroupMembersSyncProvider.NAME, synchronization.getName());
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

	private static User buildUser(String id) {
		EmfUser user = new EmfUser();
		user.setName(id);
		user.setId("emf:" + id);
		return user;
	}

}
