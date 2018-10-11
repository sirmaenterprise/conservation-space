package com.sirma.itt.seip.idp.wso2;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.collections.CollectionUtils.toArray;
import static com.sirma.itt.seip.resources.ResourceProperties.GROUP_PREFIX;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryService;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.ReadOnlyStoreException;
import com.sirma.itt.seip.resources.RemoteStoreException;
import com.sirma.itt.seip.resources.RemoteUserStoreAdapter;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * Integration of WSO2Idp server as User and Group provider. The implementation uses webservice protocol for
 * communication with the IDP server using an instance of {@link UserStoreManager}
 *
 * @author smustafov
 * @author BBonev
 */
@Extension(target = RemoteUserStoreAdapter.NAME, order = 10)
public class WSO2IdpUserStoreManager implements RemoteUserStoreAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final EmfUser USER_INSTANCE = new EmfUser();
	private static final String IDP_ADMIN_GROUP_ID = "admin";
	private static final String IDP_EVERYONE_GROUP_ID = "Internal/everyone";

	public static final String WSO2_ORG_CLAIMS_URI_DIALECT = "http://wso2.org/claims";
	public static final String ACCOUNT_DISABLED_CLAIM_URI = "http://wso2.org/claims/identity/accountDisabled";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "security.identity.server.allowWriteOperations", type = Boolean.class,
			defaultValue = "true", sensitive = true,
			label = "Enables or disables writing to the remote user store if the store itself supports write operations.")
	private ConfigurationProperty<Boolean> writeAllowed;

	@Inject
	private Instance<UserStoreManager> userStoreManager;
	@Inject
	private Instance<ClaimManager> claimManager;
	@Inject
	private Instance<UserInformationRecoveryService> informationRecoveryService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private Contextual<Boolean> initializedContextClaims;

	@PostConstruct
	void init() {
		initializedContextClaims.initializeWith(this::initClaims);
	}

	private boolean initClaims() {
		try {
			insertRequiredClaimConfigurations();
			return true;
		} catch (RemoteStoreException e) {
			LOGGER.error("Cannot insert required claims in IDP", e);
		}
		return false;
	}

	private void ensureClaimsInitializedForCurrentContext() {
		if (!initializedContextClaims.getContextValue()) {
			initializedContextClaims.reset();
		}
	}

	@Override
	public boolean isExistingUser(String userId) throws RemoteStoreException {
		if (StringUtils.isBlank(userId)) {
			return false;
		}

		try {
			return userStoreManager.get().isExistingUser(SecurityUtil.getUserWithoutTenant(userId));
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public boolean isExistingGroup(String groupId) throws RemoteStoreException {
		if (StringUtils.isBlank(groupId)) {
			return false;
		}

		try {
			return userStoreManager.get().isExistingRole(cleanGroupId(groupId));
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	private static String cleanGroupId(String groupId) {
		String localGroupId = groupId;
		if (groupId.startsWith(GROUP_PREFIX)) {
			localGroupId = groupId.substring(GROUP_PREFIX.length());
		}
		if (localGroupId.equals(ResourceService.SYSTEM_ADMIN_GROUP_ID)) {
			localGroupId = IDP_ADMIN_GROUP_ID;
		} else if (localGroupId.equals(ResourceService.EVERYONE_GROUP_ID)) {
			localGroupId = IDP_EVERYONE_GROUP_ID;
		}
		return localGroupId;
	}

	@Override
	public Optional<User> getUserData(String userId) throws RemoteStoreException {
		if (StringUtils.isBlank(userId)) {
			return Optional.empty();
		}

		UserStoreManager storeManager = userStoreManager.get();
		try {
			String userName = SecurityUtil.getUserWithoutTenant(userId);
			if (storeManager.isExistingUser(userName)) {
				Map<String, String> nameToClaimMapping = getSupportedUserClaims();
				return Optional.of(buildUser(userName, nameToClaimMapping, storeManager));
			}
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
		return Optional.empty();
	}

	private User buildUser(String userId, Map<String, String> nameToClaimMapping, UserStoreManager storeManager)
			throws UserStoreException {
		ensureClaimsInitializedForCurrentContext();

		if (!nameToClaimMapping.isEmpty()) {
			Map<String, String> userClaims;
			Collection<String> userClaimIds = nameToClaimMapping.values();
			try {
				userClaims = storeManager.getUserClaimValues(userId, toArray(userClaimIds, String.class), null);
			} catch (UserStoreException e) {
				LOGGER.warn("Failed to retrieve {} user properties {} from remote store. ", userId, userClaimIds);
				throw e;
			}

			Map<String, String> convertedProperties = convertClaimsToProperties(nameToClaimMapping, userClaims);

			// init with user id from the claims, because method's userId param can be in lowercase when importing new
			// user, but we want to keep original case of the id
			User user = new EmfUser(SecurityUtil.buildTenantUserId(
					convertedProperties.get(ResourceProperties.USER_ID), securityContext.getCurrentTenantId()));

			// add all converted properties except the user id
			// the IDP user id format is without the tenant suffix that breaks the user identifier
			convertedProperties.entrySet()
					.stream()
					.filter(e -> !ResourceProperties.USER_ID.equals(e.getKey()))
					.forEach(e -> {
						String propertyValue = e.getValue();
						if (e.getKey().equals(DefaultProperties.IS_ACTIVE)) {
							// inverse the value of mapped isActive property which comes from idp, because in idp its
							// meant for disabled state
							propertyValue = Boolean.toString(!Boolean.parseBoolean(propertyValue));
						}
						user.add(e.getKey(), propertyValue);
					});

			return user;
		}

		return null;
	}

	private static Map<String, String> convertClaimsToProperties(
			Map<String, String> nameToClaimMapping, Map<String, String> userClaims) {
		return nameToClaimMapping.entrySet().stream()
				.filter(entry -> userClaims.containsKey(entry.getValue()))
				.collect(toMap(Map.Entry::getKey, entry -> userClaims.get(entry.getValue())));
	}

	private Map<String, String> getSupportedUserClaims() {
		return definitionService.getInstanceDefinition(USER_INSTANCE)
				.fieldsStream()
				.filter(PropertyDefinition.hasDmsType())
				.collect(toMap(PropertyDefinition::getName, PropertyDefinition::getDmsType));
	}

	@Override
	public Collection<User> getAllUsers() throws RemoteStoreException {
		try {
			UserStoreManager storeManager = userStoreManager.get();
			String[] usersIds = storeManager.listUsers("*", -1);
			if (usersIds == null) {
				return emptyList();
			}

			Map<String, String> supportedUserClaims = getSupportedUserClaims();
			List<UserStoreException> suppressed = new LinkedList<>();
			List<User> users = Arrays
					.stream(usersIds)
						.map(userId -> loadUser(userId, supportedUserClaims, storeManager, suppressed))
						.filter(Objects::nonNull)
						.collect(toList());

			if (isNotEmpty(suppressed)) {
				RemoteStoreException exception = new RemoteStoreException("Failed collecting user properties");
				suppressed.forEach(exception::addSuppressed);
				throw exception;
			}

			return users;
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	private User loadUser(String userId, Map<String, String> supportedUserClaims, UserStoreManager storeManager,
			List<UserStoreException> suppressed) {
		try {
			return buildUser(userId, supportedUserClaims, storeManager);
		} catch (UserStoreException e) {
			suppressed.add(e);
			return null;
		}
	}

	@Override
	public Collection<Group> getAllGroups() throws RemoteStoreException {
		try {
			String[] groupIds = userStoreManager.get().getRoleNames();
			if (groupIds == null) {
				return emptyList();
			}

			return Arrays
					.stream(groupIds)
						.filter(skipInternalGroups())
						.map(WSO2IdpUserStoreManager::buildGroup)
						.collect(toList());
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	private static Predicate<String> skipInternalGroups() {
		return groupId -> groupId.equals(IDP_EVERYONE_GROUP_ID) || !groupId.startsWith("Internal/");
	}

	private static EmfGroup buildGroup(String groupId) {
		if (groupId.equals(IDP_ADMIN_GROUP_ID)) {
			return new EmfGroup(buildGroupId(ResourceService.SYSTEM_ADMIN_GROUP_ID),
					ResourceService.SYSTEM_ADMIN_GROUP_DISPLAY_NAME);
		}
		if (groupId.equals(IDP_EVERYONE_GROUP_ID)) {
			// everyone has no group prefix and uses security namespace for uri
			EmfGroup everyone = new EmfGroup(ResourceService.EVERYONE_GROUP_ID,
					ResourceService.EVERYONE_GROUP_DISPLAY_NAME);
			everyone.setId(ResourceService.EVERYONE_GROUP_URI);
			return everyone;
		}
		return new EmfGroup(buildGroupId(groupId), groupId);
	}

	private static String buildGroupId(String groupId) {
		return GROUP_PREFIX + groupId;
	}

	@Override
	public Collection<String> getUsersInGroup(String groupId) throws RemoteStoreException {
		if (StringUtils.isBlank(groupId)) {
			return emptyList();
		}

		try {
			String[] usersIds = userStoreManager.get().getUserListOfRole(cleanGroupId(groupId));
			if (usersIds == null) {
				return emptyList();
			}

			String tenantId = securityContext.getCurrentTenantId();
			return Arrays
					.stream(usersIds)
						.map(userId -> SecurityUtil.buildTenantUserId(userId, tenantId))
						.collect(toList());
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public Collection<String> getGroupsOfUser(String userId) throws RemoteStoreException {
		if (StringUtils.isBlank(userId)) {
			return emptyList();
		}

		try {
			String[] groupIds = userStoreManager.get().getRoleListOfUser(SecurityUtil.getUserWithoutTenant(userId));
			if (groupIds == null) {
				return emptyList();
			}

			return Arrays
					.stream(groupIds)
						.filter(skipInternalGroups())
						.map(WSO2IdpUserStoreManager::buildGroup)
						.map(Group::getName)
						.collect(toList());
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public String getUserClaim(String userId, String claimUri) throws RemoteStoreException {
		if (StringUtils.isBlank(userId) || StringUtils.isBlank(claimUri)) {
			return null;
		}

		try {
			return userStoreManager.get().getUserClaimValue(SecurityUtil.getUserWithoutTenant(userId), claimUri, null);
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public Map<String, String> getUserClaims(String userId, String... claimUris) throws RemoteStoreException {
		if (StringUtils.isBlank(userId) || claimUris == null || claimUris.length == 0) {
			return emptyMap();
		}
		String[] checkedClaims = Arrays.stream(claimUris).filter(StringUtils::isNotBlank).toArray(String[]::new);
		if (checkedClaims.length == 0) {
			return emptyMap();
		}

		try {
			return userStoreManager.get()
					.getUserClaimValues(SecurityUtil.getUserWithoutTenant(userId), checkedClaims, null);
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public void createUser(User user) throws RemoteStoreException {
		UserStoreManager storeManager = userStoreManager.get();
		if (isReadOnlyInternal(storeManager)) {
			throw new ReadOnlyStoreException("Cannot create user as the store changes are not allowed or not possible");
		}
		try {
			ensureClaimsInitializedForCurrentContext();

			UserIdentityClaimDTO[] claims = getUserClaims(user.getProperties());
			String userName = SecurityUtil.getUserWithoutTenant(user.getName());
			String tenantId = SecurityUtil.getUserAndTenant(user.getName()).getSecond();

			informationRecoveryService.get().registerUser(userName, "", claims, "default", tenantId);

			LOGGER.info("Created new user {} in WSO2 IDP", user.getName());
		} catch (RemoteException | UserInformationRecoveryServiceIdentityMgtServiceExceptionException e) {
			throw new RemoteStoreException("Creation of user [" + user.getIdentityId() + "] failed in remote store", e);
		}
	}

	private void insertRequiredClaimConfigurations() throws RemoteStoreException {
		try {
			// we may add a check if the claim is not there and not to perform insert twice
			// but the IDP sends broken data when trying to read any claim values
			// and the add methods seams not to cause any problem if it's added more than once
			// https://wso2.org/jira/browse/IDENTITY-2861

			// insert required claim configuration that is used by the IDP for storing the password timestamp used when
			// temporary password is enabled
			ClaimMapping passwordClaimMapping = createClaimMappingForPassword();

			// claim used for storing if the user is active or not
			ClaimMapping disabledUserClaimMapping = createClaimMappingForDisabledUser();

			ClaimManager localClaimManager = claimManager.get();
			localClaimManager.addNewClaimMapping(passwordClaimMapping);
			localClaimManager.addNewClaimMapping(disabledUserClaimMapping);
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	private static ClaimMapping createClaimMappingForPassword() {
		ClaimMapping mapping = new ClaimMapping();
		mapping.setMappedAttribute("facsimileTelephoneNumber");
		Claim claim = new Claim();
		claim.setClaimUri("http://wso2.org/claims/identity/passwordTimestamp");
		claim.setDescription("Identity Password timestamp");
		claim.setDisplayTag("Identity Password timestamp");
		claim.setDialectURI(WSO2_ORG_CLAIMS_URI_DIALECT);
		mapping.setClaim(claim);
		return mapping;
	}

	private static ClaimMapping createClaimMappingForDisabledUser() {
		ClaimMapping mapping = new ClaimMapping();
		// in newer versions of IDP, its mapped to ref attribute, but with current version it breaks the users in
		// the ldap if its mapped to ref
		// https://docs.wso2.com/display/IS530/Account+Disabling
		mapping.setMappedAttribute("postalCode");
		Claim claim = new Claim();
		claim.setClaimUri(ACCOUNT_DISABLED_CLAIM_URI);
		claim.setDescription("Account Disabled");
		claim.setDisplayTag("Account Disabled");
		claim.setDialectURI(WSO2_ORG_CLAIMS_URI_DIALECT);
		mapping.setClaim(claim);
		return mapping;
	}

	private UserIdentityClaimDTO[] getUserClaims(Map<String, Serializable> properties) {
		Map<String, String> supportedUserClaims = getSupportedUserClaims();
		if (!supportedUserClaims.containsKey(DefaultProperties.IS_ACTIVE)) {
			supportedUserClaims.put(DefaultProperties.IS_ACTIVE, ACCOUNT_DISABLED_CLAIM_URI);
		}
		return properties.entrySet().stream()
				.filter(entry -> entry.getValue() != null)
				.filter(entry -> supportedUserClaims.containsKey(entry.getKey()))
				.map(entry -> {
					UserIdentityClaimDTO claimDTO = new UserIdentityClaimDTO();
					claimDTO.setClaimUri(supportedUserClaims.get(entry.getKey()));

					String claimValue = entry.getValue().toString();
					if (entry.getKey().equals(DefaultProperties.IS_ACTIVE)) {
						// inverse the value for isActive property, because the mapped claim in idp is for disabled user
						claimValue = Boolean.toString(!Boolean.parseBoolean(claimValue));
					}

					claimDTO.setClaimValue(claimValue);
					return claimDTO;
				}).toArray(UserIdentityClaimDTO[]::new);
	}

	@Override
	public void updateUser(User user) throws RemoteStoreException {
		UserStoreManager storeManager = userStoreManager.get();
		if (isReadOnlyInternal(storeManager)) {
			throw new ReadOnlyStoreException("Cannot update user as the store changes are not allowed or not possible");
		}

		try {
			ensureClaimsInitializedForCurrentContext();

			String userName = SecurityUtil.getUserWithoutTenant(user.getName());
			Map<String, Serializable> clonedProperties = PropertiesUtil.cloneProperties(user.getProperties());

			// remove the user id from properties, because idp throws exception when trying to update it
			clonedProperties.remove(ResourceProperties.USER_ID);

			UserIdentityClaimDTO[] claims = getUserClaims(clonedProperties);
			Map<String, String> claimsMap = new HashMap<>(claims.length);

			for (UserIdentityClaimDTO userIdentityClaimDTO : claims) {
				claimsMap.put(userIdentityClaimDTO.getClaimUri(), userIdentityClaimDTO.getClaimValue());
			}

			storeManager.setUserClaimValues(userName, claimsMap, null);
		} catch (UserStoreException e) {
			throw new RemoteStoreException("Update of user [" + user.getIdentityId() + "] failed in remote store", e);
		}
	}

	@Override
	public void deleteUser(String userId) throws RemoteStoreException {
		UserStoreManager storeManager = userStoreManager.get();
		if (isReadOnlyInternal(storeManager)) {
			throw new ReadOnlyStoreException("Cannot delete user as the store changes are not allowed or not possible");
		}
		try {
			storeManager.deleteUser(SecurityUtil.getUserWithoutTenant(userId));
			LOGGER.info("Deleted user {} from WS2 IDP", userId);
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public void updateGroupsOfUser(String userId, List<String> groupsToRemove, List<String> groupsToAdd) throws
			RemoteStoreException {
		UserStoreManager storeManager = this.userStoreManager.get();
		if (isReadOnlyInternal(storeManager)) {
			throw new ReadOnlyStoreException("Cannot change user group assignment as the store changes are not allowed or not possible");
		}

		try {
			String username = SecurityUtil.getUserWithoutTenant(userId);
			String[] toRemove = groupsToRemove.stream()
					.map(WSO2IdpUserStoreManager::cleanGroupId)
					.toArray(String[]::new);
			String[] toAdd = groupsToAdd.stream()
					.map(WSO2IdpUserStoreManager::cleanGroupId)
					.toArray(String[]::new);

			storeManager.updateRoleListOfUser(username, toRemove, toAdd);
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public void updateUsersInGroup(String groupId, List<String> usersToRemove, List<String> usersToAdd) throws
			RemoteStoreException {
		UserStoreManager storeManager = this.userStoreManager.get();
		if (isReadOnlyInternal(storeManager)) {
			throw new ReadOnlyStoreException("Cannot change group assignments as the store changes are not allowed or not possible");
		}

		try {
			String idpGroupId = cleanGroupId(groupId);
			if (!storeManager.isExistingRole(idpGroupId)) {
				// if we want to add users, just create the group
				// otherwise exit as the group is probably deleted and we are here to remove the existing members of
				// the group
				if (isNotEmpty(usersToAdd)) {
					LOGGER.warn("Trying to add group members to non existent group. The group '{}' will be created first", idpGroupId);
					createGroupInternal(groupId, storeManager);
				} else {
					LOGGER.warn("Tried to update group members to non existent group: {}", groupId);
					return;
				}
			}

			String[] toRemove = usersToRemove.stream()
					.map(SecurityUtil::getUserWithoutTenant)
					.toArray(String[]::new);
			String[] toAdd = usersToAdd.stream()
					.map(SecurityUtil::getUserWithoutTenant)
					.toArray(String[]::new);

			storeManager.updateUserListOfRole(idpGroupId, toRemove, toAdd);
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public void deleteGroup(String groupId) throws RemoteStoreException {
		if (StringUtils.isBlank(groupId)) {
			return;
		}

		UserStoreManager storeManager = userStoreManager.get();
		if (isReadOnlyInternal(storeManager)) {
			throw new ReadOnlyStoreException("Cannot delete group as the store changes are not allowed or not possible");
		}

		try {
			storeManager.deleteRole(cleanGroupId(groupId));
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public void createGroup(Group group) throws RemoteStoreException {
		UserStoreManager storeManager = userStoreManager.get();
		if (isReadOnlyInternal(storeManager)) {
			throw new ReadOnlyStoreException("Cannot create group as the store changes are not allowed or not possible");
		}

		createGroupInternal(group.getName(), storeManager);
	}

	private void createGroupInternal(String groupName, UserStoreManager storeManager) throws RemoteStoreException {
		try {
			storeManager.addRole(cleanGroupId(groupName), new String[0], new Permission[0]);
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public boolean isReadOnly() throws RemoteStoreException {
		return isReadOnlyInternal(userStoreManager.get());
	}

	private boolean isReadOnlyInternal(UserStoreManager storeManager) throws RemoteStoreException {
		try {
			boolean isWritePossible = !storeManager.isReadOnly();
			boolean isWriteAllowed = writeAllowed.get();
			return !isWritePossible || !isWriteAllowed;
		} catch (UserStoreException e) {
			throw new RemoteStoreException(e);
		}
	}

	@Override
	public boolean isGroupInGroupSupported() {
		return false;
	}

	@Override
	public String getName() {
		return "wso2Idp";
	}
}
