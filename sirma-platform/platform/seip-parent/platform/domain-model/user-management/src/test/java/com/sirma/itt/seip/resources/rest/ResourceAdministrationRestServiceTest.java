package com.sirma.itt.seip.resources.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for {@link ResourceAdministrationRestService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 08/08/2017
 */
public class ResourceAdministrationRestServiceTest {

	private static final String ALL_OTHER_USERS = "allOtherUsers";

	@InjectMocks
	private ResourceAdministrationRestService service;

	@Mock
	private ResourceService resourceService;
	@Mock
	private SearchService searchService;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private UriInfo info;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(resourceService.getAllOtherUsers()).thenReturn(createGroup(ALL_OTHER_USERS));
		when(resourceService.getAllResources(ResourceType.USER, DefaultProperties.TITLE)).thenReturn(Arrays.asList(
				createUser("user1"),
				createUser("user2"), createUser("user3"), createUser("user4"), createUser("user5")));
		when(resourceService.getAllResources(ResourceType.GROUP, DefaultProperties.TITLE)).thenReturn(Arrays.asList(
				createGroup("group1"),
				createGroup("group2"), createGroup("group3"),
				createGroup("group4"), createGroup("group5"),
				createGroup(ALL_OTHER_USERS)));
		when(securityContext.getCurrentTenantId()).thenReturn("tenant.com");
	}

	private static Group createGroup(String groupName) {
		EmfGroup group = new EmfGroup(groupName, groupName);
		group.setId("emf:GROUP_" + groupName);
		return group;
	}

	private static User createUser(String username) {
		EmfUser user = new EmfUser(username + "@tenant.com");
		user.setId("emf:" + username);
		return user;
	}

	@Test(expected = ResourceException.class)
	public void getAllUsers_shouldFailIfInvalidRequest() throws Exception {
		service.getAllUsers(null, info);
	}

	@Test
	public void getAllUsers_shouldHonourRequestedPagination() throws Exception {
		mockArguments(2, 2);

		SearchArguments<Instance> allUsers = service.getAllUsers(null, info);
		assertNotNull(allUsers);
		assertEquals(2, allUsers.getResult().size());
		assertEquals(5, allUsers.getTotalItems());

		assertEquals("user3@tenant.com", ((Resource) allUsers.getResult().get(0)).getName());
		assertEquals("user4@tenant.com", ((Resource) allUsers.getResult().get(1)).getName());
	}

	@Test
	public void getAllUsers_shouldHandleNoRequestInfo() throws Exception {
		mockArguments(null, null);

		SearchArguments<Instance> allUsers = service.getAllUsers(null, info);
		assertNotNull(allUsers);
		assertEquals("Expected page size to be higher than 5", 5, allUsers.getResult().size());
		assertEquals(5, allUsers.getTotalItems());

		assertEquals("user1@tenant.com", ((Resource) allUsers.getResult().get(0)).getName());
		assertEquals("user2@tenant.com", ((Resource) allUsers.getResult().get(1)).getName());
	}

	@Test
	public void getAllUsers_shouldHandlePageBiggerThenTheActualData() throws Exception {
		mockArguments(2, 10);

		SearchArguments<Instance> allUsers = service.getAllUsers(null, info);
		assertNotNull(allUsers);
		assertEquals(0, allUsers.getResult().size());
		assertEquals(5, allUsers.getTotalItems());
	}

	@Test
	public void getAllUsers_shouldHonourRequestedSpecificUserByName() throws Exception {
		mockArguments(2, 2);

		SearchArguments<Instance> allUsers = service.getAllUsers("user4", info);
		assertNotNull(allUsers);
		assertEquals(2, allUsers.getResult().size());
		assertEquals(5, allUsers.getTotalItems());

		assertEquals("user3@tenant.com", ((Resource) allUsers.getResult().get(0)).getName());
		assertEquals("user4@tenant.com", ((Resource) allUsers.getResult().get(1)).getName());
	}

	@Test
	public void getAllUsers_shouldHonourRequestedSpecificUserById() throws Exception {
		mockArguments(2, 2);

		SearchArguments<Instance> allUsers = service.getAllUsers("emf:user4", info);
		assertNotNull(allUsers);
		assertEquals(2, allUsers.getResult().size());
		assertEquals(5, allUsers.getTotalItems());

		assertEquals("user3@tenant.com", ((Resource) allUsers.getResult().get(0)).getName());
		assertEquals("user4@tenant.com", ((Resource) allUsers.getResult().get(1)).getName());
	}

	@Test
	public void getAllGroups_shouldHonourRequestedPagination() throws Exception {
		mockArguments(2, 2);

		SearchArguments<Instance> allGroups = service.getAllGroups(null, info);
		assertNotNull(allGroups);
		assertEquals(2, allGroups.getResult().size());
		assertEquals(5, allGroups.getTotalItems());

		assertEquals("group3", ((Resource) allGroups.getResult().get(0)).getName());
		assertEquals("group4", ((Resource) allGroups.getResult().get(1)).getName());
	}

	@Test
	public void getAllGroups_shouldHandleNoRequestInfo() throws Exception {
		mockArguments(null, null);

		SearchArguments<Instance> allGroups = service.getAllGroups(null, info);
		assertNotNull(allGroups);
		assertEquals("Expected page size to be higher than 5", 5, allGroups.getResult().size());
		assertEquals(5, allGroups.getTotalItems());

		assertEquals("group1", ((Resource) allGroups.getResult().get(0)).getName());
		assertEquals("group2", ((Resource) allGroups.getResult().get(1)).getName());
	}

	@Test
	public void getAllGroups_shouldHandlePageBiggerThenTheActualData() throws Exception {
		mockArguments(2, 10);

		SearchArguments<Instance> allGroups = service.getAllGroups(null, info);
		assertNotNull(allGroups);
		assertEquals(0, allGroups.getResult().size());
		assertEquals(5, allGroups.getTotalItems());
	}

	@Test
	public void getAllGroups_shouldHonourRequestedSpecificGroupByName() throws Exception {
		mockArguments(2, 2);

		SearchArguments<Instance> allGroups = service.getAllGroups("group3", info);
		assertNotNull(allGroups);
		assertEquals(2, allGroups.getResult().size());
		assertEquals(5, allGroups.getTotalItems());

		assertEquals("group3", ((Resource) allGroups.getResult().get(0)).getName());
		assertEquals("group4", ((Resource) allGroups.getResult().get(1)).getName());
	}

	@Test
	public void getAllGroups_shouldHonourRequestedSpecificGroupById() throws Exception {
		mockArguments(2, 2);

		SearchArguments<Instance> allGroups = service.getAllGroups("emf:GROUP_group3", info);
		assertNotNull(allGroups);
		assertEquals(2, allGroups.getResult().size());
		assertEquals(2, allGroups.getPageNumber());
		assertEquals(5, allGroups.getTotalItems());

		assertEquals("group3", ((Resource) allGroups.getResult().get(0)).getName());
		assertEquals("group4", ((Resource) allGroups.getResult().get(1)).getName());
	}

	@Test
	public void getAllGroups_shouldFilterOutAllOtherUsersGroup() throws Exception {
		mockArguments(1, 10);

		SearchArguments<Instance> allGroups = service.getAllGroups(null, info);
		assertNotNull(allGroups);
		assertEquals(5, allGroups.getResult().size());
	}

	private void mockArguments(Integer pageNumber, Integer pageSize) {
		SearchArguments<Object> arguments = new SearchArguments<>();
		if (pageNumber != null) {
			arguments.setPageNumber(pageNumber);
		}
		if (pageSize != null) {
			arguments.setPageSize(pageSize);
		}
		when(searchService.parseRequest(any())).thenReturn(arguments);
	}

}
