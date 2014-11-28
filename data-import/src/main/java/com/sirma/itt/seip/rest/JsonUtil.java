package com.sirma.itt.seip.rest;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The JsonUtil is helper class to store some useful common operations
 */
public class JsonUtil {

	/**
	 * Convert to json object of map. If some value is currently not supported
	 * exception might be thrown.
	 * 
	 * @param data
	 *            the data to convert
	 * @return the json object constructed from the map.
	 */
	@SuppressWarnings("unchecked")
	public static JsonObject convertToJsonObject(Map<String, ? extends Serializable> data) {
		JsonObject result = new JsonObject();
		for (Entry<String, ? extends Serializable> entry : data.entrySet()) {
			Serializable value = entry.getValue();
			if (value instanceof String) {
				result.addProperty(entry.getKey(), value.toString());
			} else if (value instanceof Number) {
				result.addProperty(entry.getKey(), (Number) value);
			} else if (value instanceof Boolean) {
				result.addProperty(entry.getKey(), (Boolean) value);
			} else if (value instanceof Character) {
				result.addProperty(entry.getKey(), (Character) value);
			} else if (value instanceof Map) {
				result.add(entry.getKey(),
						convertToJsonObject((Map<String, ? extends Serializable>) value));
			} else {
				throw new RuntimeException("Not implemented yet! " + value);
			}
		}
		return result;
	}

	/**
	 * Sets {@link Instance} properties retrieved from a {@link JsonObject}
	 * 
	 * @param json
	 *            source
	 * @param instance
	 *            destination
	 */
	public static void setInstanceProperties(JsonObject json, Instance instance) {
		instance.setId(json.getAsJsonPrimitive("id").getAsString());

		for (Entry<String, JsonElement> entry : json.entrySet()) {
			if (entry.getValue().isJsonPrimitive()) {
				instance.getProperties().put(entry.getKey(), entry.getValue().getAsString());
			}
		}
	}

	/**
	 * Converts {@link JsonObject} to {@link Instance}
	 * 
	 * @param json
	 *            Source
	 * @return {@link Instance} representation of the json
	 */
	public static Instance toInstace(JsonObject json) {
		Instance instance = new Instance();
		JsonUtil.setInstanceProperties(json, instance);
		return instance;
	}

	/**
	 * Converts {@link JsonObject} to {@link CaseInstance}
	 * 
	 * @param json
	 *            Source
	 * @return {@link CaseInstance} representation of the json
	 */
	public static CaseInstance toCaseInstace(JsonObject json) {
		CaseInstance instance = new CaseInstance();
		JsonUtil.setInstanceProperties(json, instance);

		for (JsonElement current : json.getAsJsonArray("sections")) {
			JsonObject section = current.getAsJsonObject();
			instance.getSections().put(section.get("name").getAsString(),
					section.get("id").getAsString());
		}
		return instance;
	}
}
