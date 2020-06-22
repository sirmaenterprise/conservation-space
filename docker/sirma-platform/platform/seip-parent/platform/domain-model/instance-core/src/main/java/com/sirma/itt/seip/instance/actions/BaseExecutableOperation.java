package com.sirma.itt.seip.instance.actions;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Basic implementation to provide common logic with extracting data.
 *
 * @author BBonev
 */
public abstract class BaseExecutableOperation implements ExecutableOperation {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseExecutableOperation.class);

	@Inject
	protected TypeConverter typeConverter;

	@Inject
	protected ResourceService resourceService;

	@Inject
	protected DatabaseIdManager idManager;

	@Inject
	protected SecurityContext securityContext;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean couldBeAsynchronous(OperationContext data) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Serializable, Operation> getDependencies(OperationContext data) {
		// no dependencies by default
		return Collections.emptyMap();
	}

	/**
	 * Extract {@link InstanceReference} using the given id and type keys. The method will throw an exception if are
	 * missing the values for the given keys and if the type is invalid.
	 *
	 * @param properties
	 *            the properties
	 * @param idKey
	 *            the id key
	 * @param typeKey
	 *            the type key
	 * @param optional
	 *            if <code>true</code> the method will not throw an exception but will return <code>null</code>
	 * @return the instance reference
	 */
	protected InstanceReference extractReference(JSONObject properties, String idKey, String typeKey,
			boolean optional) {
		String id = JsonUtil.getStringValue(properties, idKey);
		boolean generatedId = false;
		if (id == null) {
			// if there is no ID we will generate one, but we will not track it for now
			// only when we are sure we have all the data
			id = typeConverter.convert(String.class, idManager.generateId());
			generatedId = true;
		}
		String type = JsonUtil.getStringValue(properties, typeKey);
		if (type == null) {
			if (optional) {
				return null;
			}
			throw new IllegalArgumentException("Missing instance type for key: " + typeKey);
		}
		InstanceReference referece = typeConverter.convert(InstanceReference.class, type);
		if (referece == null) {
			if (optional) {
				return null;
			}
			throw new IllegalArgumentException("Invalid instance type " + type);
		}
		referece.setId(id);

		if (generatedId) {
			// we register the id when we now that all data has been passed
			// this is not to track id that is not going to be persisted ever
			idManager.registerId(id);
		}
		return referece;
	}

	/**
	 * Extract properties from the given {@link JSONObject} request.
	 *
	 * @param data
	 *            the data
	 * @param mapKey
	 *            the key for the properties to be read and parsed
	 * @return the extracted properties
	 */
	protected Map<String, String> extractProperties(JSONObject data, String mapKey) {
		JSONObject jsonProperties = JsonUtil.getJsonObject(data, mapKey);
		Map<String, String> properties = CollectionUtils.emptyMap();
		if (jsonProperties != null) {
			properties = CollectionUtils.createLinkedHashMap(jsonProperties.length());
			for (Iterator<?> iterator = jsonProperties.keys(); iterator.hasNext();) {
				String key = iterator.next().toString();
				try {
					Object value = jsonProperties.get(key);
					if (value instanceof JSONObject || value instanceof JSONArray) {
						// some complex properties
						LOGGER.warn("Complex properties not supported, yet. Skipping key-value pair {}={}", key, value);
					} else {
						properties.put(key, value.toString());
					}
				} catch (JSONException e) {
					LOGGER.debug("Failed to get property {}", key, e);
				}
			}
		}
		return properties;
	}


}