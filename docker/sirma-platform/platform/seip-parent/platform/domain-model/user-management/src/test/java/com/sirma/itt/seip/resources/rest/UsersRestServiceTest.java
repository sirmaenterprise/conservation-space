package com.sirma.itt.seip.resources.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.io.FileTestUtils;
import com.sirma.itt.seip.testutil.mocks.SearchResultAnswer;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Unit tests for {@link UsersRestService}.
 *
 * @author yasko
 */
public class UsersRestServiceTest {

	@InjectMocks
	private UsersRestService usersRestService;

	@Mock
	private ResourceService resourceService;

	@Mock
	private SearchService searchService;

	@Mock
	private AuthorityService authorityService;

	@Mock
	private SecurityContext securityContext;

	private User testUser;

	private Map<Serializable, Resource> resources = new HashMap<>();

	/**
	 * Initialize fields and mocks before each test.
	 */
	@Before
	public void initTest() {
		testUser = createUser("test", "Test", "Testing");
		usersRestService = new UsersRestService();
		MockitoAnnotations.initMocks(this);

		when(resourceService.loadByDbId(anyString())).then(invocation -> resources.get(invocation.getArguments()[0]));

		resources.clear();
		resources.put(testUser.getId(), testUser);
	}

	/**
	 * Test user find by id.
	 */
	@Test
	public void testFindById() {
		when(resourceService.getResource("emf:test")).thenReturn(testUser);
		when(authorityService.isAdminOrSystemUser(any(Resource.class))).thenReturn(true);

		String expected = FileTestUtils.readFile("/com/sirma/itt/seip/resources/rest/userResponse.json");
		String find = usersRestService.find("emf:test");
		JsonAssert.assertJsonEquals(expected, find);
	}

	/**
	 * Test user find by username.
	 */
	@Test
	public void testFindByUsername() {
		when(resourceService.getResource(anyString())).thenReturn(null);
		when(resourceService.getResource("test", ResourceType.UNKNOWN)).thenReturn(testUser);
		when(authorityService.isAdminOrSystemUser(any(Resource.class))).thenReturn(true);

		String expected = FileTestUtils.readFile("/com/sirma/itt/seip/resources/rest/userResponse.json");
		String find = usersRestService.find("test");
		JsonAssert.assertJsonEquals(expected, find);
	}

	/**
	 * Test find with no result.
	 */
	@Test
	public void testFindNothing() {
		when(resourceService.getResource("emf:noboby")).thenReturn(null);
		when(resourceService.getResource("noboby", ResourceType.UNKNOWN)).thenReturn(null);

		String find = usersRestService.find("emf:nobody");
		Assert.assertNull(find);
	}

	/**
	 * Test search for users and groups by a partial word in name, display name, etc.
	 */
	@Test
	public void testSearchUserAndGroupSearch() {
		List<Instance> results = new ArrayList<>(1);
		SearchResultAnswer<Instance> answer = new SearchResultAnswer<>(1, results);
		Mockito.doAnswer(answer).when(searchService).search(Matchers.<Class<Instance>> any(),
				Matchers.<SearchArguments<Instance>> any());
		when(authorityService.isAdminOrSystemUser(any(Resource.class))).thenReturn(false);

		results.add(testUser);
		when(resourceService.loadByDbId(anyList())).thenReturn(results);

		String response = usersRestService.search("test", false, true, 25, 1);
		String expected = FileTestUtils.readFile("/com/sirma/itt/seip/resources/rest/usersResponse.json");
		JsonAssert.assertJsonEquals(expected, response);

		response = usersRestService.search(" ", false, true, 25, 1);
		JsonAssert.assertJsonEquals(expected, response);

		Group group = createGroup("group", "Group Grouping");
		resources.put(group.getId(), group);
		results.clear();
		results.add(group);

		response = usersRestService.search("group", true, false, 25, 1);
		expected = FileTestUtils.readFile("/com/sirma/itt/seip/resources/rest/groupsResponse.json");
		JsonAssert.assertJsonEquals(expected, response);

		answer.setTotal(2);
		results.clear();
		results.add(group);
		results.add(testUser);
		response = usersRestService.search("*", true, true, 25, 1);
		expected = FileTestUtils.readFile("/com/sirma/itt/seip/resources/rest/groupsAndUsersResponse.json");
		JsonAssert.assertJsonEquals(expected, response);
	}

	/**
	 * Tests getResources with empty data.
	 */
	@Test
	public void testGetResources_withEmptyData() {
		Response response = usersRestService.getResources("");
		Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * Tests getResources with corrupted json.
	 */
	@Test
	public void testGetResources_withCorruptedJson() {
		Response response = usersRestService.getResources("{[]}");
		Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	/**
	 * Tests getResources with two user ids in the request json.
	 */
	@Test
	public void testGetResources_withTwoUsers() {
		String json = "[\"emf:user1\",\"emf:user2\"]";
		User user1 = createUser("user1", "User1", "User1");
		User user2 = createUser("user2", "User2", "User2");

		when(resourceService.findResource(Matchers.anyString())).thenReturn(user1).thenReturn(user2);

		Response response = usersRestService.getResources(json);
		Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * Create a dummy {@link User} instance.
	 *
	 * @param username
	 *            Username to set. This will also be used to create and id for the user in the form emf:<username>
	 * @param first
	 *            User's first name.
	 * @param last
	 *            User's last name.
	 * @return the created user instance.
	 */
	private static User createUser(String username, String first, String last) {
		User user = new EmfUser(username);
		user.setId("emf:" + username);
		user.add(ResourceProperties.FIRST_NAME, first);
		user.add(ResourceProperties.LAST_NAME, last);
		return user;
	}

	/**
	 * Create a dummy {@link Group} instance.
	 *
	 * @param groupId
	 *            Group id to set. This will also be used to generate the id for the group in the for of emf:<groupId>.
	 * @param displayName
	 *            Group's display name.
	 * @return the created group instance.
	 */
	private static Group createGroup(String groupId, String displayName) {
		Group group = new EmfGroup(groupId, displayName);
		group.setId("emf:" + groupId);
		return group;
	}
}
