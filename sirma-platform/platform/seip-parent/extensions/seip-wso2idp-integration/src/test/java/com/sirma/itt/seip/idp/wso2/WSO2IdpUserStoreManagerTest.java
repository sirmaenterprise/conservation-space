package com.sirma.itt.seip.idp.wso2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryService;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.ReadOnlyStoreException;
import com.sirma.itt.seip.resources.RemoteStoreException;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Test for {@link WSO2IdpUserStoreManager}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 03/08/2017
 */
public class WSO2IdpUserStoreManagerTest {

	private static final String USER_NAME_CLAIM = "urn:scim:schemas:core:1.0:userName";
	private static final String GIVEN_NAME_CLAIM = "urn:scim:schemas:core:1.0:name.givenName";
	private static final String EMAIL_CLAIM = "urn:scim:schemas:core:1.0:emails";
	@InjectMocks
	private WSO2IdpUserStoreManager userStoreManager;

	@Spy
	private ConfigurationPropertyMock<Boolean> isWriteEnabled = new ConfigurationPropertyMock<>(Boolean.TRUE);

	@Mock
	private UserStoreManager userStoreManagerInstance;
	@Mock
	private ClaimManager claimManager;
	@Mock
	private UserInformationRecoveryService informationRecoveryService;

	@Mock
	private DefinitionService definitionService;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private Contextual<Boolean> initializedContextClaims;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		ReflectionUtils.setFieldValue(userStoreManager, "userStoreManager",
				new InstanceProxyMock<>(userStoreManagerInstance));
		ReflectionUtils.setFieldValue(userStoreManager, "claimManager", new InstanceProxyMock<>(claimManager));
		ReflectionUtils.setFieldValue(userStoreManager, "informationRecoveryService",
				new InstanceProxyMock<>(informationRecoveryService));

		when(securityContext.getCurrentTenantId()).thenReturn("tenant.com");
		when(initializedContextClaims.getContextValue()).thenReturn(Boolean.TRUE);

