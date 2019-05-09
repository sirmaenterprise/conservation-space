package com.sirma.sep.keycloak.synchronization;

import static com.sirma.itt.seip.resources.ResourceProperties.GROUP_PREFIX;
import static com.sirma.sep.keycloak.synchronization.KeycloakRemoteUserStoreAdapter.IDP_ADMIN_GROUP;
import static com.sirma.sep.keycloak.synchronization.UserClaims.EMAIL;
import static com.sirma.sep.keycloak.synchronization.UserClaims.FIRST_NAME;
import static com.sirma.sep.keycloak.synchronization.UserClaims.IS_DISABLED;
import static com.sirma.sep.keycloak.synchronization.UserClaims.LAST_NAME;
import static com.sirma.sep.keycloak.synchronization.UserClaims.MOBILE;
import static com.sirma.sep.keycloak.synchronization.UserClaims.USERNAME;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.ReadOnlyStoreException;
import com.sirma.itt.seip.resources.RemoteStoreException;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.keycloak.ClientProperties;
import com.sirma.sep.keycloak.ldap.LdapConfiguration;
import com.sirma.sep.keycloak.ldap.LdapMode;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;

/**
 * Tests for {@link KeycloakRemoteUserStoreAdapter}.
 *
 * @author smustafov
 */
public class KeycloakRemoteUserStoreAdapterTest {

	private static final String TENANT_ID = "sep.test";
	private static final Response CREATED_RESPONSE = Response
			.created(URI.create("http://idp/auth/admin/realms/sep.test/users/5e9dd11a-6b23-4e6d-9fd8-c5558c4c986a"))
			.build();
	private static final String UI_URL = "sep.test.ui";

	@InjectMocks
	private KeycloakRemoteUserStoreAdapter adapter;

	@Mock
	private LdapConfiguration ldapConfiguration;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private InstanceTypes instanceTypes;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private KeycloakClientProducer keycloakClientProducer;

	@Mock
	private UsersResource usersResourceRest;

