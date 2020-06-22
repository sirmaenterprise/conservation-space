package com.sirma.sep.instance.operation.executor;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.ALL_OTHERS;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.INHERIT;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * Utility containing common logic used in instance executors, mainly related to permissions extraction from JSON object
 * and populating specific context with them.
 *
 * @author A. Kunchev
 */
class InstanceExecutorUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Extracts permissions from the given {@link JSONObject} request.
	 *
	 * @param data
	 *            JsonObject from request
	 * @param mapKey
	 *            String Key
	 * @param resourceService
	 *            the resource service
	 * @param roleService
	 *            the role service
	 * @return Map<emf:username,RoleIdentifier> map that contains a mapping between emf user and its role identifier
	 */
	public static Map<String, RoleIdentifier> extractPermissions(JSONObject data, String mapKey,
			ResourceService resourceService, RoleService roleService) {
		JSONObject jsonPermissions = JsonUtil.getJsonObject(data, mapKey);
		if (jsonPermissions == null || !jsonPermissions.keys().hasNext()) {
			return emptyMap();
		}

		Map<String, RoleIdentifier> permissions = new HashMap<>(jsonPermissions.length());
		for (Iterator<?> iterator = jsonPermissions.keys(); iterator.hasNext();) {
			String key = iterator.next().toString();
			try {
				Object value = jsonPermissions.get(key);
				if (value instanceof JSONObject || value instanceof JSONArray) {
					// some complex properties
					LOGGER.warn("Complex permissions not supported, yet. Skipping key-value pair {}={}", key, value);
				} else {
					populetePermissionsForResource(resourceService, roleService, permissions, key, value);
				}
			} catch (JSONException e) {
				LOGGER.debug("Failed to get permission {}", key, e);
			}
		}

		return permissions;
	}

	private static void populetePermissionsForResource(ResourceService resourceService, RoleService roleService,
			Map<String, RoleIdentifier> permissions, String key, Object value) {
		Resource resource = resourceService.findResource(key);
		RoleIdentifier roleIdentifier = roleService.getRoleIdentifier(value.toString());
		if (resource != null && roleIdentifier != null) {
			permissions.put(resource.getId().toString(), roleIdentifier);
		} else {
			LOGGER.warn("Not found user/group=role. Skipping key-value pair {}={}", key, value);
		}
	}

	/**
	 * Extracts inherited and all others permissions and populates the passed context with them. If those permissions
	 * are not set in the passed JSON, the method does nothing.
	 *
	 * @param data
	 *            the JSON object from which the permissions should be retrieved
	 * @param context
	 *            in which the permissions should be stored
	 * @param roleService
	 *            for retrieving the role identifiers for the permissions
	 */
	public static void extractAdditionalPermissions(JSONObject data, OperationContext context,
			RoleService roleService) {
		Object inherit = JsonUtil.getValueOrNull(data, INHERIT);
		if (inherit instanceof String) {
			Boolean inherited = Boolean.valueOf((String) inherit);
			context.put(INHERIT, inherited);
		}

		Object allOther = JsonUtil.getValueOrNull(data, ALL_OTHERS);
		if (allOther instanceof String) {
			context.put(ALL_OTHERS, roleService.getRoleIdentifier((String) allOther));
		}
	}

	private InstanceExecutorUtils() {
		// utility
	}
}
