package com.sirma.sep.keycloak.synchronization;

import static com.sirma.itt.seip.resources.ResourceProperties.GROUP_PREFIX;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfResourcesUtil;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.ReadOnlyStoreException;
import com.sirma.itt.seip.resources.RemoteStoreException;
import com.sirma.itt.seip.resources.RemoteUserStoreAdapter;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.keycloak.ClientProperties;
import com.sirma.sep.keycloak.ldap.LdapConfiguration;
import com.sirma.sep.keycloak.ldap.LdapMode;
import com.sirma.sep.keycloak.producers.KeycloakClientProducer;
import com.sirma.sep.keycloak.util.KeycloakApiUtil;

/**
 * Integration of Keycloak as user and group provider. The Keycloak's REST API java client is used for communication.
 * <p>
 * The keycloak rest api have endpoints for retrieving user by username and group by name, but on the keycloak end
 * it makes wildcard search like %userId% and as result returns all alike users and groups. To overcome this the logic
 * iterates through the returned result and checks for exact match. https://issues.jboss.org/browse/KEYCLOAK-2343
 * <p>
 * The everyone group does not exist in keycloak, because it stores unnecessary data to the underlying LDAP.
 * Instead its returned explicitly when needed.
 *
 * @author smustafov
 */
@Extension(target = RemoteUserStoreAdapter.NAME, order = 20)
public class KeycloakRemoteUserStoreAdapter implements RemoteUserStoreAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final EmfUser USER_INSTANCE = new EmfUser();

	/**
	 * Properties which are not included in the primary properties of remote user, but set as separate methods.
	 */
	private static final Set<String> SPECIAL_PROPERTIES = new HashSet<>(
			Arrays.asList(ResourceProperties.USER_ID, ResourceProperties.EMAIL, ResourceProperties.FIRST_NAME,
					ResourceProperties.LAST_NAME));

	static final String NAME = "keycloak";
	static final String IDP_ADMIN_GROUP = "admin";

	@Inject
	private KeycloakClientProducer keycloakClientProducer;

	@Inject
	private LdapConfiguration ldapConfiguration;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private InstanceTypes instanceTypes;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Override
	public boolean isExistingUser(String userId) throws RemoteStoreException {
		if (StringUtils.isBlank(userId)) {
			return false;
		}

		try {
			String userWithoutTenant = SecurityUtil.getUserWithoutTenant(userId);
			return getRemoteUser(userWithoutTenant) != null;
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public boolean isExistingGroup(String groupId) throws RemoteStoreException {
		if (StringUtils.isBlank(groupId)) {
			return false;
		}

		try {
			String cleanGroupId = cleanGroupId(groupId);
			if (ResourceService.EVERYONE_GROUP_ID.equals(cleanGroupId)) {
				// everyone always should exists
				return true;
			}

			return getRemoteGroup(cleanGroupId) != null;
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}
	}

	private static String cleanGroupId(String groupId) {
		String localGroupId = groupId;
		if (localGroupId.startsWith(GROUP_PREFIX)) {
			localGroupId = groupId.substring(GROUP_PREFIX.length());
		}
		if (ResourceService.SYSTEM_ADMIN_GROUP_ID.equals(localGroupId)) {
			localGroupId = IDP_ADMIN_GROUP;
		}
		return localGroupId;
	}

	@Override
	public Optional<User> getUserData(String userId) throws RemoteStoreException {
		if (StringUtils.isBlank(userId)) {
			return Optional.empty();
		}

		try {
			String userWithoutTenant = SecurityUtil.getUserWithoutTenant(userId);
			UserRepresentation remoteUser = getRemoteUser(userWithoutTenant);
			if (remoteUser != null) {
				Map<String, String> mappedUserProperties = getMappedUserProperties();
				return Optional.of(buildUser(remoteUser, mappedUserProperties));
			}
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}

		return Optional.empty();
	}

	private UserRepresentation getRemoteUser(String userId) {
		List<UserRepresentation> foundUsers = getUsersResource().search(userId);
		for (UserRepresentation foundUser : foundUsers) {
			if (foundUser.getUsername().equalsIgnoreCase(userId)) {
				return foundUser;
			}
		}
		return null;
	}

	@Override
	public Collection<User> getAllUsers() throws RemoteStoreException {
		try {
			List<UserRepresentation> users = getUsersResource().list(0, -1);
			if (CollectionUtils.isEmpty(users)) {
				LOGGER.warn("No users found returning empty list");
				return Collections.emptyList();
			}

			Map<String, String> mappedUserProperties = getMappedUserProperties();
			return users.stream().map(user -> buildUser(user, mappedUserProperties)).collect(Collectors.toList());
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}
	}

	private Map<String, String> getMappedUserProperties() {
		return definitionService.getInstanceDefinition(USER_INSTANCE).fieldsStream()
				.filter(PropertyDefinition.hasDmsType())
				.collect(toMap(PropertyDefinition::getName, PropertyDefinition::getDmsType));
	}

	private User buildUser(UserRepresentation remoteUserRepresentation, Map<String, String> mappedUserProperties) {
		String username = remoteUserRepresentation.getUsername();
		String fullUsername = SecurityUtil.buildTenantUserId(username, securityContext.getCurrentTenantId());

		Map<String, List<String>> remoteUserProperties = remoteUserRepresentation.getAttributes();
		Map<String, String> filteredProperties = filterMappedUserProperties(mappedUserProperties, remoteUserProperties);

		EmfUser user = new EmfUser(fullUsername);
		user.setType(ResourceType.USER);
		instanceTypes.from(EMF.USER.toString()).ifPresent(user::setType);

		filteredProperties.forEach((propertyName, propertyValue) -> {
			if (propertyName.equals(ResourceProperties.IS_ACTIVE)) {
				// inverse the value of mapped isActive property which comes from idp, because in ldap its
				// meant for disabled state and add it as boolean not as string to avoid unnecessary modifications
				user.add(propertyName, !Boolean.parseBoolean(propertyValue));
			} else {
				user.add(propertyName, propertyValue);
			}
		});

		// add explicitly these properties as they are not returned in the remote user properties map
		if (mappedUserProperties.containsKey(ResourceProperties.EMAIL) && StringUtils
				.isNotBlank(remoteUserRepresentation.getEmail())) {
			user.add(ResourceProperties.EMAIL, remoteUserRepresentation.getEmail());
		}
		if (mappedUserProperties.containsKey(ResourceProperties.FIRST_NAME) && StringUtils
				.isNotBlank(remoteUserRepresentation.getFirstName())) {
			user.add(ResourceProperties.FIRST_NAME, remoteUserRepresentation.getFirstName());
		}
		if (mappedUserProperties.containsKey(ResourceProperties.LAST_NAME) && StringUtils
				.isNotBlank(remoteUserRepresentation.getLastName())) {
			user.add(ResourceProperties.LAST_NAME, remoteUserRepresentation.getLastName());
		}

		return user;
	}

	private static Map<String, String> filterMappedUserProperties(Map<String, String> mappedUserProperties,
			Map<String, List<String>> remoteUserProperties) {
		return mappedUserProperties.entrySet().stream()
				.filter(entry -> remoteUserProperties.containsKey(entry.getValue()))
				.collect(toMap(Map.Entry::getKey, entry -> remoteUserProperties.get(entry.getValue()).get(0)));
	}

	@Override
	public Collection<Group> getAllGroups() throws RemoteStoreException {
		try {
			List<GroupRepresentation> groups = getGroupsResource().groups();
			if (CollectionUtils.isEmpty(groups)) {
				LOGGER.warn("No groups found returning empty list");
				return Collections.emptyList();
			}

			Group everyoneGroup = (Group) EmfResourcesUtil.createEveryoneGroup();
			Stream<Group> groupStream = groups.stream().map(this::buildGroup);

			return Stream.concat(groupStream, Stream.of(everyoneGroup)).collect(Collectors.toList());
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}
	}

	private Group buildGroup(GroupRepresentation groupRepresentation) {
		String groupId = groupRepresentation.getName();
		EmfGroup group;
		if (groupId.equals(IDP_ADMIN_GROUP)) {
			group = new EmfGroup(buildGroupId(ResourceService.SYSTEM_ADMIN_GROUP_ID),
					ResourceService.SYSTEM_ADMIN_GROUP_DISPLAY_NAME);
		} else {
			group = new EmfGroup(buildGroupId(groupId), groupId);
		}

		group.setType(ResourceType.GROUP);
		instanceTypes.from(EMF.GROUP.toString()).ifPresent(group::setType);
		return group;
	}

	private static String buildGroupId(String groupId) {
		return GROUP_PREFIX + groupId;
	}

	@Override
	public Collection<String> getUsersInGroup(String groupId) throws RemoteStoreException {
		if (StringUtils.isBlank(groupId)) {
			return Collections.emptyList();
		}

		try {
			String cleanGroupId = cleanGroupId(groupId);

			if (ResourceService.EVERYONE_GROUP_ID.equals(cleanGroupId)) {
				LOGGER.debug("Everyone group requested: {}", cleanGroupId);
				return mapRemoteUsersToUsername(getUsersResource().list(0, -1));
			}

			GroupRepresentation remoteGroup = getRemoteGroup(cleanGroupId);
			if (remoteGroup != null) {
				GroupResource groupResource = getGroupsResource().group(remoteGroup.getId());
				return mapRemoteUsersToUsername(groupResource.members(0, -1));
			}
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}

		return Collections.emptyList();
	}

	private GroupRepresentation getRemoteGroup(String groupId) {
		List<GroupRepresentation> foundGroups = getGroupsResource().groups(groupId, null, null);
		for (GroupRepresentation foundGroup : foundGroups) {
			if (foundGroup.getName().equals(groupId)) {
				return foundGroup;
			}
		}
		return null;
	}

	private Collection<String> mapRemoteUsersToUsername(List<UserRepresentation> users) {
		String currentTenantId = securityContext.getCurrentTenantId();
		return users.stream().map(user -> SecurityUtil.buildTenantUserId(user.getUsername(), currentTenantId))
				.collect(Collectors.toList());
	}

	@Override
	public Collection<String> getGroupsOfUser(String userId) throws RemoteStoreException {
		if (StringUtils.isBlank(userId)) {
			return Collections.emptyList();
		}

		try {
			String userWithoutTenant = SecurityUtil.getUserWithoutTenant(userId);

			UserRepresentation remoteUser = getRemoteUser(userWithoutTenant);
			if (remoteUser != null) {
				UserResource userResource = getUsersResource().get(remoteUser.getId());
				Stream<String> groupsStream = userResource.groups().stream()
						.map(this::buildGroup).map(Group::getName);

				return Stream.concat(groupsStream, Stream.of(ResourceService.EVERYONE_GROUP_ID))
						.collect(Collectors.toList());
			}
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}

		return Collections.emptyList();
	}

	@Override
	public String getUserClaim(String userId, String claimUri) throws RemoteStoreException {
		if (StringUtils.isBlank(userId) || StringUtils.isBlank(claimUri)) {
			return null;
		}

		try {
			String userWithoutTenant = SecurityUtil.getUserWithoutTenant(userId);

			UserRepresentation remoteUser = getRemoteUser(userWithoutTenant);
			if (remoteUser != null) {
				return getClaimValue(remoteUser, claimUri);
			}
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}

		return null;
	}

	private String getClaimValue(UserRepresentation userRepresentation, String claimUri) {
		Map<String, List<String>> remoteUserAttributes = userRepresentation.getAttributes();
		if (remoteUserAttributes.containsKey(claimUri)) {
			return remoteUserAttributes.get(claimUri).get(0);
		}

		if (UserClaims.USERNAME.equals(claimUri)) {
			return userRepresentation.getUsername();
		}
		if (UserClaims.FIRST_NAME.equals(claimUri)) {
			return userRepresentation.getFirstName();
		}
		if (UserClaims.LAST_NAME.equals(claimUri)) {
			return userRepresentation.getLastName();
		}
		if (UserClaims.EMAIL.equals(claimUri)) {
			return userRepresentation.getEmail();
		}
		return null;
	}

	@Override
	public Map<String, String> getUserClaims(String userId, String... claimUris) throws RemoteStoreException {
		if (StringUtils.isBlank(userId) || claimUris == null || claimUris.length == 0) {
			return Collections.emptyMap();
		}

		List<String> processedClaims = Arrays.stream(claimUris).filter(StringUtils::isNotBlank)
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(processedClaims)) {
			return emptyMap();
		}

		try {
			String userWithoutTenant = SecurityUtil.getUserWithoutTenant(userId);

			UserRepresentation remoteUser = getRemoteUser(userWithoutTenant);
			if (remoteUser != null) {
				Map<String, String> userClaims = new HashMap<>(processedClaims.size());
				for (String processedClaim : processedClaims) {
					userClaims.put(processedClaim, getClaimValue(remoteUser, processedClaim));
				}
				return userClaims;
			}
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}

		return Collections.emptyMap();
	}

	@Override
	public void createUser(User user) throws RemoteStoreException {
		if (isReadOnly()) {
			throw new ReadOnlyStoreException("Cannot create user as the underlying user store is readonly");
		}

		try {
			UserRepresentation remoteUser = buildRemoteUser(user);

			// create user
			UsersResource usersResource = getUsersResource();
			Response response = usersResource.create(remoteUser);

			// send mail for updating password
			sendUpdatePasswordEmail(usersResource, response, user.getName(), securityContext.getCurrentTenantId());

			LOGGER.info("Created new user: {}", user.getName());
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}
	}

	private UserRepresentation buildRemoteUser(User user) {
		UserRepresentation remoteUser = new UserRepresentation();
		remoteUser.setEnabled(true);
		remoteUser.setUsername(SecurityUtil.getUserWithoutTenant(user.getName()));
		remoteUser.setEmail(user.getAsString(ResourceProperties.EMAIL));
		remoteUser.setFirstName(user.getAsString(ResourceProperties.FIRST_NAME));
		remoteUser.setLastName(user.getAsString(ResourceProperties.LAST_NAME));
		remoteUser.setAttributes(buildRemoteUserProperties(user.getProperties(), new HashMap<>()));
		return remoteUser;
	}

	private Map<String, List<String>> buildRemoteUserProperties(Map<String, Serializable> userProperties,
			Map<String, List<String>> remoteUserProperties) {
		Map<String, String> mappedUserProperties = getMappedUserProperties();

		userProperties.entrySet().stream()
				.filter(entry -> !SPECIAL_PROPERTIES.contains(entry.getKey()) && entry.getValue() != null)
				.filter(entry -> mappedUserProperties.containsKey(entry.getKey()))
				.forEach(entry -> {
					String propertyValue = entry.getValue().toString();
					if (ResourceProperties.IS_ACTIVE.equals(entry.getKey())) {
						// inverse the value of mapped isActive property which comes from idp, because in ldap its
						// meant for disabled state
						propertyValue = Boolean.toString(!Boolean.parseBoolean(propertyValue));
					}
					remoteUserProperties
							.put(mappedUserProperties.get(entry.getKey()), Collections.singletonList(propertyValue));
		});

		return remoteUserProperties;
	}

	private void sendUpdatePasswordEmail(UsersResource usersResource, Response response, String username,
			String tenantId) {
		String uiClientUrl = getUiClientUrl(tenantId);

		String remoteUserId = KeycloakApiUtil.getCreatedId(response);
		usersResource.get(remoteUserId).executeActionsEmail(ClientProperties.SEP_UI_CLIENT_ID, uiClientUrl,
				Collections.singletonList(UserActions.UPDATE_PASSWORD.toString()));

		LOGGER.debug("Sent email for updating user password for: {}", username);
	}

	private String getUiClientUrl(String tenantId) {
		String ui2Url = systemConfiguration.getUi2Url().requireConfigured().get();
		return ui2Url + "?" + ClientProperties.TENANT_MAPPER_NAME + "=" + tenantId;
	}

	@Override
	public void updateUser(User user) throws RemoteStoreException {
		if (isReadOnly()) {
			throw new ReadOnlyStoreException("Cannot update user as the underlying user store is readonly");
		}

		try {
			UserRepresentation remoteUser = getRemoteUser(SecurityUtil.getUserWithoutTenant(user.getName()));
			if (remoteUser != null) {
				Map<String, Serializable> clonedProperties = PropertiesUtil.cloneProperties(user.getProperties());
				Map<String, List<String>> remoteUserProperties = buildRemoteUserProperties(clonedProperties, remoteUser.getAttributes());

				// this controls if the user can login in keycloak
				remoteUser.setEnabled(user.isActive());

				remoteUser.setEmail(user.getAsString(ResourceProperties.EMAIL));
				remoteUser.setFirstName(user.getAsString(ResourceProperties.FIRST_NAME));
				remoteUser.setLastName(user.getAsString(ResourceProperties.LAST_NAME));
				remoteUser.setAttributes(remoteUserProperties);

				UserResource userResource = getUsersResource().get(remoteUser.getId());
				userResource.update(remoteUser);

				removeUserSessions(userResource, remoteUser.isEnabled());

				LOGGER.info("Updated user: {}, is active: {}", user.getName(), user.isActive());
			}
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}
	}

	private void removeUserSessions(UserResource userResource, boolean enabled) {
		if (!enabled) {
			// if user deactivated - remove all user sessions associated with the user
			userResource.logout();
		}
	}

	@Override
	public void deleteUser(String userId) throws RemoteStoreException {
		if (isReadOnly()) {
			throw new ReadOnlyStoreException("Cannot delete user as the underlying user store is readonly");
		}

		if (StringUtils.isBlank(userId)) {
			LOGGER.warn("Empty id passed for deleting user. Skipping it.");
			return;
		}

		try {
			UserRepresentation remoteUser = getRemoteUser(SecurityUtil.getUserWithoutTenant(userId));
			if (remoteUser != null) {
				getUsersResource().get(remoteUser.getId()).remove();
				LOGGER.info("Deleted {} from keycloak", userId);
			}
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public void createGroup(Group group) throws RemoteStoreException {
		if (isReadOnly()) {
			throw new ReadOnlyStoreException("Cannot create group as the underlying user store is readonly");
		}

		try {
			String cleanGroupId = cleanGroupId(group.getName());
			if (ResourceService.EVERYONE_GROUP_ID.equals(cleanGroupId)) {
				return;
			}

			GroupRepresentation groupRepresentation = new GroupRepresentation();
			groupRepresentation.setName(cleanGroupId);

			getGroupsResource().add(groupRepresentation);
			LOGGER.info("Created new group with id: {} and SEP id: {}", cleanGroupId, group.getName());
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public void deleteGroup(String groupId) throws RemoteStoreException {
		if (isReadOnly()) {
			throw new ReadOnlyStoreException("Cannot delete group as the underlying user store is readonly");
		}

		if (StringUtils.isBlank(groupId)) {
			LOGGER.warn("Empty id passed for deleting group. Skipping it.");
			return;
		}

		try {
			String cleanGroupId = cleanGroupId(groupId);
			if (ResourceService.EVERYONE_GROUP_ID.equals(cleanGroupId)) {
				return;
			}

			GroupRepresentation remoteGroup = getRemoteGroup(cleanGroupId);
			if (remoteGroup != null) {
				getGroupsResource().group(remoteGroup.getId()).remove();
				LOGGER.info("Deleted group: {}", groupId);
			}
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public void updateGroupsOfUser(String userId, List<String> groupsToRemove, List<String> groupsToAdd)
			throws RemoteStoreException {
		if (isReadOnly()) {
			throw new ReadOnlyStoreException("Cannot update group as the underlying user store is readonly");
		}

		if (StringUtils.isBlank(userId)) {
			LOGGER.warn("Empty id passed for updating groups of user. Skipping it.");
			return;
		}

		try {
			UserRepresentation remoteUser = getRemoteUser(SecurityUtil.getUserWithoutTenant(userId));
			if (remoteUser != null) {
				UserResource userResource = getUsersResource().get(remoteUser.getId());

				groupsToRemove.stream()
						.map(KeycloakRemoteUserStoreAdapter::cleanGroupId)
						.map(this::getRemoteGroup)
						.filter(Objects::nonNull)
						.forEach(remoteGroup -> {
							userResource.leaveGroup(remoteGroup.getId());
							LOGGER.debug("Removed user: {} from group: {}", remoteUser.getUsername(), remoteGroup.getName());
						});

				groupsToAdd.stream()
						.map(KeycloakRemoteUserStoreAdapter::cleanGroupId)
						.map(this::getRemoteGroup)
						.filter(Objects::nonNull)
						.forEach(remoteGroup -> {
							userResource.joinGroup(remoteGroup.getId());
							LOGGER.debug("Added user: {} to group: {}", remoteUser.getUsername(), remoteGroup.getName());
						});
			}
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public void updateUsersInGroup(String groupId, List<String> usersToRemove, List<String> usersToAdd)
			throws RemoteStoreException {
		if (isReadOnly()) {
			throw new ReadOnlyStoreException("Cannot update group as the underlying user store is readonly");
		}

		if (StringUtils.isBlank(groupId)) {
			LOGGER.warn("Empty id passed for updating users in group. Skipping it.");
			return;
		}

		try {
			GroupRepresentation remoteGroup = getRemoteGroup(cleanGroupId(groupId));
			if (remoteGroup != null) {
				// currently keycloak does not support batch updates
				// https://issues.jboss.org/browse/KEYCLOAK-1413
				UsersResource usersResource = getUsersResource();

				usersToRemove.stream()
						.map(SecurityUtil::getUserWithoutTenant)
						.map(this::getRemoteUser)
						.filter(Objects::nonNull)
						.forEach(remoteUser -> {
							UserResource userResource = usersResource.get(remoteUser.getId());
							userResource.leaveGroup(remoteGroup.getId());
							LOGGER.debug("Removed user: {} from group: {}", remoteUser.getUsername(), remoteGroup.getName());
						});

				usersToAdd.stream()
						.map(SecurityUtil::getUserWithoutTenant)
						.map(this::getRemoteUser)
						.filter(Objects::nonNull)
						.forEach(remoteUser -> {
							UserResource userResource = usersResource.get(remoteUser.getId());
							userResource.joinGroup(remoteGroup.getId());
							LOGGER.debug("Added user: {} to group: {}", remoteUser.getUsername(), remoteGroup.getName());
						});
			}
		} catch (WebApplicationException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public boolean isReadOnly() throws RemoteStoreException {
		return LdapMode.READ_ONLY.equals(ldapConfiguration.getLdapMode().get());
	}

	@Override
	public boolean isGroupInGroupSupported() {
		return true;
	}

	@Override
	public String getName() {
		return NAME;
	}

	private UsersResource getUsersResource() {
		return keycloakClientProducer.produceUsersResource();
	}

	private GroupsResource getGroupsResource() {
		return keycloakClientProducer.produceGroupsResource();
	}

}