		DefinitionMock definition = createDefinitionMock();
		when(definitionService.getInstanceDefinition(any(EmfUser.class))).thenReturn(definition);
	}

	private static DefinitionMock createDefinitionMock() {
		DefinitionMock definition = new DefinitionMock();
		definition.getFields().add(createField("status", DefaultProperties.NOT_USED_PROPERTY_VALUE));
		definition.getFields().add(createField(ResourceProperties.USER_ID, USER_NAME_CLAIM));
		definition.getFields().add(createField("firstName", GIVEN_NAME_CLAIM));
		definition.getFields().add(createField(ResourceProperties.EMAIL, EMAIL_CLAIM));
		definition.getFields().add(createField("rdf:type", DefaultProperties.NOT_USED_PROPERTY_VALUE));
		return definition;
	}

	private static PropertyDefinitionMock createField(String name, String dmsType) {
		PropertyDefinitionMock field = new PropertyDefinitionMock();
		field.setName(name);
		field.setDmsType(dmsType);
		return field;
	}

	@Test
	public void isExistingUser_shouldRemoveTenantOnCheck() throws Exception {
		when(userStoreManagerInstance.isExistingUser("user1")).thenReturn(Boolean.TRUE);

		assertTrue(userStoreManager.isExistingUser("user1"));
		assertTrue(userStoreManager.isExistingUser("user1@tenant.com"));
	}

	@Test(expected = RemoteStoreException.class)
	public void isExistingUser_shouldThrowExceptionOnError() throws Exception {
		when(userStoreManagerInstance.isExistingUser(anyString())).thenThrow(new UserStoreException());

		userStoreManager.isExistingUser("user1");
	}

	@Test
	public void isExistingGroup_shouldRemoveGroupPrefix() throws Exception {
		when(userStoreManagerInstance.isExistingRole("Test group")).thenReturn(Boolean.TRUE);

		assertTrue(userStoreManager.isExistingGroup("Test group"));
		assertTrue(userStoreManager.isExistingGroup("GROUP_Test group"));
	}

	@Test
	public void isExistingGroup_shouldConvertAdminGroupIdToIdpAdminGroup() throws Exception {
		when(userStoreManagerInstance.isExistingRole("admin")).thenReturn(Boolean.TRUE);

		assertTrue(userStoreManager.isExistingGroup("admin"));
		assertTrue(userStoreManager.isExistingGroup(ResourceService.SYSTEM_ADMIN_GROUP_ID));
	}

	@Test(expected = RemoteStoreException.class)
	public void isExistingGroup_shouldThrowExceptionOnError() throws Exception {
		when(userStoreManagerInstance.isExistingRole(anyString())).thenThrow(new UserStoreException());

		userStoreManager.isExistingGroup("user1");
	}

	@Test
	public void getUserData_EmptyOptionalForMissingUser() throws Exception {
		assertFalse(userStoreManager.getUserData("user1").isPresent());
		verify(userStoreManagerInstance).isExistingUser(anyString());
	}

	@Test
	public void getUserData_getUserProperties() throws Exception {
		when(userStoreManagerInstance.isExistingUser("user1")).thenReturn(Boolean.TRUE);
		Map<String, String> claims = new HashMap<>();
		claims.put(USER_NAME_CLAIM, "user1");
		claims.put(EMAIL_CLAIM, "user1@tenant.com");
		when(userStoreManagerInstance.getUserClaimValues(eq("user1"), any(String[].class), any())).thenReturn(claims);

		Optional<User> userData = userStoreManager.getUserData("user1@test.com");
		assertTrue("User data should be present", userData.isPresent());
		User user = userData.get();

		assertEquals("user1@tenant.com", user.getName());
		assertEquals("user1@tenant.com", user.getEmail());
		assertEquals("user1@tenant.com", user.get(ResourceProperties.USER_ID));
	}

	@Test(expected = RemoteStoreException.class)
	public void getUserData_shouldThrowExceptionOnError() throws Exception {
		when(userStoreManagerInstance.isExistingUser("user1")).thenReturn(Boolean.TRUE);
		when(userStoreManagerInstance.getUserClaimValues(eq("user1"), any(String[].class), any()))
				.thenThrow(new UserStoreException());

		userStoreManager.getUserData("user1@test.com");
	}

	@Test
	public void getUserData_ShouldInverseValueOfIsActiveClaim() throws Exception {
		when(userStoreManagerInstance.isExistingUser("user1")).thenReturn(Boolean.TRUE);

		Map<String, String> claims = new HashMap<>();
		claims.put(WSO2IdpUserStoreManager.ACCOUNT_DISABLED_CLAIM_URI, "false");
		when(userStoreManagerInstance.getUserClaimValues(eq("user1"), any(String[].class), any())).thenReturn(claims);

		DefinitionMock definition = createDefinitionMock();
		definition.getFields()
				.add(createField(DefaultProperties.IS_ACTIVE, WSO2IdpUserStoreManager.ACCOUNT_DISABLED_CLAIM_URI));
		when(definitionService.getInstanceDefinition(any(EmfUser.class))).thenReturn(definition);

		Optional<User> userData = userStoreManager.getUserData("user1@test.com");
		assertTrue("User data should be present", userData.isPresent());
		User user = userData.get();
		assertEquals("true", user.get(DefaultProperties.IS_ACTIVE));

		claims.put(WSO2IdpUserStoreManager.ACCOUNT_DISABLED_CLAIM_URI, "true");
		when(userStoreManagerInstance.getUserClaimValues(eq("user1"), any(String[].class), any())).thenReturn(claims);

		userData = userStoreManager.getUserData("user1@test.com");
		assertTrue("User data should be present", userData.isPresent());
		user = userData.get();
		assertEquals("false", user.get(DefaultProperties.IS_ACTIVE));
	}

	@Test
	public void getUserData_shouldReturn_UserIdInOriginalCaseSenitivity() throws Exception {
		when(userStoreManagerInstance.isExistingUser("johnlegend")).thenReturn(Boolean.TRUE);

		Map<String, String> claims = new HashMap<>();
		claims.put(USER_NAME_CLAIM, "johnLegend");
		claims.put(EMAIL_CLAIM, "johnLegend@tenant.com");
		when(userStoreManagerInstance.getUserClaimValues(eq("johnlegend"), any(String[].class), any()))
				.thenReturn(claims);

		Optional<User> userData = userStoreManager.getUserData("johnlegend@tenant.com");
		assertTrue("User data should be present", userData.isPresent());
		User user = userData.get();

		assertEquals("johnLegend@tenant.com", user.getName());
		assertEquals("johnLegend@tenant.com", user.getEmail());
		assertEquals("johnLegend@tenant.com", user.get(ResourceProperties.USER_ID));
	}

	@Test
	public void getAllUsers_shouldFetchAllUserProperties() throws Exception {

		when(userStoreManagerInstance.listUsers("*", -1)).thenReturn(new String[] { "user1", "user2", "user3" });

		when(userStoreManagerInstance.getUserClaimValues(anyString(), any(String[].class), any())).then(
				this::buildUserClaimsMock);

		Collection<User> allUsers = userStoreManager.getAllUsers();
		assertEquals(3, allUsers.size());

		Iterator<User> iterator = allUsers.iterator();

		User user = iterator.next();
		assertEquals("user1@tenant.com", user.getName());
		assertEquals("user1@test.com", user.getEmail());
		assertEquals("user1@tenant.com", user.get(ResourceProperties.USER_ID));
		assertEquals("Given_user1", user.get("firstName"));

		user = iterator.next();
		assertEquals("user2@tenant.com", user.getName());
		assertEquals("user2@test.com", user.getEmail());
		assertEquals("user2@tenant.com", user.get(ResourceProperties.USER_ID));

		user = iterator.next();
		assertEquals("user3@tenant.com", user.getName());
		assertEquals("user3@test.com", user.getEmail());
		assertEquals("user3@tenant.com", user.get(ResourceProperties.USER_ID));
	}

	@Test(expected = RemoteStoreException.class)
	public void getAllUsers_shouldThrowErrorIfCannotFetchSomeOfTheUserData() throws Exception {

		when(userStoreManagerInstance.listUsers("*", -1)).thenReturn(new String[] { "user1", "user2", "user3" });

		when(userStoreManagerInstance.getUserClaimValues(eq("user1"), any(String[].class), any())).then(
				this::buildUserClaimsMock);
		when(userStoreManagerInstance.getUserClaimValues(eq("user2"), any(String[].class), any()))
				.thenThrow(new UserStoreException());

		when(userStoreManagerInstance.getUserClaimValues(eq("user3"), any(String[].class), any())).then(
				this::buildUserClaimsMock);

		userStoreManager.getAllUsers();
	}

	private Object buildUserClaimsMock(InvocationOnMock a) {
		String userName = a.getArgumentAt(0, String.class);
		Map<String, String> claims = new HashMap<>();
		claims.put(USER_NAME_CLAIM, userName);
		claims.put(EMAIL_CLAIM, userName + "@test.com");
		claims.put(GIVEN_NAME_CLAIM, "Given_" + userName);
		return claims;
	}

	@Test
	public void getAllGroups() throws Exception {
		when(userStoreManagerInstance.getRoleNames()).thenReturn(new String[] { "group1", "group2" });

		Collection<Group> allGroups = userStoreManager.getAllGroups();

		assertEquals(2, allGroups.size());
		Iterator<Group> iterator = allGroups.iterator();

		Group group = iterator.next();
		assertEquals("GROUP_group1", group.getName());
		assertEquals("group1", group.getDisplayName());
		group = iterator.next();
		assertEquals("GROUP_group2", group.getName());
		assertEquals("group2", group.getDisplayName());
	}

	@Test(expected = RemoteStoreException.class)
	public void getAllGroups_shouldThrowExceptionOnFetchError() throws Exception {
		when(userStoreManagerInstance.getRoleNames()).thenThrow(new UserStoreException());

		userStoreManager.getAllGroups();
	}

	@Test
	public void getAllGroups_shouldSkipInternalGroups() throws Exception {
		when(userStoreManagerInstance.getRoleNames())
				.thenReturn(new String[] { "Consumers", "Internal/everyone", "Contributors", "Internal/identity" });

		Collection<Group> groups = userStoreManager.getAllGroups();
		assertEquals(3, groups.size());
	}

	@Test
	public void getAllGroups_shouldReturnInternalEveryoneGroup() throws Exception {
		when(userStoreManagerInstance.getRoleNames())
				.thenReturn(new String[] { "Internal/everyone", "Internal/identity", "Internal/carbon" });

		Collection<Group> groups = userStoreManager.getAllGroups();
		assertEquals(1, groups.size());

		Group group = groups.iterator().next();
		assertEquals(ResourceService.EVERYONE_GROUP_ID, group.getName());
		assertEquals(ResourceService.EVERYONE_GROUP_DISPLAY_NAME, group.getDisplayName());
		assertEquals(ResourceService.EVERYONE_GROUP_URI, group.getId());
	}

	@Test
	public void getAllGroups_shouldConvertIdpAdminGroupIdToSystemAdminGroupId() throws Exception {
		when(userStoreManagerInstance.getRoleNames())
				.thenReturn(new String[] { "Internal/identity", "admin" });

		Collection<Group> groups = userStoreManager.getAllGroups();
		assertEquals("GROUP_" + ResourceService.SYSTEM_ADMIN_GROUP_ID, groups.iterator().next().getName());
	}

	@Test
	public void getUsersInGroup_shouldIgnoreInvalidGroupId() throws Exception {
		assertTrue(userStoreManager.getUsersInGroup(null).isEmpty());
		assertTrue(userStoreManager.getUsersInGroup("").isEmpty());
	}

	@Test
	public void getUsersInGroup() throws Exception {
		when(userStoreManagerInstance.getUserListOfRole("group1")).thenReturn(new String[] { "user1", "user2" });

		Collection<String> usersInGroup = userStoreManager.getUsersInGroup("GROUP_group1");
		assertEquals(2, usersInGroup.size());
		assertTrue(usersInGroup.contains("user1@tenant.com"));
		assertTrue(usersInGroup.contains("user2@tenant.com"));
	}

	@Test(expected = RemoteStoreException.class)
	public void getUsersInGroup_shouldReturnNothingOnRemoteException() throws Exception {
		when(userStoreManagerInstance.getUserListOfRole("group1")).thenThrow(new UserStoreException());

		userStoreManager.getUsersInGroup("GROUP_group1");
	}

	@Test
	public void getGroupsOfUser() throws Exception {
		when(userStoreManagerInstance.getRoleListOfUser("user1")).thenReturn(new String[] { "group1", "group2" });

		Collection<String> usersInGroup = userStoreManager.getGroupsOfUser("user1@test.com");
		assertEquals(2, usersInGroup.size());
		assertTrue(usersInGroup.contains("GROUP_group1"));
		assertTrue(usersInGroup.contains("GROUP_group2"));
	}

	@Test(expected = RemoteStoreException.class)
	public void getGroupsOfUser_shouldThrowExceptionOnRemoteException() throws Exception {
		when(userStoreManagerInstance.getRoleListOfUser("user1")).thenThrow(new UserStoreException());

		userStoreManager.getGroupsOfUser("user1@test.com");
	}

	@Test
	public void getUserClaim() throws Exception {
		when(userStoreManagerInstance.getUserClaimValue("user1", USER_NAME_CLAIM, null)).thenReturn("user1Name");

		assertEquals("user1Name", userStoreManager.getUserClaim("user1@test.com", USER_NAME_CLAIM));
	}

	@Test(expected = RemoteStoreException.class)
	public void getUserClaim_shouldThrowExceptionOnError() throws Exception {
		when(userStoreManagerInstance.getUserClaimValue("user1", USER_NAME_CLAIM, null))
				.thenThrow(new UserStoreException());

		userStoreManager.getUserClaim("user1@test.com", USER_NAME_CLAIM);
	}

	@Test
	public void getUserClaim_shouldReturnNullOnInvalidParams() throws Exception {
		assertNull(userStoreManager.getUserClaim(null, USER_NAME_CLAIM));
		assertNull(userStoreManager.getUserClaim("", USER_NAME_CLAIM));
		assertNull(userStoreManager.getUserClaim("", null));
		assertNull(userStoreManager.getUserClaim(null, null));
		assertNull(userStoreManager.getUserClaim("user1", null));
		assertNull(userStoreManager.getUserClaim("user1", ""));
	}

	@Test
	public void getUserClaims_shouldReturnEmptyMapOnInvalidParams() throws Exception {
		assertTrue(userStoreManager.getUserClaims(null).isEmpty());
		assertTrue(userStoreManager.getUserClaims("").isEmpty());
		assertTrue(userStoreManager.getUserClaims("user1").isEmpty());
		assertTrue(userStoreManager.getUserClaims("user1", (String[]) null).isEmpty());
		assertTrue(userStoreManager.getUserClaims("user1", "").isEmpty());
		assertTrue(userStoreManager.getUserClaims("user1", "", "", "").isEmpty());
	}

	@Test(expected = RemoteStoreException.class)
	public void getUserClaims_shouldThrowExceptionOnError() throws Exception {
		when(userStoreManagerInstance.getUserClaimValues(anyString(), any(String[].class), any()))
				.thenThrow(new UserStoreException());

		userStoreManager.getUserClaims("user", USER_NAME_CLAIM).isEmpty();
	}

	@Test
	public void getUserClaims() throws Exception {
		Map<String, String> claims = new HashMap<>();
		claims.put(USER_NAME_CLAIM, "user1");
		claims.put(EMAIL_CLAIM, "user1@tenant.com");
		when(userStoreManagerInstance.getUserClaimValues(eq("user1"), any(String[].class), eq(null)))
				.thenReturn(claims);

		Map<String, String> userClaims = userStoreManager.getUserClaims("user1@test.com", USER_NAME_CLAIM, EMAIL_CLAIM,
				GIVEN_NAME_CLAIM);

		assertFalse(userClaims.isEmpty());
		assertEquals("user1", userClaims.get(USER_NAME_CLAIM));
		assertEquals("user1@tenant.com", userClaims.get(EMAIL_CLAIM));
		assertNull(userClaims.get(GIVEN_NAME_CLAIM));
	}

	@Test(expected = RemoteStoreException.class)
	public void isReadOnly_shouldThrowExceptionIfErrorOccurredDuringCheck() throws Exception {
		when(userStoreManagerInstance.isReadOnly()).thenThrow(new UserStoreException());

		assertTrue(userStoreManager.isReadOnly());
	}

	@Test
	public void isReadOnly_shouldBeTrueIfWriteIsPossibleButNotAllowed() throws Exception {
		when(userStoreManagerInstance.isReadOnly()).thenReturn(false);
		isWriteEnabled.setValue(Boolean.FALSE);

		assertTrue(userStoreManager.isReadOnly());
	}

	@Test
	public void isReadOnly_shouldBeTrueIfWriteIsNotPossibleButAllowed() throws Exception {
		when(userStoreManagerInstance.isReadOnly()).thenReturn(true);
		isWriteEnabled.setValue(Boolean.TRUE);

		assertTrue(userStoreManager.isReadOnly());
	}

	@Test
	public void isReadOnly_shouldBeTrueIfWriteIsNotPossibleAndNotAllowed() throws Exception {
		when(userStoreManagerInstance.isReadOnly()).thenReturn(true);
		isWriteEnabled.setValue(Boolean.FALSE);

		assertTrue(userStoreManager.isReadOnly());
	}

	@Test
	public void isReadOnly_shouldBeFalseIfWriteIsPossibleAndAllowed() throws Exception {
		when(userStoreManagerInstance.isReadOnly()).thenReturn(false);
		isWriteEnabled.setValue(Boolean.TRUE);

		assertFalse(userStoreManager.isReadOnly());
	}

	@Test
	public void isGroupInGroupSupported() throws Exception {
		assertFalse(userStoreManager.isGroupInGroupSupported());
	}

	@Test(expected = ReadOnlyStoreException.class)
	public void createUser_shouldFailIfStoreIsReadOnly() throws Exception {
		when(userStoreManagerInstance.isReadOnly()).thenReturn(true);
		isWriteEnabled.setValue(Boolean.TRUE);

		userStoreManager.createUser(new EmfUser("test"));
	}

	@Test
	public void createUser_shouldCreateUser() throws Exception {
		EmfUser user = new EmfUser("test@tenant.com");
		user.add("firstName", "Test");
		userStoreManager.createUser(user);

		verify(informationRecoveryService).registerUser(eq("test"), eq(""), any(), eq("default"), eq("tenant.com"));
	}

	@Test(expected = RemoteStoreException.class)
	public void createUser_shouldFailIfFailToRegisterUser() throws Exception {
		when(informationRecoveryService.registerUser(any(), any(), any(), eq("default"), any())).thenThrow(
				new RemoteException());

		userStoreManager.createUser(new EmfUser("test"));
	}

	@Test
	public void deleteUser_shouldDeleteTheUserFromIDP() throws Exception {
		userStoreManager.deleteUser("test@tenant.com");
		verify(userStoreManagerInstance).deleteUser("test");
	}

	@Test(expected = RemoteStoreException.class)
	public void deleteUser_shouldFailIfCannotDeleteTheUser() throws Exception {
		doThrow(UserStoreException.class).when(userStoreManagerInstance).deleteUser("test");
		userStoreManager.deleteUser("test@tenant.com");
	}

	@Test(expected = ReadOnlyStoreException.class)
	public void deleteUser_shouldFailIfTheStoreIsReadOnly() throws Exception {
		when(userStoreManagerInstance.isReadOnly()).thenReturn(true);
		userStoreManager.deleteUser("test@tenant.com");
	}

	@Test(expected = ReadOnlyStoreException.class)
	public void updateUser_ShouldFailIfTheStoreIsReadOnly() throws Exception {
		when(userStoreManagerInstance.isReadOnly()).thenReturn(true);
		userStoreManager.updateUser(new EmfUser());
	}

	@Test(expected = RemoteStoreException.class)
	public void updateUser_ShouldFail_When_IdpCommunicationErrorOccurs() throws Exception {
		doThrow(UserStoreException.class).when(userStoreManagerInstance).setUserClaimValues(anyString(), any(Map.class),
				any());

		userStoreManager.updateUser(new EmfUser("user@tenant.com"));
	}

	@Test
	public void updateUser_ShouldUpdateClaims() throws Exception {
		String email = "user@mail.com";
		String firstName = "John";
		EmfUser user = new EmfUser("user@tenant.com");
		user.add(ResourceProperties.EMAIL, email);
		user.add(ResourceProperties.FIRST_NAME, firstName);

		when(userStoreManagerInstance.getUserClaimValues(anyString(), any(String[].class), any()))
				.then(this::buildUserClaimsMock);

		userStoreManager.updateUser(user);

		ArgumentCaptor<Map<String, String>> argCaptor = ArgumentCaptor.forClass(Map.class);

		verify(userStoreManagerInstance).setUserClaimValues(eq("user"), argCaptor.capture(), eq(null));
		Map<String, String> claims = argCaptor.getValue();
		assertEquals(2, claims.size());
		assertEquals(email, claims.get(EMAIL_CLAIM));
		assertEquals(firstName, claims.get(GIVEN_NAME_CLAIM));
	}

	@Test
	public void updateUser_ShouldIncludeIsActiveAsMappedClaim_When_NotMappedInDefinition() throws Exception {
		EmfUser user = new EmfUser("user@tenant.com");
		user.add(DefaultProperties.IS_ACTIVE, Boolean.TRUE);

		when(userStoreManagerInstance.getUserClaimValues(anyString(), any(String[].class), any()))
				.then(this::buildUserClaimsMock);

		userStoreManager.updateUser(user);

		ArgumentCaptor<Map<String, String>> argCaptor = ArgumentCaptor.forClass(Map.class);

		verify(userStoreManagerInstance).setUserClaimValues(eq("user"), argCaptor.capture(), eq(null));
		Map<String, String> claims = argCaptor.getValue();
		assertEquals(Boolean.toString(false), claims.get(WSO2IdpUserStoreManager.ACCOUNT_DISABLED_CLAIM_URI));
	}

	@Test
	public void updateUser_ShouldDoNothing_When_IsActiveMappedInDefinition() throws Exception {
		EmfUser user = new EmfUser("user@tenant.com");
		user.add(DefaultProperties.IS_ACTIVE, Boolean.FALSE);

		DefinitionMock definition = createDefinitionMock();
		definition.getFields()
				.add(createField(DefaultProperties.IS_ACTIVE, WSO2IdpUserStoreManager.ACCOUNT_DISABLED_CLAIM_URI));
		when(definitionService.getInstanceDefinition(any(EmfUser.class))).thenReturn(definition);

		when(userStoreManagerInstance.getUserClaimValues(anyString(), any(String[].class), any()))
				.then(this::buildUserClaimsMock);

		userStoreManager.updateUser(user);

		ArgumentCaptor<Map<String, String>> argCaptor = ArgumentCaptor.forClass(Map.class);

		verify(userStoreManagerInstance).setUserClaimValues(eq("user"), argCaptor.capture(), eq(null));
		Map<String, String> claims = argCaptor.getValue();
		assertEquals(Boolean.toString(true), claims.get(WSO2IdpUserStoreManager.ACCOUNT_DISABLED_CLAIM_URI));
	}

	@Test(expected = ReadOnlyStoreException.class)
	public void updateGroupsOfUser_shouldFailForReadOnlyStore() throws Exception {
		when(userStoreManagerInstance.isReadOnly()).thenReturn(true);
		userStoreManager.updateGroupsOfUser("user", Collections.emptyList(), Collections.emptyList());
	}

	@Test
	public void updateGroupsOfUser_shouldUpdateTheGroupsInTheRemoteStore() throws Exception {
		userStoreManager.updateGroupsOfUser("user@tenant.com", Arrays.asList("GROUP_to_remove_1", "to_remove_2"),
				Arrays.asList("GROUP_to_add_1", "to_add_2"));

		verify(userStoreManagerInstance).updateRoleListOfUser("user", new String[] { "to_remove_1", "to_remove_2" },
				new String[] { "to_add_1", "to_add_2" });
	}

	@Test(expected = ReadOnlyStoreException.class)
	public void updateUsersInGroup_shouldFailForReadOnlyStore() throws Exception {
		when(userStoreManagerInstance.isReadOnly()).thenReturn(true);
		userStoreManager.updateUsersInGroup("GROUP_test", Collections.emptyList(), Collections.emptyList());
	}

	@Test
	public void updateUsersInGroup_shouldDoNothingIfGroupDoesNotExists() throws Exception {
		when(userStoreManagerInstance.isExistingRole("test")).thenReturn(Boolean.FALSE);

		userStoreManager.updateUsersInGroup("GROUP_test", Arrays.asList("user_1@tenant.com", "user_2@tenant.com"),
				Collections.emptyList());

		verify(userStoreManagerInstance, never()).updateUserListOfRole(eq("test"), any(), any());
	}

	@Test
	public void updateUsersInGroup_shouldUpdateTheUsersInTheRemoteStore() throws Exception {
		userStoreManager.updateUsersInGroup("GROUP_test", Arrays.asList("user_1@tenant.com", "user_2@tenant.com"),
				Arrays.asList("user_3@tenant.com", "user_4@tenant.com"));

		verify(userStoreManagerInstance).updateUserListOfRole("test", new String[] { "user_1", "user_2" },
				new String[] { "user_3", "user_4" });
	}

	@Test
	public void init_shouldInsertClaimMappings() throws Exception {
		doAnswer(invocation -> {
			Supplier<Boolean> supplier = invocation.getArgumentAt(0, Supplier.class);
			supplier.get();
			return null;
		}).when(initializedContextClaims).initializeWith(any(Supplier.class));

		userStoreManager.init();

		ArgumentCaptor<ClaimMapping> captor = ArgumentCaptor.forClass(ClaimMapping.class);
		verify(claimManager, times(2)).addNewClaimMapping(captor.capture());

		List<ClaimMapping> allValues = captor.getAllValues();
		assertEquals(2, allValues.size());

		ClaimMapping passwordMapping = allValues.get(0);
		Claim passwordClaim = passwordMapping.getClaim();
		assertExpectedClaimMapping(passwordMapping, passwordClaim, "http://wso2.org/claims/identity/passwordTimestamp",
				"facsimileTelephoneNumber");

		ClaimMapping accountDisabledMapping = allValues.get(1);
		Claim accountDisabledClaim = accountDisabledMapping.getClaim();
		assertExpectedClaimMapping(accountDisabledMapping, accountDisabledClaim,
				"http://wso2.org/claims/identity/accountDisabled", "postalCode");
	}

	private static void assertExpectedClaimMapping(ClaimMapping mapping, Claim claim, String expectedClaimUri,
			String expectedMappedAttribute) {
		assertEquals(expectedMappedAttribute, mapping.getMappedAttribute());
		assertEquals(expectedClaimUri, claim.getClaimUri());
		assertEquals(WSO2IdpUserStoreManager.WSO2_ORG_CLAIMS_URI_DIALECT, claim.getDialectURI());
	}

	@Test
	public void getName_shouldReturnWso2Idp() {
		assertEquals("wso2Idp", userStoreManager.getName());
	}

}
