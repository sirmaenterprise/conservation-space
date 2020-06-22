package com.sirma.itt.seip.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.testutil.mocks.DatabaseIdManagerMock;

/**
 * Test for {@link ResourceEntityDao}
 *
 * @author BBonev
 */
@SuppressWarnings("unchecked")
public class ResourceEntityDaoTest {

	@InjectMocks
	private ResourceEntityDao entityDao;

	@Mock
	private DbDao dbDao;
	@Spy
	private DatabaseIdManager idManager = new DatabaseIdManagerMock();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testFindResourceEntities() {
		entityDao.findResourceEntities(new HashSet<>());
		verify(dbDao).fetchWithNamed(eq(ResourceEntity.QUERY_RESOURCES_BY_IDS_KEY), anyList());
	}

	@Test
	public void testGetAllResources() {
		entityDao.getAllResources();
		verify(dbDao).fetchWithNamed(eq(ResourceEntity.QUERY_ALL_RESOURCES_KEY), anyList());
	}

	@Test
	public void testGetAllResourcesByType() {
		entityDao.getAllResourcesByType(ResourceType.USER);
		verify(dbDao).fetchWithNamed(eq(ResourceEntity.QUERY_ALL_RESOURCES_BY_TYPE_KEY), anyList());
	}

	@Test
	public void testGetAllResourceIdsByType() {
		entityDao.getAllResourceIdsByType(ResourceType.USER);
		verify(dbDao).fetchWithNamed(eq(ResourceEntity.QUERY_ALL_RESOURCE_IDS_BY_TYPE_KEY), anyList());
	}

	@Test
	public void testFindResourcesByIdentifierAndType() {
		entityDao.findResourcesByNameAndType(new LinkedList<>(), ResourceType.USER);
		verify(dbDao).fetchWithNamed(eq(ResourceEntity.QUERY_RESOURCES_BY_NAMES_AND_TYPE_KEY), anyList());
	}

	@Test
	public void testFindResourcesByIdentifierAndType_ShouldLowerCaseOfIds() {
		String user1 = "Regularuser@tenant.com";
		String user2 = "someuser@tenant.com";

		List<String> ids = new LinkedList<>();
		ids.add(user1);
		ids.add(user2);
		entityDao.findResourcesByNameAndType(ids, ResourceType.USER);

		ArgumentCaptor<List<Pair<String, Object>>> argCaptor = ArgumentCaptor.forClass(List.class);

		verify(dbDao).fetchWithNamed(eq(ResourceEntity.QUERY_RESOURCES_BY_NAMES_AND_TYPE_KEY), argCaptor.capture());

		List<String> resultIds = (List<String>) argCaptor.getValue().get(0).getSecond();
		assertEquals(user1.toLowerCase(), resultIds.get(0));
		assertEquals(user2, resultIds.get(1));
	}

	@Test
	public void testPersist() {
		entityDao.persist(new ResourceEntity());
		verify(dbDao).saveOrUpdate(any(ResourceEntity.class));
	}

	@Test
	public void testFind() {
		entityDao.find("emf:id");
		verify(dbDao).find(ResourceEntity.class, "emf:id");
	}

	@Test
	@SuppressWarnings("boxing")
	public void testResourceExists() {
		assertFalse(entityDao.resourceExists("emf:id"));
		verify(dbDao).fetchWithNamed(eq(ResourceEntity.CHECK_IF_RESOURCE_EXISTS_KEY), anyList());
		when(dbDao.fetchWithNamed(eq(ResourceEntity.CHECK_IF_RESOURCE_EXISTS_KEY), anyList()))
				.thenReturn(Arrays.asList(1L));
		assertTrue(entityDao.resourceExists("emf:id"));
	}

	@Test
	public void testGetMemberIdsOf() {
		entityDao.getMemberIdsOf("emf:groupId");
		verify(dbDao).fetchWithNamed(eq(GroupMembershipEntity.GET_ALL_MEMBERS_IDS_KEY), anyList());
	}

	@Test
	public void testGetMembersOf() {
		entityDao.getMembersOf("emf:groupId");
		verify(dbDao).fetchWithNamed(eq(GroupMembershipEntity.GET_ALL_MEMBERS_KEY), anyList());
	}

	@Test
	public void testGetContainingGroups() {
		entityDao.getContainingGroups("emf:user");
		verify(dbDao).fetchWithNamed(eq(GroupMembershipEntity.GET_CONTAINING_GROUP_IDS_KEY), anyList());
	}

	@Test
	public void testAddMembers() {

		assertFalse(entityDao.addMembers("emf:group", null, Assert::assertNotNull));
		assertFalse(entityDao.addMembers(null, Collections.singleton("emf:newMember"), Assert::assertNotNull));

		when(dbDao.fetchWithNamed(eq(GroupMembershipEntity.GET_ALL_MEMBERS_IDS_KEY), anyList()))
				.thenReturn(Arrays.asList("emf:oldMember"));
		assertTrue(entityDao.addMembers("emf:group", new HashSet<>(Arrays.asList("emf:newMember", "emf:oldMember")), Assert::assertNotNull));
		verify(dbDao).saveOrUpdate(new GroupMembershipEntity("emf:group", "emf:newMember"));
	}

	@Test
	public void testRemoveMembers() {
		assertFalse(entityDao.removeMembers("emf:groupId", null, Assert::assertNotNull));
		assertFalse(entityDao.removeMembers(null, null, Assert::assertNotNull));

		Set<Serializable> members = new HashSet<>();
		members.add("emf:memberToRemove1");
		ResourceEntity entity = new ResourceEntity();
		entity.setId("emf:memberToRemove2");
		members.add(entity);
		EmfResource resource = new EmfResource();
		resource.setId("emf:memberToRemove3");
		members.add(resource);
		entityDao.removeMembers("emf:groupId", members, Assert::assertNotNull);

		verify(dbDao).executeUpdate(eq(GroupMembershipEntity.REMOVE_MEMBERS_KEY), anyList());
	}

	@Test
	public void testGenerateResourceDbId() {
		ResourceEntity value = new ResourceEntity();
		value.setIdentifier("resource@tenant.com");
		String resourceDbId = entityDao.generateResourceDbId(value.getIdentifier(), () -> null);
		assertNotNull(resourceDbId);
		assertEquals("emf:resource-tenant.com", resourceDbId);
	}

	@Test
	public void testRemoveAllMembers() throws Exception {
		assertFalse(entityDao.removeAllMembers(null));

		entityDao.removeAllMembers("emf:group_to_delete");
		verify(dbDao).executeUpdate(eq(GroupMembershipEntity.REMOVE_GROUP_MEMBERS_KEY), anyList());
	}

	@Test
	public void testRemoveParticipation() throws Exception {
		assertFalse(entityDao.removeParticipation(null));

		entityDao.removeParticipation("emf:user");
		verify(dbDao).executeUpdate(eq(GroupMembershipEntity.REMOVE_MEMPER_PARTICIPATION_KEY), anyList());
	}

}