	@Mock
	private GroupsResource groupsResourceRest;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);

		when(keycloakClientProducer.produceUsersResource()).thenReturn(usersResourceRest);
		when(keycloakClientProducer.produceGroupsResource()).thenReturn(groupsResourceRest);

		when(securityContext.getCurrentTenantId()).thenReturn(TENANT_ID);

		when(definitionService.getInstanceDefinition(any(EmfUser.class))).thenReturn(createDefinitionMock());

		InstanceType instanceType = InstanceType.create(EMF.USER.toString());
		when(instanceTypes.from(EMF.USER.toString())).thenReturn(Optional.of(instanceType));

		InstanceType instanceType1 = InstanceType.create(EMF.GROUP.toString());
		when(instanceTypes.from(EMF.GROUP.toString())).thenReturn(Optional.of(instanceType1));

		when(systemConfiguration.getUi2Url()).thenReturn(new ConfigurationPropertyMock<>(UI_URL));
	}

	@Test(expected = RemoteStoreException.class)
	public void isExistingUser_Should_ThrowRemoteStoreException_When_IdpRequestFails() throws RemoteStoreException {
		when(usersResourceRest.search(anyString())).thenThrow(NotFoundException.class);
		adapter.isExistingUser("userId");
	}

	@Test
	public void isExistingUser_Should_ReturnFalse_When_UserIdIsEmpty() throws RemoteStoreException {
		assertFalse(adapter.isExistingUser(null));
		assertFalse(adapter.isExistingUser(""));
	}

	@Test
	public void isExistingUser_Should_ReturnFalse_When_UserNotInIdp() throws RemoteStoreException {
		withUsersInIdp(emptyList());

		assertFalse(adapter.isExistingUser("regularuser@sep.test"));
	}

	@Test
	public void isExistingUser_Should_ReturnFalse_When_UserNotInIdp_WithMultipleResults() throws RemoteStoreException {
		withUsersInIdp(Arrays.asList("1regularuser", "regularuser1", "regularuser2", "regularuser3"));

		assertFalse(adapter.isExistingUser("regularuser@sep.test"));
	}

	@Test
	public void isExistingUser_Should_ReturnTrue_When_UserExistsInIdp() throws RemoteStoreException {
		withUsersInIdp(Collections.singletonList("regularuser"));

		assertTrue(adapter.isExistingUser("regularuser@sep.test"));
	}

	@Test
	public void isExistingUser_Should_WorkCorrectly_WithoutTenantInUserId() throws RemoteStoreException {
		withUsersInIdp(Collections.singletonList("regularuser"));

		assertTrue(adapter.isExistingUser("regularuser"));
	}

	@Test
	public void isExistingUser_Should_PerformCaseInsensitiveCheck() throws RemoteStoreException {
		withUsersInIdp(Collections.singletonList("regularuser"));

		assertTrue(adapter.isExistingUser("Regularuser"));
	}

	@Test
	public void isExistingUser_Should_FindCorrectUser_When_IdpReturnsMultipleUsers() throws RemoteStoreException {
		withUsersInIdp(Arrays.asList("01regularuser", "2regularuser", "regularuser", "regularuser5"));

		assertTrue(adapter.isExistingUser("regularuser"));
	}

	@Test(expected = RemoteStoreException.class)
	public void isExistingGroup_Should_ThrowStoreException_When_IdpRequestFails() throws RemoteStoreException {
		when(groupsResourceRest.groups(anyString(), any(), any())).thenThrow(NotFoundException.class);
		adapter.isExistingGroup("groupId");
	}

	@Test
	public void isExistingGroup_Should_ReturnFalse_When_GroupIdIsEmpty() throws RemoteStoreException {
		assertFalse(adapter.isExistingGroup(null));
		assertFalse(adapter.isExistingGroup(""));
	}

	@Test
	public void isExistingGroup_Should_ReturnTrue_When_GroupExistsInIdp() throws RemoteStoreException {
		withGroupsInIdp(Collections.singletonList("Wizards"));

		assertTrue(adapter.isExistingGroup("GROUP_Wizards"));
	}

	@Test
	public void isExistingGroup_Should_ReturnFalse_When_GroupNotInIdp() throws RemoteStoreException {
		withGroupsInIdp(emptyList());

		assertFalse(adapter.isExistingGroup("GROUP_Wizards"));
	}

	@Test
	public void isExistingGroup_Should_ReturnFalse_When_GroupNotInIdp_WithMultipleResults()
			throws RemoteStoreException {
		withGroupsInIdp(Arrays.asList("Wizard", "Wizards2", "OldWizards"));

		assertFalse(adapter.isExistingGroup("GROUP_Wizards"));
	}

	@Test
	public void isExistingGroup_Should_CorrectlyWork_WithAdminGroup() throws RemoteStoreException {
		withGroupsInIdp(Collections.singletonList(IDP_ADMIN_GROUP));

		assertTrue(adapter.isExistingGroup("GROUP_" + ResourceService.SYSTEM_ADMIN_GROUP_ID));
	}

	@Test
	public void isExistingGroup_Should_ReturnTrue_When_RequestingEveryone() throws RemoteStoreException {
		assertTrue(adapter.isExistingGroup(ResourceService.EVERYONE_GROUP_ID));
	}

	@Test(expected = RemoteStoreException.class)
	public void getUserData_Should_ThrowStoreException_When_IdpRequestFails() throws RemoteStoreException {
		when(usersResourceRest.search(anyString())).thenThrow(InternalServerErrorException.class);
		adapter.getUserData("userId");
	}

	@Test
	public void getUserData_Should_CorrectlyMapAllUserProperties() throws RemoteStoreException {
		UserRepresentation regularuser = createIdpUser("regularuser", "Regular", "User", "regularuser@company.com");
		Map<String, List<String>> attributes = new HashMap<>();
		attributes.put(IS_DISABLED, Collections.singletonList("false"));
		attributes.put("mobile", Collections.singletonList("123-456-789"));
		regularuser.setAttributes(attributes);

		when(usersResourceRest.search(regularuser.getUsername())).thenReturn(Collections.singletonList(regularuser));

		Optional<User> userData = adapter.getUserData("regularuser@sep.test");

		assertTrue(userData.isPresent());
		User user = userData.get();
		assertEquals(ResourceType.USER, user.getType());
		assertEquals(EMF.USER.toString(), user.type().toString());
		assertEquals("regularuser@sep.test", user.getIdentityId());
		assertEquals("regularuser@sep.test", user.getName());
		assertEquals(6, user.getProperties().size());
		assertEquals("regularuser@sep.test", user.get(ResourceProperties.USER_ID));
		assertEquals("Regular", user.get(ResourceProperties.FIRST_NAME));
		assertEquals("User", user.get(ResourceProperties.LAST_NAME));
		assertEquals("regularuser@company.com", user.get(ResourceProperties.EMAIL));
		assertEquals(Boolean.TRUE, user.get(ResourceProperties.IS_ACTIVE));
		assertEquals("123-456-789", user.get("mobile"));
	}

	@Test
	public void getUserData_Should_CorrectlyReturnOnlyMappedUserProperties() throws RemoteStoreException {
		UserRepresentation john = createIdpUser("john", "John", "Smith", "john@mail.com");
		Map<String, List<String>> attributes = new HashMap<>();
		attributes.put(IS_DISABLED, Collections.singletonList("true"));
		attributes.put("mobile", Collections.singletonList("123-456-789"));
		john.setAttributes(attributes);

		when(usersResourceRest.search(john.getUsername())).thenReturn(Collections.singletonList(john));
		when(definitionService.getInstanceDefinition(any(EmfUser.class))).thenReturn(createDefinitionMock(
				Arrays.asList(new Pair<>(ResourceProperties.IS_ACTIVE, IS_DISABLED),
						new Pair<>(ResourceProperties.EMAIL, EMAIL))));

		Optional<User> userData = adapter.getUserData("john@sep.test");

		assertTrue(userData.isPresent());
		User user = userData.get();
		assertEquals(ResourceType.USER, user.getType());
		assertEquals(EMF.USER.toString(), user.type().toString());
		assertEquals("john@sep.test", user.getIdentityId());
		assertEquals("john@sep.test", user.getName());
		assertEquals(3, user.getProperties().size());
		assertEquals("john@sep.test", user.get(ResourceProperties.USER_ID));
		assertEquals("john@mail.com", user.get(ResourceProperties.EMAIL));
		assertEquals(Boolean.FALSE, user.get(ResourceProperties.IS_ACTIVE));
	}

	@Test
	public void getUserData_Should_SkipEmptySpecialProperties() throws RemoteStoreException {
		UserRepresentation john = createIdpUser("john", "", "", "john@mail.com");
		john.setAttributes(new HashMap<>());
		when(usersResourceRest.search(john.getUsername())).thenReturn(Collections.singletonList(john));
		when(definitionService.getInstanceDefinition(any(EmfUser.class))).thenReturn(createDefinitionMock(
				Arrays.asList(new Pair<>(ResourceProperties.FIRST_NAME, FIRST_NAME),
						new Pair<>(ResourceProperties.LAST_NAME, LAST_NAME),
						new Pair<>(ResourceProperties.EMAIL, EMAIL))));

		Optional<User> userData = adapter.getUserData("john@sep.test");

		assertTrue(userData.isPresent());
		User user = userData.get();
		assertEquals(ResourceType.USER, user.getType());
		assertEquals(EMF.USER.toString(), user.type().toString());
		assertEquals("john@sep.test", user.getIdentityId());
		assertEquals("john@sep.test", user.getName());
		assertEquals("john@sep.test", user.get(ResourceProperties.USER_ID));
		assertEquals("john@mail.com", user.get(ResourceProperties.EMAIL));
		assertNull(user.get(ResourceProperties.FIRST_NAME));
		assertNull(user.get(ResourceProperties.LAST_NAME));
	}

	@Test
	public void getUserData_Should_ReturnEmptyList_When_UserIdEmpty() throws RemoteStoreException {
		assertFalse(adapter.getUserData(null).isPresent());
		assertFalse(adapter.getUserData("").isPresent());
	}

	@Test(expected = RemoteStoreException.class)
	public void getAllUsers_Should_ThrowStoreException_When_IdpRequestFails() throws RemoteStoreException {
		when(usersResourceRest.list(any(), any())).thenThrow(InternalServerErrorException.class);
		adapter.getAllUsers();
	}

	@Test
	public void getAllUsers_Should_ReturnEmptyList_When_NoUsersInIdp() throws RemoteStoreException {
		assertTrue(adapter.getAllUsers().isEmpty());
	}

	@Test
	public void getAllUsers_Should_ReturnCorrectUsers() throws RemoteStoreException {
		withUsersInIdp(Arrays.asList("regularuser", "john", "admin", "pesho"));

		Collection<User> allUsers = adapter.getAllUsers();

		assertEquals(4, allUsers.size());
		Iterator<User> iterator = allUsers.iterator();

		User user = iterator.next();
		assertEquals("regularuser@sep.test", user.getName());

		user = iterator.next();
		assertEquals("john@sep.test", user.getName());

		user = iterator.next();
		assertEquals("admin@sep.test", user.getName());

		user = iterator.next();
		assertEquals("pesho@sep.test", user.getName());
	}

	@Test(expected = RemoteStoreException.class)
	public void getAllGroups_Should_ThrowStoreException_When_IdpRequestFails() throws RemoteStoreException {
		when(groupsResourceRest.groups()).thenThrow(InternalServerErrorException.class);
		adapter.getAllGroups();
	}

	@Test
	public void getAllGroups_Should_ReturnEmptyList_When_NoGroupsInIdp() throws RemoteStoreException {
		assertTrue(adapter.getAllGroups().isEmpty());
	}

	@Test
	public void getAllGroups_Should_ReturnCorrectGroupsWithEveryoneGroupIncluded() throws RemoteStoreException {
		withGroupsInIdp(Arrays.asList("TestGroup1", "TestGroup2", "TestGroup3", IDP_ADMIN_GROUP));

		Collection<Group> allGroups = adapter.getAllGroups();

		assertEquals(5, allGroups.size());
		Iterator<Group> iterator = allGroups.iterator();

		Group group = iterator.next();
		assertEquals(GROUP_PREFIX + "TestGroup1", group.getName());
		assertEquals(ResourceType.GROUP, group.getType());
		assertEquals(EMF.GROUP.toString(), group.type().toString());

		group = iterator.next();
		assertEquals(GROUP_PREFIX + "TestGroup2", group.getName());
		assertEquals(ResourceType.GROUP, group.getType());
		assertEquals(EMF.GROUP.toString(), group.type().toString());

		group = iterator.next();
		assertEquals(GROUP_PREFIX + "TestGroup3", group.getName());
		assertEquals(ResourceType.GROUP, group.getType());
		assertEquals(EMF.GROUP.toString(), group.type().toString());

		group = iterator.next();
		assertEquals(GROUP_PREFIX + ResourceService.SYSTEM_ADMIN_GROUP_ID, group.getName());
		assertEquals(ResourceType.GROUP, group.getType());
		assertEquals(EMF.GROUP.toString(), group.type().toString());

		group = iterator.next();
		assertEquals(ResourceService.EVERYONE_GROUP_ID, group.getName());
		assertEquals(ResourceType.GROUP, group.getType());
		assertEquals(EMF.GROUP.toString(), group.type().toString());
	}

	@Test
	public void getUsersInGroup_Should_ReturnEmptyList_When_GroupIsEmpty() throws RemoteStoreException {
		assertTrue(adapter.getUsersInGroup(null).isEmpty());
		assertTrue(adapter.getUsersInGroup("").isEmpty());
	}

	@Test
	public void getUsersInGroup_Should_ReturnAllUsers_ForEveryoneGroup() throws RemoteStoreException {
		withUsersInIdp(Arrays.asList("user1", "user2", "user3", "user4", "user5", "admin"));

		Collection<String> users = adapter.getUsersInGroup(ResourceService.EVERYONE_GROUP_ID);

		assertEquals(6, users.size());
		Iterator<String> iterator = users.iterator();

		String userId = iterator.next();
		assertEquals("user1@" + TENANT_ID, userId);

		userId = iterator.next();
		assertEquals("user2@" + TENANT_ID, userId);

		userId = iterator.next();
		assertEquals("user3@" + TENANT_ID, userId);

		userId = iterator.next();
		assertEquals("user4@" + TENANT_ID, userId);

		userId = iterator.next();
		assertEquals("user5@" + TENANT_ID, userId);

		userId = iterator.next();
		assertEquals("admin@" + TENANT_ID, userId);
	}

	@Test
	public void getUsersInGroup_Should_ReturnCorrectUsersForGroup() throws RemoteStoreException {
		withGroupsInIdp(Collections.singletonList("TestGroup"));

		GroupResource groupResource = mock(GroupResource.class);
		when(groupResource.members(any(), any()))
				.thenReturn(Collections.singletonList(createIdpUser("testUser", null, null, null)));
		when(groupsResourceRest.group(any())).thenReturn(groupResource);

		Collection<String> users = adapter.getUsersInGroup(GROUP_PREFIX + "TestGroup");

		assertEquals(1, users.size());
		assertEquals("testUser@" + TENANT_ID, users.iterator().next());
	}

	@Test(expected = RemoteStoreException.class)
	public void getUsersInGroup_Should_ThrowStoreException_When_IdpRequestFails() throws RemoteStoreException {
		withGroupsInIdp(Collections.singletonList("TestGroup"));

		when(groupsResourceRest.group(any())).thenThrow(NotFoundException.class);

		adapter.getUsersInGroup(GROUP_PREFIX + "TestGroup");
	}

	@Test
	public void getGroupsOfUser_Should_ReturnEmptyList_When_UserIdIsEmpty() throws RemoteStoreException {
		assertTrue(adapter.getGroupsOfUser(null).isEmpty());
		assertTrue(adapter.getGroupsOfUser("").isEmpty());
	}

	@Test(expected = RemoteStoreException.class)
	public void getGroupsOfUser_Should_ThrowStoreException_When_IdpRequestFails() throws RemoteStoreException {
		when(usersResourceRest.search(any())).thenThrow(NotAuthorizedException.class);
		adapter.getGroupsOfUser("userId");
	}

	@Test
	public void getGroupsOfUser_Should_ReturnGroupOfUser() throws RemoteStoreException {
		withUsersInIdp(Collections.singletonList("testUser"));
		UserResource userResource = mock(UserResource.class);
		when(userResource.groups())
				.thenReturn(Arrays.asList(createIdpGroup("TestGroup1"), createIdpGroup("TestGroup2")));
		when(usersResourceRest.get(any())).thenReturn(userResource);

		Collection<String> groups = adapter.getGroupsOfUser("testUser@" + TENANT_ID);
		assertEquals(3, groups.size());

		Iterator<String> iterator = groups.iterator();

		String groupId = iterator.next();
		assertEquals(GROUP_PREFIX + "TestGroup1", groupId);

		groupId = iterator.next();
		assertEquals(GROUP_PREFIX + "TestGroup2", groupId);

		groupId = iterator.next();
		assertEquals(ResourceService.EVERYONE_GROUP_ID, groupId);
	}

	@Test
	public void getUserClaim_Should_RetrunNull_When_ParamsAreEmpty() throws RemoteStoreException {
		assertNull(adapter.getUserClaim(null, null));
		assertNull(adapter.getUserClaim(null, ""));
		assertNull(adapter.getUserClaim("", null));
		assertNull(adapter.getUserClaim("", ""));
	}

	@Test
	public void getUserClaim_Should_FetchAllClaimsCorrectly() throws RemoteStoreException {
		UserRepresentation user = createIdpUser("testUser", "Test", "User", "test@mail.com");
		Map<String, List<String>> userAttributes = new HashMap<>();
		userAttributes.put(IS_DISABLED, Collections.singletonList("false"));
		userAttributes.put(MOBILE, Collections.singletonList("123"));
		user.setAttributes(userAttributes);

		when(usersResourceRest.search("testUser")).thenReturn(Collections.singletonList(user));

		String fullUsername = "testUser@" + TENANT_ID;

		assertEquals("false", adapter.getUserClaim(fullUsername, IS_DISABLED));
		assertEquals("test@mail.com", adapter.getUserClaim(fullUsername, EMAIL));
		assertEquals("123", adapter.getUserClaim(fullUsername, MOBILE));
		assertEquals("Test", adapter.getUserClaim(fullUsername, FIRST_NAME));
		assertEquals("User", adapter.getUserClaim(fullUsername, LAST_NAME));
		assertEquals("testUser", adapter.getUserClaim(fullUsername, USERNAME));
	}

	@Test
	public void getUserClaim_Should_ReturnNull_When_ClaimHasNoValue() throws RemoteStoreException {
		UserRepresentation user = createIdpUser("testUser", "Test", "User", "test@mail.com");
		user.setAttributes(new HashMap<>());

		when(usersResourceRest.search("testUser")).thenReturn(Collections.singletonList(user));

		String fullUsername = "testUser@" + TENANT_ID;
		assertNull(adapter.getUserClaim(fullUsername, MOBILE));
	}

	@Test(expected = RemoteStoreException.class)
	public void getUserClaim_Should_ThrowStoreException_When_IdpRequestFails() throws RemoteStoreException {
		when(usersResourceRest.search(anyString())).thenThrow(InternalServerErrorException.class);
		adapter.getUserClaim("userId", MOBILE);
	}

	@Test(expected = RemoteStoreException.class)
	public void getUserClaims_Should_ThrowStoreException_When_IdpRequestFails() throws RemoteStoreException {
		when(usersResourceRest.search(anyString())).thenThrow(InternalServerErrorException.class);
		adapter.getUserClaims("userId", MOBILE, EMAIL);
	}

	@Test
	public void getUserClaims_Should_ReturnEmptyMap_When_ParamsAreEmpty() throws RemoteStoreException {
		assertTrue(adapter.getUserClaims(null, null).isEmpty());
		assertTrue(adapter.getUserClaims(null, "").isEmpty());
		assertTrue(adapter.getUserClaims("", null).isEmpty());
		assertTrue(adapter.getUserClaims("", "").isEmpty());
	}

	@Test
	public void getUserClaims_Should_ReturnAllClaimsCorrectly() throws RemoteStoreException {
		UserRepresentation user = createIdpUser("testUser", "Test", "User", "test@mail.com");
		Map<String, List<String>> userAttributes = new HashMap<>();
		userAttributes.put(IS_DISABLED, Collections.singletonList("false"));
		user.setAttributes(userAttributes);

		when(usersResourceRest.search("testUser")).thenReturn(Collections.singletonList(user));

		String fullUsername = "testUser@" + TENANT_ID;
		Map<String, String> userClaims = adapter
				.getUserClaims(fullUsername, EMAIL, MOBILE, IS_DISABLED, USERNAME, FIRST_NAME, LAST_NAME);

		assertEquals(6, userClaims.size());
		assertEquals("testUser", userClaims.get(USERNAME));
		assertEquals("test@mail.com", userClaims.get(EMAIL));
		assertEquals("false", userClaims.get(IS_DISABLED));
		assertEquals("Test", userClaims.get(FIRST_NAME));
		assertEquals("User", userClaims.get(LAST_NAME));
		assertNull(userClaims.get(MOBILE));
	}

	@Test(expected = RemoteStoreException.class)
	public void createUser_Should_ThrowException_When_ReadOnlyStore() throws Exception {
		withLdapConfig(LdapMode.READ_ONLY);
		adapter.createUser(new EmfUser());
	}

	@Test
	public void createUser_Should_CreateProperUser() throws Exception {
		withLdapConfig(LdapMode.WRITABLE);
		when(usersResourceRest.create(any())).thenReturn(CREATED_RESPONSE);
		UserResource userResource = mock(UserResource.class);
		when(usersResourceRest.get("5e9dd11a-6b23-4e6d-9fd8-c5558c4c986a")).thenReturn(userResource);

		EmfUser user = new EmfUser("regularuser@sep.test");
		user.setActive(true);
		user.setEmail("regularuser@mail.com");
		user.add(ResourceProperties.FIRST_NAME, "John");
		user.add(ResourceProperties.LAST_NAME, "Doe");

		adapter.createUser(user);

		verify(userResource).executeActionsEmail(ClientProperties.SEP_UI_CLIENT_ID, getUiUrl(),
				Collections.singletonList(UserActions.UPDATE_PASSWORD.toString()));

		ArgumentCaptor<UserRepresentation> argumentCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
		verify(usersResourceRest).create(argumentCaptor.capture());
		UserRepresentation createdUser = argumentCaptor.getValue();
		assertTrue(createdUser.isEnabled());
		assertEquals("regularuser", createdUser.getUsername());
		assertEquals("regularuser@mail.com", createdUser.getEmail());
		assertEquals("John", createdUser.getFirstName());
		assertEquals("Doe", createdUser.getLastName());
		assertEquals("false", createdUser.getAttributes().get(UserClaims.IS_DISABLED).get(0));
	}

	@Test(expected = RemoteStoreException.class)
	public void updateUser_Should_ThrowException_When_ReadOnlyStore() throws Exception {
		withLdapConfig(LdapMode.READ_ONLY);
		adapter.updateUser(new EmfUser());
	}

	@Test
	public void updateUser_Should_ProperlyUpdateUser() throws Exception {
		withLdapConfig(LdapMode.WRITABLE);
		withUsersInIdp(Collections.singletonList("regularuser"));
		UserResource userResource = mock(UserResource.class);
		when(usersResourceRest.get("regularuser")).thenReturn(userResource);

		EmfUser user = new EmfUser("regularuser@sep.test");
		user.setActive(false);
		user.setEmail("regularuser@mail.com");
		user.add(UserClaims.MOBILE, "123456789");
		user.add(ResourceProperties.FIRST_NAME, "John");
		user.add(ResourceProperties.LAST_NAME, "Doe");

		adapter.updateUser(user);

		verify(userResource).logout();

		ArgumentCaptor<UserRepresentation> argumentCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
		verify(userResource).update(argumentCaptor.capture());
		UserRepresentation updatedUser = argumentCaptor.getValue();
		assertFalse(updatedUser.isEnabled());
		assertEquals("regularuser", updatedUser.getUsername());
		assertEquals("regularuser@mail.com", updatedUser.getEmail());
		assertEquals("John", updatedUser.getFirstName());
		assertEquals("Doe", updatedUser.getLastName());

		Map<String, List<String>> attributes = updatedUser.getAttributes();
		assertEquals("true", attributes.get(UserClaims.IS_DISABLED).get(0));
		assertEquals("123456789", attributes.get(UserClaims.MOBILE).get(0));
	}

	@Test
	public void updateUser_Should_NotRemoveUserSessions_When_UserActive() throws Exception {
		withLdapConfig(LdapMode.WRITABLE);
		withUsersInIdp(Collections.singletonList("regularuser"));
		UserResource userResource = mock(UserResource.class);
		when(usersResourceRest.get("regularuser")).thenReturn(userResource);

		EmfUser user = new EmfUser("regularuser@sep.test");
		user.setActive(true);

		adapter.updateUser(user);

		verify(userResource, never()).logout();
	}

	@Test(expected = RemoteStoreException.class)
	public void deleteUser_Should_ThrowException_When_ReadOnlyStore() throws Exception {
		withLdapConfig(LdapMode.READ_ONLY);
		adapter.deleteUser("test");
	}

	@Test
	public void deleteUser_Should_DoNothing_When_UserIdEmpty() throws Exception {
		withLdapConfig(LdapMode.WRITABLE);
		UserResource userResource = mock(UserResource.class);
		when(usersResourceRest.get(any())).thenReturn(userResource);

		adapter.deleteUser(null);

		verify(userResource, never()).remove();
	}

	@Test
	public void deleteUser_Should_DeleteProperUser() throws Exception {
		withLdapConfig(LdapMode.WRITABLE);
		withUsersInIdp(Collections.singletonList("regularuser"));
		UserResource userResource = mock(UserResource.class);
		when(usersResourceRest.get("regularuser")).thenReturn(userResource);

		adapter.deleteUser("regularuser@sep.test");

		verify(userResource).remove();
	}

	@Test
	public void deleteUser_Should_DoNothingIfUserNotFound() throws Exception {
		withLdapConfig(LdapMode.WRITABLE);
		withUsersInIdp(Collections.singletonList("regularuser"));
		UserResource userResource = mock(UserResource.class);
		when(usersResourceRest.get("regularuser")).thenReturn(userResource);

		adapter.deleteUser("test@sep.test");

		verify(userResource, never()).remove();
	}

	@Test(expected = ReadOnlyStoreException.class)
	public void createGroup_Should_ThrowException_When_ReadOnlyStore() throws RemoteStoreException {
		withLdapConfig(LdapMode.READ_ONLY);
		adapter.createGroup(new EmfGroup());
	}

	@Test
	public void createGroup_Should_CreateProperGroup() throws RemoteStoreException {
		withLdapConfig(LdapMode.WRITABLE);

		adapter.createGroup(buildGroup("GROUP_Managers"));

		ArgumentCaptor<GroupRepresentation> argumentCaptor = ArgumentCaptor.forClass(GroupRepresentation.class);
		verify(groupsResourceRest).add(argumentCaptor.capture());
		GroupRepresentation createdGroup = argumentCaptor.getValue();
		assertEquals("Managers", createdGroup.getName());
	}

	@Test
	public void createGroup_Should_DoNothing_ForEveryoneGroup() throws RemoteStoreException {
		withLdapConfig(LdapMode.WRITABLE);

		adapter.createGroup(buildGroup(ResourceService.EVERYONE_GROUP_ID));

		verify(groupsResourceRest, never()).add(any(GroupRepresentation.class));
	}

	@Test(expected = ReadOnlyStoreException.class)
	public void deleteGroup_Should_ThrowException_When_ReadOnlyStore() throws RemoteStoreException {
		withLdapConfig(LdapMode.READ_ONLY);
		adapter.deleteGroup("testGroup");
	}

	@Test
	public void deleteGroup_Should_DeleteCorrectGroup() throws RemoteStoreException {
		withLdapConfig(LdapMode.WRITABLE);
		withGroupsInIdp(Collections.singletonList("TestGroup"));
		GroupResource groupResource = mock(GroupResource.class);
		when(groupsResourceRest.group("TestGroup")).thenReturn(groupResource);

		adapter.deleteGroup("GROUP_TestGroup");

		verify(groupResource).remove();
	}

	@Test
	public void deleteGroup_Should_DoNothing_ForEveryoneGroup() throws RemoteStoreException {
		withLdapConfig(LdapMode.WRITABLE);
		GroupResource groupResource = mock(GroupResource.class);
		when(groupsResourceRest.group(any())).thenReturn(groupResource);

		adapter.deleteGroup(ResourceService.EVERYONE_GROUP_ID);

		verify(groupResource, never()).remove();
	}

	@Test
	public void deleteGroup_Should_DoNothing_WithEmptyGroupId() throws RemoteStoreException {
		withLdapConfig(LdapMode.WRITABLE);
		GroupResource groupResource = mock(GroupResource.class);
		when(groupsResourceRest.group(any())).thenReturn(groupResource);

		adapter.deleteGroup(null);
		adapter.deleteGroup("");

		verify(groupResource, never()).remove();
	}

	@Test(expected = ReadOnlyStoreException.class)
	public void updateGroupsOfUser_Should_ThrowException_When_ReadOnlyStore() throws RemoteStoreException {
		withLdapConfig(LdapMode.READ_ONLY);
		adapter.updateGroupsOfUser("user", emptyList(), emptyList());
	}

	@Test
	public void updateGroupsOfUser_Should_UpdateProperMembership() throws RemoteStoreException {
		withLdapConfig(LdapMode.WRITABLE);
		withUsersInIdp(Collections.singletonList("testUser1"));
		withGroupsInIdp(Arrays.asList("TestGroup1", "TestGroup2"));
		UserResource userResource = mock(UserResource.class);
		when(usersResourceRest.get("testUser1")).thenReturn(userResource);

		adapter.updateGroupsOfUser("testUser1@" + TENANT_ID, Collections.singletonList("GROUP_TestGroup1"),
				Collections.singletonList("GROUP_TestGroup2"));

		verify(userResource).leaveGroup("TestGroup1");
		verify(userResource).joinGroup("TestGroup2");
	}

	@Test
	public void updateGroupsOfUser_Should_DoNothing_ForEmptyUserId() throws RemoteStoreException {
		withLdapConfig(LdapMode.WRITABLE);
		UserResource userResource = mock(UserResource.class);
		when(usersResourceRest.get("testUser1")).thenReturn(userResource);

		adapter.updateGroupsOfUser(null, Collections.singletonList("GROUP_TestGroup1"),
				Collections.singletonList("GROUP_TestGroup2"));

		verify(userResource, never()).leaveGroup(any());
		verify(userResource, never()).joinGroup(any());
	}

	@Test(expected = ReadOnlyStoreException.class)
	public void updateUsersInGroup_Should_ThrowException_When_ReadOnlyStore() throws RemoteStoreException {
		withLdapConfig(LdapMode.READ_ONLY);
		adapter.updateUsersInGroup("group", emptyList(), emptyList());
	}

	@Test
	public void updateUsersInGroup_Should_UpdateProperMemberships() throws RemoteStoreException {
		withLdapConfig(LdapMode.WRITABLE);
		withGroupsInIdp(Collections.singletonList("TestGroup1"));
		withUsersInIdp(Arrays.asList("testUser1", "testUser2", "testUser3", "testUser4"));
		UserResource userResource = mock(UserResource.class);
		when(usersResourceRest.get("testUser1")).thenReturn(userResource);
		when(usersResourceRest.get("testUser2")).thenReturn(userResource);
		when(usersResourceRest.get("testUser3")).thenReturn(userResource);
		when(usersResourceRest.get("testUser4")).thenReturn(userResource);

		adapter.updateUsersInGroup("GROUP_TestGroup1", Arrays.asList("testUser1@sep.test", "testUser2@sep.test"),
				Arrays.asList("testUser4@sep.test", "testUser3@sep.test"));

		verify(userResource, times(2)).leaveGroup("TestGroup1");
		verify(userResource, times(2)).joinGroup("TestGroup1");
	}

	@Test
	public void updateUsersInGroup_Should_DoNothing_WithEmptyGroupId() throws RemoteStoreException {
		withLdapConfig(LdapMode.WRITABLE);
		withGroupsInIdp(Collections.singletonList("TestGroup1"));
		UserResource userResource = mock(UserResource.class);
		when(usersResourceRest.get(any())).thenReturn(userResource);

		adapter.updateUsersInGroup(null, Arrays.asList("testUser1@sep.test", "testUser2@sep.test"),
				Arrays.asList("testUser4@sep.test", "testUser3@sep.test"));

		verify(userResource, never()).leaveGroup(anyString());
		verify(userResource, never()).joinGroup(anyString());
	}

	@Test
	public void isReadOnly_ShouldReturnTrue_When_LdapModeIsReadOnly() throws RemoteStoreException {
		withLdapConfig(LdapMode.READ_ONLY);
		assertTrue(adapter.isReadOnly());
	}

	@Test
	public void isReadOnly_ShouldReturnFalse_When_LdapModeIsNotReadOnly() throws RemoteStoreException {
		withLdapConfig(LdapMode.WRITABLE);
		assertFalse(adapter.isReadOnly());
	}

	@Test
	public void getName_ShouldReturnKeycloak() {
		assertEquals(KeycloakRemoteUserStoreAdapter.NAME, adapter.getName());
	}

	@Test
	public void shouldSupportNestedGroups() {
		assertTrue(adapter.isGroupInGroupSupported());
	}

	private void withUsersInIdp(List<String> userIds) {
		List<UserRepresentation> users = new ArrayList<>();
		for (String userId : userIds) {
			UserRepresentation userRepresentation = new UserRepresentation();
			userRepresentation.setUsername(userId);
			userRepresentation.setId(userId);
			userRepresentation.setAttributes(new HashMap<>());
			users.add(userRepresentation);
		}

		when(usersResourceRest.search(anyString())).thenReturn(users);
		when(usersResourceRest.list()).thenReturn(users);
		when(usersResourceRest.list(any(), any())).thenReturn(users);
	}

	private UserRepresentation createIdpUser(String username, String firstName, String lastName, String email) {
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setUsername(username);
		userRepresentation.setFirstName(firstName);
		userRepresentation.setLastName(lastName);
		userRepresentation.setEmail(email);
		return userRepresentation;
	}

	private void withGroupsInIdp(List<String> groupIds) {
		List<GroupRepresentation> groups = new ArrayList<>();
		for (String groupId : groupIds) {
			GroupRepresentation groupRepresentation = new GroupRepresentation();
			groupRepresentation.setName(groupId);
			groupRepresentation.setId(groupId);
			groups.add(groupRepresentation);
		}

		when(groupsResourceRest.groups(anyString(), any(), any())).thenReturn(groups);
		when(groupsResourceRest.groups()).thenReturn(groups);
	}

	private GroupRepresentation createIdpGroup(String name) {
		GroupRepresentation groupRepresentation = new GroupRepresentation();
		groupRepresentation.setName(name);
		return groupRepresentation;
	}

	private void withLdapConfig(LdapMode mode) {
		when(ldapConfiguration.getLdapMode()).thenReturn(new ConfigurationPropertyMock<>(mode));
	}

	private static DefinitionMock createDefinitionMock() {
		DefinitionMock definition = new DefinitionMock();
		definition.getFields().add(createField(DefaultProperties.STATUS, DefaultProperties.NOT_USED_PROPERTY_VALUE));
		definition.getFields()
				.add(createField(DefaultProperties.SEMANTIC_TYPE, DefaultProperties.NOT_USED_PROPERTY_VALUE));
		definition.getFields().add(createField(ResourceProperties.FIRST_NAME, FIRST_NAME));
		definition.getFields().add(createField(ResourceProperties.LAST_NAME, LAST_NAME));
		definition.getFields().add(createField(ResourceProperties.EMAIL, EMAIL));
		definition.getFields().add(createField(ResourceProperties.IS_ACTIVE, IS_DISABLED));
		definition.getFields().add(createField("mobile", MOBILE));
		return definition;
	}

	private static DefinitionMock createDefinitionMock(List<Pair<String, String>> mappings) {
		DefinitionMock definition = new DefinitionMock();
		for (Pair<String, String> mapping : mappings) {
			definition.getFields().add(createField(mapping.getFirst(), mapping.getSecond()));
		}
		return definition;
	}

	private static PropertyDefinitionMock createField(String name, String dmsType) {
		PropertyDefinitionMock field = new PropertyDefinitionMock();
		field.setName(name);
		field.setDmsType(dmsType);
		return field;
	}

	private static Group buildGroup(String name) {
		return new EmfGroup(name, name);
	}

	private static String getUiUrl() {
		return UI_URL + "?" + ClientProperties.TENANT_MAPPER_NAME + "=" + TENANT_ID;
	}

}
