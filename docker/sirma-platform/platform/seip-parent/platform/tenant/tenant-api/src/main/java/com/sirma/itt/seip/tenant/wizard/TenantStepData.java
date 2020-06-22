package com.sirma.itt.seip.tenant.wizard;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The TenantStepData is wrapper for internal data model of tenant step.
 *
 * @author bbanchev
 */
public class TenantStepData implements Identity, JsonRepresentable {
	private static final String SUCCESS = "$success$";
	/** Map/Json key for list of properties. */
	public static final String KEY_PROPERTIES_LIST = "properties";
	/** Map/Json key for step/property id. */
	public static final String KEY_ID = "id";
	/** Map/Json property value. */
	public static final String KEY_VALUE = "value";
	private JSONObject model;
	private String identifier;
	private Map<String, Serializable> properties;
	private List<File> models = new LinkedList<>();

	/**
	 * Instantiates a new tenant step data.
	 *
	 * @param identifier
	 *            is the step id
	 * @param dataModel
	 *            the data model
	 */
	public TenantStepData(String identifier, JSONObject dataModel) {
		this.identifier = identifier;
		model = dataModel;
	}

	/**
	 * Creates the empty.
	 *
	 * @param name
	 *            the name
	 * @return the tenant step data
	 */
	public static TenantStepData createEmpty(String name) {
		JSONObject dataModel = new JSONObject();
		JsonUtil.addToJson(dataModel, KEY_PROPERTIES_LIST, new JSONArray());
		JsonUtil.addToJson(dataModel, KEY_ID, name);
		return new TenantStepData(name, dataModel);
	}

	/**
	 * Creates instance using the given data model if not null or empty one if null.
	 *
	 * @param name
	 *            the name
	 * @param dataModel
	 *            the data model
	 * @return the tenant step data
	 */
	public static TenantStepData create(String name, JSONObject dataModel) {
		if (dataModel == null) {
			return createEmpty(name);
		}
		return new TenantStepData(name, dataModel);
	}

	/**
	 * @return the model represented
	 */
	public Map<String, Serializable> getProperties() {
		if (properties != null) {
			return properties;
		}
		try {
			JSONArray propertiesArray = model.getJSONArray(KEY_PROPERTIES_LIST);
			properties = new LinkedHashMap<>();
			for (int i = 0; i < propertiesArray.length(); i++) {
				JSONObject nextProperty = propertiesArray.getJSONObject(i);
				properties.put(nextProperty.getString(KEY_ID), nextProperty.getString(KEY_VALUE));
			}
		} catch (Exception e) {
			throw new TenantCreationException("Invalid tenant step model! ", e);
		}
		return properties;
	}

	/**
	 * Add a file to the available tenant step models.
	 *
	 * @param file
	 *            the file to be added
	 */
	public void addModel(File file) {
		models.add(file);
	}

	/**
	 * Get all available tenant step models.
	 *
	 * @return the available tenant step models
	 */
	public List<File> getModels() {
		return models;
	}

	/**
	 * Reads a generic property from current model from the properties array
	 *
	 * @param data
	 *            is the step data
	 * @param key
	 *            is the id of the data
	 * @param required
	 *            whether to throw exception on missing value
	 * @return the value or null if this is not a required field
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Serializable> T readGenericValue(String key, boolean required) {
		Serializable result = null;
		result = getProperties().get(key);
		if (result == null) {
			if (required) {
				throw new TenantCreationException("Missing required argument: " + key);
			}
			return null;
		}
		return (T) result;
	}

	/**
	 * Reads a string property from current model from the properties array
	 *
	 * @param key
	 *            is the id of the data
	 * @param required
	 *            whether to throw exception on missing value
	 * @return the value or null if this is not a required field
	 */
	public String getPropertyValue(String key, boolean required) {
		Serializable genericValue = readGenericValue(key, required);
		if (genericValue != null) {
			String stringValue = genericValue.toString().trim();
			if (StringUtils.isNotBlank(stringValue)) {
				return stringValue;
			}
		}
		return null;
	}

	/**
	 * Reads a string property from current model from the properties array
	 *
	 * @param key
	 *            is the id of the data
	 * @param defaultValue
	 *            the default value to return if the requested is missing
	 * @return the value or null if this is not a required field
	 */
	public String getPropertyValue(String key, String defaultValue) {
		String genericValue = getPropertyValue(key, false);
		return EqualsHelper.getOrDefault(genericValue, defaultValue);
	}

	@Override
	public void setIdentifier(String identifier) {
		throw new TenantCreationException("Illegal access - Id for step is set internally!");
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Marks the current step as completed successfully. This is internal operation and could be used when rolling back
	 * step to verify if the current step was completed in order not to try to revert something that was not
	 * initialized.
	 */
	public void completedSuccessfully() {
		getProperties().put(SUCCESS, Boolean.TRUE);
	}

	/**
	 * Checks if the current tenant step has been completed successfully.
	 *
	 * @return true if it has, false otherwise
	 * @see #completedSuccessfully()
	 */
	public boolean isCompleted() {
		return getProperties().containsKey(SUCCESS);
	}

	@Override
	public JSONObject toJSONObject() {
		return model;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		model = jsonObject;
	}

}
