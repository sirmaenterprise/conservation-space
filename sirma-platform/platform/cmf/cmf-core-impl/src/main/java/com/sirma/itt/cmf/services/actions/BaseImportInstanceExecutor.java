package com.sirma.itt.cmf.services.actions;

import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.ALL_OTHERS;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.CTX_TARGET;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.DMS_ID;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.INHERIT;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.PERMISSIONS;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.actions.OperationResponse;
import com.sirma.itt.seip.instance.actions.OperationStatus;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleService;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * Base class to implement the import operations.
 *
 * @author BBonev
 */
public abstract class BaseImportInstanceExecutor extends BaseInstanceExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private RoleService roleService;

	@Override
	public OperationContext parseRequest(JSONObject data) {
		OperationContext context = super.parseRequest(data);
		context.put(DMS_ID, JsonUtil.getStringValue(data, DMS_ID));
		// mark the class as non persisted - we are just importing it
		InstanceReference reference = context.getIfSameType(CTX_TARGET, InstanceReference.class);
		idManager.registerId(reference.getIdentifier());
		context.put(PERMISSIONS, (Serializable) extractPermissions(data, PERMISSIONS, resourceService, roleService));
		Object inherit = JsonUtil.getValueOrNull(data, INHERIT);
		if (inherit instanceof String) {
			Boolean inherited = Boolean.valueOf((String) inherit);
			context.put(INHERIT, inherited);
		}
		Object allOther = JsonUtil.getValueOrNull(data, ALL_OTHERS);
		if (allOther instanceof String) {
			context.put(ALL_OTHERS, roleService.getRoleIdentifier((String) allOther));
		}
		return context;
	}

	@Override
	public OperationResponse execute(OperationContext context) {
		Instance instance = getOrCreateInstance(context);
		if (instance instanceof DMSInstance) {
			String dmsId = context.getIfSameType(DMS_ID, String.class);
			((DMSInstance) instance).setDmsId(dmsId);
		}
		instance.getProperties().put(DefaultProperties.IS_IMPORTED, Boolean.TRUE);
		beforeSave(instance, context);

		Options.DO_NOT_CALL_DMS.enable();
		try {
			Instance savedInstance = getInstanceService().save(instance, new Operation(getOperation()));
			return new OperationResponse(OperationStatus.COMPLETED, toJson(savedInstance));
		} finally {
			Options.DO_NOT_CALL_DMS.disable();
		}
	}

	/**
	 * Method called before save of the instance after has been created.
	 *
	 * @param instance
	 *            the instance
	 * @param context
	 *            the context
	 */
	protected void beforeSave(Instance instance, OperationContext context) {
		// nothing to do here
	}

	@Override
	public boolean rollback(OperationContext data) {
		// nothing to rollback
		return true;
	}

	/**
	 * Extract permissions from the given {@link JSONObject} request.
	 *
	 * @param data
	 *            JsonObject from request
	 * @param mapKey
	 *            String Key
	 * @param resourceService
	 *            the resource service
	 * @param roleService
	 *            the role service
	 * @return Map<emf:username,RoleIdentifier>
	 */
	public static Map<String, RoleIdentifier> extractPermissions(JSONObject data, String mapKey,
			ResourceService resourceService, RoleService roleService) {
		Map<String, RoleIdentifier> permissions = new HashMap<>(0);
		JSONObject jsonPermissions = JsonUtil.getJsonObject(data, mapKey);
		if (jsonPermissions != null) {
			permissions = new HashMap<>(jsonPermissions.length());
			for (Iterator<?> iterator = jsonPermissions.keys(); iterator.hasNext();) {
				String key = iterator.next().toString();
				try {
					Object value = jsonPermissions.get(key);
					if (value instanceof JSONObject || value instanceof JSONArray) {
						// some complex properties
						LOGGER.warn("Complex permissions not supported, yet. Skipping key-value pair {}={}", key,
								value);
					} else {
						Resource resource = resourceService.findResource(key);
						RoleIdentifier roleIdentifier = roleService.getRoleIdentifier(value.toString());
						if (resource != null && roleIdentifier != null) {
							permissions.put(resource.getId().toString(), roleIdentifier);
						} else {
							LOGGER.warn("Not found user/group=role. Skipping key-value pair {}={}", key, value);
						}
					}
				} catch (JSONException e) {
					LOGGER.debug("Failed to get permission {}", key, e);
				}
			}
		}
		return permissions;
	}

}
