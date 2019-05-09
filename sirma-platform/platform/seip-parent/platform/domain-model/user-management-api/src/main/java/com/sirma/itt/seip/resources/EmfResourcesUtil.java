package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_ACTIVE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_DELETED;
import static com.sirma.itt.seip.resources.ResourceProperties.GROUP_PREFIX;
import static com.sirma.itt.seip.resources.ResourceProperties.USER_ID;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Utility class for helper method for {@link Resource} such as building a display name.
 *
 * @author BBonev
 */
public class EmfResourcesUtil {

	/**
	 * Instantiates a new emf resources util.
	 */
	private EmfResourcesUtil() {
		// utility class
	}

	/**
	 * Builds the display name from the given map using the keys from {@link ResourceProperties}
	 *
	 * @param properties the properties
	 * @return the string
	 */
	public static String buildDisplayName(Map<String, Serializable> properties) {
		String displayName;
		StringBuilder builder = new StringBuilder();
		String firstName = (String) properties.get(ResourceProperties.FIRST_NAME);
		String lastName = (String) properties.get(ResourceProperties.LAST_NAME);
		boolean hasLastName = StringUtils.isNotBlank(lastName) && !"null".equals(lastName);
		if (StringUtils.isNotBlank(firstName) && !"null".equals(firstName)) {
			builder.append(firstName);
			if (hasLastName) {
				builder.append(" ").append(lastName);
			}
		}
		if (builder.length() == 0) {
			// if we does not have a first name, but only last we will use it
			if (hasLastName) {
				displayName = lastName;
			} else {
				displayName = (String) properties.get(ResourceProperties.USER_ID);
			}
		} else {
			displayName = builder.toString();
		}
		return displayName;
	}

	/**
	 * Returns the group display name from given properties map which should belong to a group.
	 * First tries to fetch the {@link com.sirma.itt.seip.domain.instance.DefaultProperties#TITLE} if no value found for
	 * it the {@link ResourceProperties#GROUP_ID} will be returned as fallback.
	 *
	 * @param properties the properties map of a group
	 * @return the group display name
	 */
	public static String getGroupDisplayName(Map<String, Serializable> properties) {
		Serializable title = properties.get(DefaultProperties.TITLE);
		if (title != null) {
			return (String) title;
		}
		return (String) properties.get(ResourceProperties.GROUP_ID);
	}

	/**
	 * Removes group prefix from given group id. If the given group id does not have prefix or empty/null its returned.
	 *
	 * @param groupId id of a group in the system
	 * @return id of the group without group prefix
	 */
	public static String cleanGroupId(String groupId) {
		if (StringUtils.isNotBlank(groupId) && groupId.startsWith(ResourceProperties.GROUP_PREFIX)) {
			return groupId.substring(ResourceProperties.GROUP_PREFIX.length());
		}
		return groupId;
	}

	/**
	 * Extracts properties which have to be synchronized from the external user store and returns them. This method
	 * should be used in user/group synchronization. The returned properties are these that are explicitly defined as
	 * external in the model
	 *
	 * @param oldValue                old instance in the local db
	 * @param newValue                new instance from the external user store
	 * @param definitionModelProvider provider that matches an instance against definition model
	 * @return map with the properties synchronized from external user store
	 */
	public static Map<String, Serializable> extractExternalProperties(Instance oldValue, Instance newValue,
			Function<Instance, DefinitionModel> definitionModelProvider) {
		Map<String, Serializable> newProperties = null;
		DefinitionModel model = definitionModelProvider.apply(newValue);
		if (model != null) {
			Set<String> dmsEnabledFields = getDmsEnabledFields(model);
			// this will reset any properties that are externally synchronized
			oldValue.getOrCreateProperties().keySet().removeAll(dmsEnabledFields);
			oldValue.setIdentifier(model.getIdentifier());
			// necessary, because newValue is sealed
			newProperties = PropertiesUtil.cloneProperties(newValue.getOrCreateProperties());
			newProperties.keySet().retainAll(dmsEnabledFields);
		}
		return newProperties;
	}

	/**
	 * Returns the fields that are externally synchronized for users/groups. The dms type of the fields determine whether
	 * they are externally synchronized or not.
	 *
	 * @param model the definition model
	 * @return set of field ids which are externally synchronized
	 */
	private static Set<String> getDmsEnabledFields(DefinitionModel model) {
		return model.fieldsStream()
				.filter(PropertyDefinition.isObjectProperty().negate())
				.filter(PropertyDefinition.hasDmsType())
				.map(PropertyDefinition::getName)
				.filter(name -> !name.equalsIgnoreCase(USER_ID))
				.collect(Collectors.toSet());
	}

