package com.sirma.itt.seip.resources.synchronization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.adapter.CMFGroupService;
import com.sirma.itt.seip.resources.adapter.CMFUserService;

/**
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 07/08/2017
 */
public class AlfrescoRemoteUserStoreAdapterTest {

	@InjectMocks
	private AlfrescoRemoteUserStoreAdapter storeAdapter;

	@Mock
	private CMFUserService userService;
	@Mock
	private CMFGroupService groupService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		storeAdapter.init();
	}

	@Test
	public void isExistingUser() throws Exception {
		when(userService.findUser("user1")).thenReturn(new EmfUser());

		assertTrue(storeAdapter.isExistingUser("user1"));
		assertFalse(storeAdapter.isExistingUser("user2"));
	}

	@Test
	public void isExistingGroup() throws Exception {
		when(groupService.findGroup("group1")).thenReturn(new EmfGroup());

		assertTrue(storeAdapter.isExistingGroup("group1"));
		assertFalse(storeAdapter.isExistingGroup("group2"));
	}

	@Test
	public void getUserData() throws Exception {
		when(userService.findUser("user1")).thenReturn(new EmfUser());
		Optional<User> user1 = storeAdapter.getUserData("user1");
		assertNotNull(user1);
		assertTrue(user1.isPresent());
	}

	@Test
	public void getAllUsers() throws Exception {
		when(userService.getAllUsers()).thenReturn(Arrays.asList(new EmfUser("user1"), new EmfUser("user2")));
		Collection<User> allUsers = storeAdapter.getAllUsers();
		assertEquals(2, allUsers.size());
	}

	@Test
	public void getAllGroups() throws Exception {
		when(groupService.getAllGroups()).thenReturn(Arrays.asList(new EmfGroup("group1", "group1"),
				new EmfGroup("group2", "group2")));
		Collection<Group> allGroups = storeAdapter.getAllGroups();
		assertEquals(2, allGroups.size());
	}

	@Test
	public void getUsersInGroup() throws Exception {
		when(groupService.getUsersInAuthority(any())).thenReturn(Arrays.asList("user1", "user2"));
		Collection<String> users = storeAdapter.getUsersInGroup("group1");
		assertEquals(2, users.size());
	}

	@Test
	public void getGroupsOfUser() throws Exception {
		when(groupService.getAuthorities(any())).thenReturn(Arrays.asList(new EmfGroup("group1", "group1"),
				new EmfGroup("group2", "group2")));
		Collection<String> grpups = storeAdapter.getGroupsOfUser("user1");
		assertEquals(2, grpups.size());
	}

	@Test
	public void getUserClaim() throws Exception {
		User user = new EmfUser("user1");
		user.add("firstName", "User");
		when(userService.findUser("user1")).thenReturn(user);

		String userClaim = storeAdapter.getUserClaim("user1", "urn:scim:schemas:core:1.0:name.givenName");
		assertEquals("User", userClaim);
	}

	@Test
	public void getUserClaims() throws Exception {
		User user = new EmfUser("user1");
		user.add("firstName", "User");
		user.add("lastName", "1");
		user.add("someProperty", "sameValue");
		when(userService.findUser("user1")).thenReturn(user);

		Map<String, String> claims = storeAdapter.getUserClaims("user1", "urn:scim:schemas:core:1.0:name.givenName",
				"urn:scim:schemas:core:1.0:name.familyName", "someMissingClaim");
		assertEquals("User", claims.get("urn:scim:schemas:core:1.0:name.givenName"));
		assertEquals("1", claims.get("urn:scim:schemas:core:1.0:name.familyName"));
		assertEquals(2, claims.size());
	}

	@Test
	public void getUserClaims_withInvalidArgumentsProducesEmptyMap() throws Exception {

		assertTrue(storeAdapter.getUserClaims(null).isEmpty());
		assertTrue(storeAdapter.getUserClaims("").isEmpty());
		assertTrue(storeAdapter.getUserClaims("user1").isEmpty());
		assertTrue(storeAdapter.getUserClaims("user1", null).isEmpty());
	}

	@Test
	public void isReadOnly_ShouldReturnTrue() {
		assertTrue(storeAdapter.isReadOnly());
	}

	@Test
	public void isGroupInGroupSupported_ShouldReturnTrue() {
		assertTrue(storeAdapter.isGroupInGroupSupported());
	}

	@Test
	public void getName_ShouldBeAlfresco() {
		assertEquals("alfresco", storeAdapter.getName());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void createUser_ShouldThrowException() {
		storeAdapter.createUser(new EmfUser());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void updateUser_ShouldThrowException() {
		storeAdapter.updateUser(new EmfUser());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void deleteUser_ShouldThrowException() {
		storeAdapter.deleteUser(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void createGroup_ShouldThrowException() {
		storeAdapter.createGroup(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void deleteGroup_ShouldThrowException() {
		storeAdapter.deleteGroup(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void updateGroupsOfUser_ShouldThrowException() {
		storeAdapter.updateGroupsOfUser(null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void updateUsersInGroup_ShouldThrowException() {
		storeAdapter.updateUsersInGroup(null, null, null);
	}

}
