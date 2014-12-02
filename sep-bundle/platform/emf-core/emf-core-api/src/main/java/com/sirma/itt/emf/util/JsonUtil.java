package com.sirma.itt.emf.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.StringPair;
import com.sirma.itt.emf.domain.model.TreeNode;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.security.model.Action;

/**
 * Utility class for helper methods for working with JSON objects REVIEW - 1. may be move in emf
 * core 2. exception could be thrown if key is not found
 * 
 * @author BBonev
 */
public class JsonUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

	/**
	 * Adds the to JSON object.
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public static void addToJson(JSONObject object, String key, Object value) {
		if ((object == null) || (key == null)) {
			return;
		}
		try {
			object.put(key, value);
		} catch (JSONException e) {
			LOGGER.warn("Failed to set entry with key {} and value {}", key, value, e);
			try {
				object.put(key, (String) null);
			} catch (JSONException e1) {
				LOGGER.warn("Failed to put entry with key {} and value null", key, e1);
			}
		}
	}

	/**
	 * Copy to json.
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param source
	 *            the source
	 * @param sourceKey
	 *            the source key
	 */
	public static void copyToJson(JSONObject object, String key, Map<String, ?> source,
			String sourceKey) {
		if ((object == null) || (key == null)) {
			return;
		}
		copyToJson(object, key, source, sourceKey, null);
	}

	/**
	 * Copy to json.
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param source
	 *            the source
	 */
	public static void copyToJson(JSONObject object, String key, Map<String, ?> source) {
		if ((object == null) || (key == null)) {
			return;
		}
		copyToJson(object, key, source, key, null);
	}

	/**
	 * Copy to json.
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param source
	 *            the source
	 * @param targetClass
	 *            the target class
	 */
	public static void copyToJson(JSONObject object, String key, Map<String, ?> source,
			Class<?> targetClass) {
		if ((object == null) || (key == null)) {
			return;
		}
		copyToJson(object, key, source, key, targetClass);
	}

	/**
	 * Copy to json.
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param source
	 *            the source
	 * @param sourceKey
	 *            the source key
	 * @param targetClass
	 *            the target class
	 */
	public static void copyToJson(JSONObject object, String key, Map<String, ?> source,
			String sourceKey, Class<?> targetClass) {
		if ((object == null) || (key == null)) {
			return;
		}
		if (source != null) {
			String localKey = sourceKey;
			if (localKey == null) {
				localKey = key;
			}
			Object value = source.get(localKey);
			if (targetClass != null) {
				value = TypeConverterUtil.getConverter().convert(targetClass, value);
			}
			addToJson(object, key, value);
		}
	}

	/**
	 * Appends the given value to the {@link org.json.JSONArray} that is represented by the given
	 * key.
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @param value
	 *            the value to append
	 */
	public static void append(JSONObject object, String key, Object value) {
		if ((object == null) || (key == null)) {
			return;
		}
		try {
			object.append(key, value);
		} catch (JSONException e) {
			LOGGER.warn("Failed to append to entry with key " + key + " and value " + value, e);
			try {
				object.append(key, (String) null);
			} catch (JSONException e1) {
				LOGGER.warn("Failed to append to entry with key " + key + " and value null", e1);
			}
		}
	}

	/**
	 * Gets a long value from the JSON object with a key if present
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the long value
	 */
	public static Long getLongValue(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value != null) {
			return Long.parseLong(value.toString());
		}
		return null;
	}

	/**
	 * Gets a double value from the JSON object with a key if present
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the double value
	 */
	public static Double getDoubleValue(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value != null) {
			return Double.parseDouble(value.toString());
		}
		return null;
	}

	/**
	 * Gets a String value from the JSON object with a key if present
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the string value
	 */
	public static String getStringValue(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value != null) {
			return value.toString();
		}
		return null;
	}

	/**
	 * Gets an integer value from the JSON object with a key if present
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the integer value
	 */
	public static Integer getIntegerValue(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value != null) {
			return Integer.parseInt(value.toString());
		}
		return null;
	}

	/**
	 * Gets the boolean value.
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the boolean value
	 */
	public static Boolean getBooleanValue(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value != null) {
			return Boolean.parseBoolean(value.toString());
		}
		return null;
	}

	/**
	 * Gets the value from the JSON object if present or <code>null</code> if not
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key to check and retrieve
	 * @return the value or <code>null</code> if not present
	 */
	public static Object getValueOrNull(JSONObject object, String key) {
		if (object.has(key)) {
			try {
				Object value = object.get(key);
				if (((value instanceof String) && (("null".compareTo((String) value) == 0) || ""
						.equals(value))) || (value == JSONObject.NULL)) {
					return null;
				}
				return value;
			} catch (JSONException e) {
				LOGGER.warn("Failed to get value with key " + key, e);
			}
		}
		return null;
	}

	/**
	 * Retrieves a string value from a {@link JSONArray} at a specified index.
	 * 
	 * @param array
	 *            Array from which to get the value.
	 * @param index
	 *            Index of the value in the array.
	 * @return The string value of the element at the specified index or null if there was an error
	 *         while retrieving the value.
	 */
	public static String getStringFromArray(JSONArray array, int index) {
		try {
			String string = array.getString(index);
			return string;
		} catch (JSONException e) {
			LOGGER.warn("Failed to get value from array with index " + index, e);
		}
		return null;
	}

	/**
	 * Get value from array by index
	 * 
	 * @param array
	 *            Array to get from
	 * @param index
	 *            Element index
	 * @return Element or null there was an exception durring retrieval.
	 */
	public static Object getFromArray(JSONArray array, int index) {
		try {
			Object object = array.get(index);
			return object;
		} catch (JSONException e) {
			LOGGER.warn("Failed to get value from array with index " + index, e);
		}
		return null;
	}

	/**
	 * Gets the JSONObject from the given object or <code>null</code> if the result is not an
	 * {@link JSONObject} instance.
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the json object or <code>null</code>
	 */
	public static JSONObject getJsonObject(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value instanceof JSONObject) {
			return (JSONObject) value;
		}
		return null;
	}

	/**
	 * Gets the JSONArray from the given object or <code>null</code> if the result is not an
	 * {@link JSONArray} instance.
	 * 
	 * @param object
	 *            the object
	 * @param key
	 *            the key
	 * @return the json array or <code>null</code>
	 */
	public static JSONArray getJsonArray(JSONObject object, String key) {
		Object value = getValueOrNull(object, key);
		if (value instanceof JSONArray) {
			return (JSONArray) value;
		}
		return null;
	}

	/**
	 * Converts the given data to {@link JSONArray}. If the data is invalid empty array will be
	 * returned.
	 * 
	 * @param data
	 *            the data
	 * @return the jSON array
	 */
	public static JSONArray toJsonArray(String data) {
		try {
			return new JSONArray(data);
		} catch (JSONException e) {
			LOGGER.warn("Json array data was invalid: " + data, e);
		}
		// return empty array if the data is invalid not to break anything
		return new JSONArray();
	}

	/**
	 * Copies missing properties from the given source to destination.
	 * 
	 * @param source
	 *            the source
	 * @param destination
	 *            the destination
	 */
	public static void copyMissingProperties(JSONObject source, JSONObject destination) {
		for (Iterator<?> it = source.keys(); it.hasNext();) {
			String type = (String) it.next();
			if (!destination.has(type)) {
				try {
					destination.put(type, source.get(type));
				} catch (JSONException e) {
					LOGGER.warn("Failed to copy property " + type, e);
				}
			}
		}
	}

	/**
	 * Builds a tree from the given list of nodes.
	 * 
	 * @param <T>
	 *            the node to build the tree from
	 * @param <I>
	 *            the id type
	 * @param entries
	 *            the entries
	 * @param converter
	 *            the converter
	 * @return the jSON object
	 */
	public static <T extends TreeNode<I>, I> JSONArray buildTree(List<T> entries,
			TypeConverter converter) {
		JSONArray tasksTree = new JSONArray();
		JSONObject sortedByParentTasks = new JSONObject();
		try {
			tasksTree = makeTree(entries, tasksTree, sortedByParentTasks, converter, null);
		} catch (JSONException e) {
			LOGGER.warn("Failed to build tree due to " + e.getMessage());
			LOGGER.trace("", e);
			return new JSONArray(Arrays.asList(mapError(e.getMessage())));
		}

		return tasksTree;
	}

	/**
	 * Builds the tree representation same as the given template as JSON.
	 * 
	 * @param <T>
	 *            the node to build the tree from
	 * @param <I>
	 *            the id type
	 * @param template
	 *            the template
	 * @param entries
	 *            the entries
	 * @param converter
	 *            the converter
	 * @return the jSON array
	 */
	public static <T extends TreeNode<I>, I> JSONArray buildTreeAs(JSONArray template,
			List<T> entries, TypeConverter converter) {
		JSONArray tasksTree = new JSONArray();
		JSONObject sortedByParentTasks = new JSONObject();
		try {
			tasksTree = makeTree(entries, tasksTree, sortedByParentTasks, converter, template);
		} catch (JSONException e) {
			LOGGER.warn("Failed to make tree due to " + e.getMessage());
			LOGGER.trace("", e);
			return new JSONArray(Arrays.asList(mapError(e.getMessage())));
		}
		return tasksTree;
	}

	/**
	 * Make tree.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param <I>
	 *            the generic type
	 * @param list
	 *            the list
	 * @param tasksTree
	 *            The tasks tree.
	 * @param sortedByParentTasks
	 *            The sorted by parent tasks.
	 * @param converter
	 *            the converter
	 * @param template
	 *            the template
	 * @return the jSON array
	 * @throws JSONException
	 *             the jSON exception
	 */
	private static <T extends TreeNode<I>, I> JSONArray makeTree(List<T> list, JSONArray tasksTree,
			JSONObject sortedByParentTasks, TypeConverter converter, JSONArray template)
			throws JSONException {

		Iterator<T> listIterator = list.iterator();
		String parentId;

		while (listIterator.hasNext()) {
			T task = listIterator.next();
			JSONArray equalParentId;
			parentId = "" + task.getParentId();
			JSONObject taskJSON = converter.convert(JSONObject.class, task);

			if (sortedByParentTasks.has(parentId)) {
				sortedByParentTasks.accumulate(parentId, taskJSON);
			} else {
				equalParentId = new JSONArray();
				equalParentId.put(taskJSON);
				sortedByParentTasks.put(parentId, equalParentId);
			}
		}

		if (!list.isEmpty()) {
			if (template == null) {
				if (sortedByParentTasks.has("null")) {
					addNodes(sortedByParentTasks.getJSONArray("null"), tasksTree,
							sortedByParentTasks);
				} else {
					String next = (String) sortedByParentTasks.keys().next();
					addNodes(sortedByParentTasks.getJSONArray(next), tasksTree, sortedByParentTasks);
				}
			} else {
				for (int i = 0; i < template.length(); i++) {
					JSONObject object = template.getJSONObject(i);
					String parent = JsonUtil.getStringValue(object, DefaultProperties.PARENT_ID);
					if (parent == null) {
						if (sortedByParentTasks.has("null")) {
							addNodes(sortedByParentTasks.getJSONArray("null"), tasksTree,
									sortedByParentTasks);
						} else {
							throw new EmfRuntimeException("Invalid parent: \"null\"");
						}
					} else {
						if (sortedByParentTasks.has(parent)) {
							addNodes(sortedByParentTasks.getJSONArray(parent), tasksTree,
									sortedByParentTasks);
						} else {
							throw new EmfRuntimeException("Invalid parent: " + parent);
						}
					}
				}
			}

		}

		return tasksTree;
	}

	/**
	 * Adds the nodes.
	 * 
	 * @param nodes
	 *            the nodes
	 * @param tasksTree
	 *            the tasks tree
	 * @param sortedByParentTasks
	 *            the sorted by parent tasks
	 * @throws JSONException
	 *             the jSON exception
	 */
	private static void addNodes(JSONArray nodes, JSONArray tasksTree,
			JSONObject sortedByParentTasks) throws JSONException {
		for (int i = 0, l = nodes.length(); i < l; i++) {
			JSONObject taskChildrenObject = nodes.getJSONObject(i);
			listHierarchy(taskChildrenObject, sortedByParentTasks);
			tasksTree.put(taskChildrenObject);
		}
	}

	/**
	 * List hierarchy.
	 * 
	 * @param task
	 *            the task
	 * @param sortedByParentTasks
	 *            the sorted by parent tasks
	 * @throws JSONException
	 *             the jSON exception
	 */
	private static void listHierarchy(JSONObject task, JSONObject sortedByParentTasks)
			throws JSONException {
		JSONArray childrenArray = new JSONArray();
		JSONArray childNodes = new JSONArray();

		try {
			childNodes = sortedByParentTasks.getJSONArray("" + task.get(DefaultProperties.ID));
		} catch (JSONException e) {
		}

		if (childNodes.length() > 0) {
			for (int i = 0, l = childNodes.length(); i < l; i++) {
				JSONObject childObject = childNodes.getJSONObject(i);
				childrenArray.put(childObject);
				try {
					task.put("children", childrenArray);
					task.put(DefaultProperties.LEAF, false);
					task.put("expanded", true);
				} catch (JSONException e) {
				}

				listHierarchy(childObject, sortedByParentTasks);
			}
		}
	}

	/**
	 * Generates modelMap to return in the modelAndView in case of exception.
	 * 
	 * @param msg
	 *            message
	 * @return the string
	 */
	public static JSONObject mapError(String msg) {

		JSONObject modelMap = new JSONObject();
		addToJson(modelMap, "message", msg);
		addToJson(modelMap, "success", Boolean.FALSE);
		return modelMap;
	}

	/**
	 * Converts the given actions collection to JSON array
	 * 
	 * @param actions
	 *            the actions
	 * @return the jSON array
	 */
	public static JSONArray convertActions(Collection<Action> actions) {
		JSONArray array = new JSONArray();
		for (Action action : actions) {
			JSONObject jsonObject = new JSONObject();
			addToJson(jsonObject, "action", action.getActionId());
			addToJson(jsonObject, "label", action.getLabel());
			addToJson(jsonObject, "confirmationMessage", action.getConfirmationMessage());
			addToJson(jsonObject, "disabledReason", action.getDisabledReason());
			addToJson(jsonObject, "onClick", action.getOnclick());
			addToJson(jsonObject, "disabled", action.isDisabled());
			addToJson(jsonObject, "immediate", action.isImmediateAction());

			array.put(jsonObject);
		}
		return array;
	}

	/**
	 * Adds the given actions to the target JSON object.
	 * 
	 * @param object
	 *            the object
	 * @param actions
	 *            the actions
	 */
	public static void addActions(JSONObject object, Collection<Action> actions) {
		addToJson(object, "actions", convertActions(actions));
	}

	/**
	 * Parses a string to a {@link JSONArray}.
	 * 
	 * @param arrayString
	 *            String to parse.
	 * @return {@link JSONArray} representation of the string, or null if there was an error during
	 *         parse.
	 */
	public static JSONArray createArrayFromString(String arrayString) {
		try {
			JSONArray jsonArray = new JSONArray(arrayString);
			return jsonArray;
		} catch (JSONException e) {
			LOGGER.warn("Failed to parse string {}", arrayString, e);
		}
		return null;
	}

	/**
	 * Parses a string to a {@link JSONObject}.
	 * 
	 * @param objectString
	 *            String representing a JSON object.
	 * @return {@link JSONObject} representation of the string, or null if there was an error during
	 *         parse.
	 */
	public static JSONObject createObjectFromString(String objectString) {
		if (objectString == null) {
			return null;
		}
		try {
			return new JSONObject(objectString);
		} catch (JSONException e) {
			LOGGER.warn("Failed to parse string {}", objectString, e);
		}
		return null;
	}

	/**
	 * Builds a {@link JSONObject} from the provided {@link InstanceReference}.
	 * 
	 * @param source
	 *            the source
	 * @return the JSON object
	 */
	public static JSONObject toJsonObject(InstanceReference source) {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "instanceId", source.getIdentifier());
		JsonUtil.addToJson(object, "instanceType", source.getReferenceType().getName());
		return object;
	}

	/**
	 * Builds a {@link JSONObject} from string or map. If the value is not of those types, convert
	 * to String is attempted
	 * 
	 * @param value
	 *            the value to convert
	 * @return the JSON object or null on error
	 */
	public static JSONObject toJsonObject(Serializable value) {
		if (value == null) {
			return null;
		}
		try {
			if (value instanceof String) {
				return new JSONObject(value.toString());
			}
			if (value instanceof Map) {
				return new JSONObject((Map<?, ?>) value);
			}
			String converted = TypeConverterUtil.getConverter().convert(String.class, value);
			if (converted != null) {
				return new JSONObject(converted);
			}
		} catch (Exception e) {
			LOGGER.error("Error during convert to JSONObject of {}", value, e);
		}
		return null;
	}

	/**
	 * Transforms an emf instance to json object by copying properties.
	 * 
	 * @param instance
	 *            instance to transform
	 * @param properties
	 *            properties to map.
	 * @return new json object containing the mapped properties or null if the provided instance is
	 *         null.
	 */
	public static JSONObject transformInstance(Instance instance, String... properties) {
		JSONObject result = null;
		if ((instance != null) && (properties != null)) {
			result = new JSONObject();
			Map<String, Serializable> instanceProperties = instance.getProperties();
			for (String propertyName : properties) {
				JsonUtil.addToJson(result, propertyName, instanceProperties.get(propertyName));
			}

			if (instance.getId() != null) {
				JsonUtil.addToJson(result, "id", instance.getId().toString());
			}
		}
		return result;
	}

	/**
	 * Transforms an emf instance to json object by copying properties.
	 * 
	 * @param instance
	 *            instance to transform
	 * @param properties
	 *            properties to map. The first element in the StringPair is the name of the json
	 *            (destination) property, the second element is the name of the {@link Instance}
	 *            (source) property.
	 * @return new json object containing the mapped properties or null if the provided instance is
	 *         null.
	 */
	public static JSONObject transformInstance(Instance instance, StringPair... properties) {
		JSONObject result = null;
		if ((instance != null) && (properties != null)) {
			result = new JSONObject();
			Map<String, Serializable> instanceProperties = instance.getProperties();
			for (StringPair property : properties) {
				JsonUtil.addToJson(result, property.getFirst(),
						instanceProperties.get(property.getSecond()));
			}

			if (instance.getId() != null) {
				JsonUtil.addToJson(result, "id", instance.getId().toString());
			}
		}
		return result;
	}

	/**
	 * From Json to map object.
	 * 
	 * @param jsonObject
	 *            the json object
	 * @return the map
	 */
	public static Map<String, Object> jsonToMap(JSONObject jsonObject) {
		Map<String, Object> map = new HashMap<>();
		Iterator<?> interator = jsonObject.keys();
		while (interator.hasNext()) {
			String key = (String) interator.next();
			Object value = JsonUtil.getValueOrNull(jsonObject, key);
			if ((value != null) && (value instanceof JSONArray)) {
				JSONArray jsonArray = (JSONArray) value;
				map.put(key, jsonArrayToList(jsonArray));
			} else {
				map.put(key, JsonUtil.getValueOrNull(jsonObject, key));
			}
		}
		return map;
	}

	/**
	 * Json array to list.
	 * 
	 * @param jsonArray
	 *            the json array
	 * @return the list
	 */
	public static List<?> jsonArrayToList(JSONArray jsonArray) {
		return TypeConverterUtil.getConverter().convert(List.class, jsonArray);
	}

}