	/**
	 * Returns comparator which to be used in the user/group synchronization.
	 *
	 * @param definitionModelProvider provider that matches an instance against definition model
	 * @param copyFunc                function which to perform copy of the current instances if the given instance does not implement {@link GenericProxy}
	 * @return a predicate which to be used when comparing users/groups
	 */
	public static BiPredicate<Instance, Instance> getComparatorForSynchronization(
			Function<Instance, DefinitionModel> definitionModelProvider, Function<Instance, Instance> copyFunc) {
		return (r1, r2) -> {
			DefinitionModel r1Model = definitionModelProvider.apply(r1);
			if (r1Model == null) {
				// if we have missing models return that resources as equal so no synchronization to be done as we
				// cannot compare them properly
				return true;
			}

			Instance r1Copy = copy(r1, copyFunc);
			Instance r2Copy = copy(r2, copyFunc);

			Set<String> modelKeys = getDmsEnabledFields(r1Model);

			r1Copy.getOrCreateProperties().keySet().retainAll(modelKeys);
			r2Copy.getOrCreateProperties().keySet().retainAll(modelKeys);

			return r1Copy.getProperties().equals(r2Copy.getProperties());
		};
	}

	private static Instance copy(Instance instance, Function<Instance, Instance> copyFunc) {
		if (instance instanceof GenericProxy<?>) {
			return (Instance) ((GenericProxy<?>) instance).createCopy();
		}
		return copyFunc.apply(instance);
	}

	/**
	 * Creates Everyone (system all other users) group with populated instance id, name, title and type.
	 *
	 * @return everyone group as resource instance
	 */
	public static Resource createEveryoneGroup() {
		EmfGroup authority = new EmfGroup(ResourceService.EVERYONE_GROUP_ID,
				ResourceService.EVERYONE_GROUP_DISPLAY_NAME);
		authority.setType(ResourceType.GROUP);
		authority.setId(ResourceService.EVERYONE_GROUP_URI);
		authority.add(IS_DELETED, Boolean.FALSE);
		authority.add(IS_ACTIVE, Boolean.TRUE);
		authority.setType(InstanceType.create(EMF.GROUP.toString()));
		return authority;
	}

	/**
	 * Creates admin group with populated instance id, name, title and type.
	 *
	 * @return admin group as resource instance
	 */
	public static Resource createSystemAdminGroup() {
		EmfGroup authority = new EmfGroup(GROUP_PREFIX + ResourceService.SYSTEM_ADMIN_GROUP_ID,
				ResourceService.SYSTEM_ADMIN_GROUP_DISPLAY_NAME);
		authority.setType(ResourceType.GROUP);
		authority.setId(ResourceService.SYSTEM_ADMIN_GROUP_URI);
		authority.add(IS_DELETED, Boolean.FALSE);
		authority.add(IS_ACTIVE, Boolean.TRUE);
		authority.setType(InstanceType.create(EMF.GROUP.toString()));
		return authority;
	}

	/**
	 * Builds username from provided user id. The username is built with: userId@tenantId, where tenant is the current
	 * tenant id.
	 * <p>
	 * If there is already set tenant in the user id its checked if is valid, if not {@link IllegalArgumentException} is
	 * thrown.
	 *
	 * @param userId          {@link ResourceProperties#USER_ID} from user instance
	 * @param securityContext the current security context
	 * @return built username in format: userId@tenantId
	 */
	public static String buildUserName(String userId, SecurityContext securityContext) {
		String userIdCopy = userId;
		String tenantSuffix = "@" + securityContext.getCurrentTenantId();
		int tenantSeparatorIndex = userIdCopy.indexOf('@');
		if (securityContext.isDefaultTenant()) {
			if (userIdCopy.endsWith(tenantSuffix)) {
				// remove the tenant identifier if the tenant is default
				// it should not have a tenant id in it
				userIdCopy = userIdCopy.substring(0, userIdCopy.indexOf('@'));
			} else if (tenantSeparatorIndex != -1) {
				throw new IllegalArgumentException(
						"Cannot create user in another tenant: " + userIdCopy.substring(tenantSeparatorIndex));
			}
			// else no tenant id - all good
		} else if (tenantSeparatorIndex != -1) {
			if (!userIdCopy.substring(tenantSeparatorIndex).equals(tenantSuffix)) {
				throw new IllegalArgumentException(
						"Cannot create user in another tenant: " + userIdCopy.substring(tenantSeparatorIndex));
			}
		} else if (!userIdCopy.endsWith(tenantSuffix)) {
			userIdCopy = userIdCopy + tenantSuffix;
		}
		return userIdCopy;
	}

}
