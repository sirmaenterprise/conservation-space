package com.sirma.sep.content.idoc;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonReader;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sirma.sep.content.idoc.handler.SearchContentNodeHandler;

/**
 * Represents generic widget configuration. Subclasses may be used to provide more meaningful methods for configuration
 * manipulation
 *
 * @author BBonev
 */
public class WidgetConfiguration {

	protected static final Gson DEFAULT_GSON_OBJECT = new Gson();

	/**
	 * Used to store the ids of the selected objects, when the selection mode of the processed widget is manually.
	 * <p>
	 * Type: json array
	 */
	private static final String SELECTED_OBJECTS_KEY = "selectedObjects";

	/**
	 * Used to store the id of the selected object, when the selection mode of the processed widget is manually and the
	 * user need to select only one object.
	 * <p>
	 * Type: single string value
	 */
	private static final String SELECTED_OBJECT_KEY = "selectedObject";
	private static final String SELECTED_PROPERTIES_KEY = "selectedProperties";

	private static final String CRITERIA_KEY = "criteria";
	private static final String SELECT_OBJECT_MODE_KEY = "selectObjectMode";

	private static final Type STRING_COLLECTION_TYPE = new TypeToken<Collection<String>>() {
		// empty body
	}.getType();

	private JsonObject config;
	private Widget widget;

	/**
	 * Used as temporary store for the widget search results. They could be used for further processing, if needed. This
	 * results won't be persisted anywhere. Default value is {@link WidgetResults#EMPTY}.
	 */
	private WidgetResults resultsStore = WidgetResults.EMPTY;

	/**
	 * Instantiates a new widget configuration.
	 *
	 * @param widget
	 *            the widget that owns the given configuration. Optional parameter. If present the changes to the
	 *            configuration will be written back to that widget.
	 * @param encodedConfig
	 *            the encoded configuration
	 */
	public WidgetConfiguration(Widget widget, String encodedConfig) {
		this(widget, decodeConfiguration(encodedConfig));
	}

	/**
	 * Instantiates a new widget configuration.
	 *
	 * @param widget
	 *            the widget that owns the given configuration. Optional parameter. If present the changes to the
	 *            configuration will be written back to that widget.
	 * @param configuration
	 *            widget configuration in JSON format
	 */
	public WidgetConfiguration(Widget widget, JsonObject configuration) {
		this.widget = widget;
		config = Objects.requireNonNull(configuration, "Configuration is required!");
	}

	/**
	 * Decodes the provided string configuration from Base64.
	 *
	 * @param configAttr
	 *            - the provided string configuration
	 * @return the decoded configuration
	 */
	private static JsonObject decodeConfiguration(String configAttr) {
		byte[] decoded = Base64.getDecoder().decode(configAttr);
		String decodedString = new String(decoded, StandardCharsets.UTF_8);
		return new JsonParser().parse(decodedString).getAsJsonObject();
	}

	/**
	 * Encodes the provided configuration to Base64 format.
	 *
	 * @param configuration
	 *            - the provided config for encoding
	 * @return the encoded config
	 */
	private static String encodeConfiguration(JsonObject configuration) {
		String migratedConfiguration = configuration.toString();
		return Base64.getEncoder().encodeToString(migratedConfiguration.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Gets a mutable raw widget configuration. Any changes to the object will be propagated to the current object state
	 * but changes will not be applied to the widget unless one of the methods {@link #writeConfiguration()} or
	 * {@link #writeConfiguration(JsonObject)} is called.
	 *
	 * @return the configuration
	 */
	public JsonObject getConfiguration() {
		return config;
	}

	/**
	 * Adds property to the current widget configuration. If the key is missing or the value is <code>null</code> the
	 * method will do nothing.
	 *
	 * @param key
	 *            the key with which the property should be mapped
	 * @param value
	 *            the property that should be added to the configuration
	 */
	public void addNotNullProperty(String key, JsonElement value) {
		if (StringUtils.isBlank(key) || value == null) {
			return;
		}

		config.add(key, value);
	}

	/**
	 * Writes the given configuration to the underling widget instance. If <code>null</code> is passed only write will
	 * happen.
	 *
	 * @param configuration
	 *            the configuration
	 */
	public void writeConfiguration(JsonObject configuration) {
		if (configuration != null) {
			config = configuration;
		}
		writeConfiguration();
	}

	/**
	 * Writes the current configuration to the underling widget instance.
	 */
	public void writeConfiguration() {
		if (widget != null) {
			widget.setConfiguration(encodeConfiguration(config));
		}
	}

	/**
	 * Get widget search criteria if any.
	 *
	 * @return optional of the search criteria tree
	 */
	public Optional<javax.json.JsonObject> getSearchCriteria() {
		JsonObject criteria = getConfiguration().getAsJsonObject(CRITERIA_KEY);
		if (criteria == null || criteria.isJsonNull() || criteria.entrySet().isEmpty()) {
			return Optional.empty();
		}

		try (JsonReader reader = Json.createReader(new StringReader(criteria.toString()))) {
			return Optional.of(reader.readObject());
		}
	}

	/**
	 * Gets the value of the 'selectObjectMode' from the configuration.
	 *
	 * @return the selection mode of the widget (manually, automatically, etc..)
	 */
	public WidgetSelectionMode getSelectionMode() {
		String mode = getProperty(SELECT_OBJECT_MODE_KEY, String.class);
		return WidgetSelectionMode.getMode(mode);
	}

	/**
	 * Sets value from the property 'selectObjectMode' in the current configuration.
	 *
	 * @param mode
	 *            one of the {@link WidgetSelectionMode}
	 */
	public void setSelectionMode(WidgetSelectionMode mode) {
		config.addProperty(SELECT_OBJECT_MODE_KEY, mode.toString().toLowerCase());
	}

	/**
	 * Retrieves property of type {@link JsonArray} as {@link List}. Could return empty list, when there is no property
	 * with the passed key or the size of the array is <code>0</code>.
	 *
	 * @param <R>
	 *            the type of the list elements
	 * @param key
	 *            the key of the property that should be retrieved
	 * @return {@link List} of values of the array element that should be retrieved
	 * @see Gson#fromJson(JsonElement, Type)
	 */
	public <R> List<R> getArrayPropertyAsList(String key) {
		JsonElement element = getConfiguration().get(key);
		if (element == null || element.isJsonNull() || !element.isJsonArray()) {
			return emptyList();
		}

		JsonArray array = element.getAsJsonArray();
		if (array.size() == 0) {
			return emptyList();
		}

		Type type = new TypeToken<List<R>>() {
			// empty body
		}.getType();

		return DEFAULT_GSON_OBJECT.fromJson(array, type);
	}

	/**
	 * Retrieves property for given key. If there is no property with such key <code>null</code> will be returned.
	 *
	 * @param <R>
	 *            the type of the element
	 * @param key
	 *            the key of the property which value should be retrieved
	 * @param type
	 *            the return type
	 * @return the value of the found property or <code>null</code> if no property is found or the return type does not
	 *         match the element type
	 * @see Gson#fromJson(JsonElement, Class)
	 */
	public <R> R getProperty(String key, Class<R> type) {
		JsonElement element = getConfiguration().get(key);
		return DEFAULT_GSON_OBJECT.fromJson(element, type);
	}

	/**
	 * Removes property from the widget configuration by property key. If blank key is passed the method will do nothing
	 * and return.
	 *
	 * @param key
	 *            of the property that should be removed
	 * @return current widget configuration
	 */
	public WidgetConfiguration removeProperty(String key) {
		if (StringUtils.isBlank(key)) {
			return this;
		}

		getConfiguration().remove(key);
		return this;
	}

	/**
	 * Retrieves all selected objects from widget configuration. It is convenient method that combines checks if there
	 * are selected objects and collects them it there are. Collected properties: {@link #SELECTED_OBJECT_KEY},
	 * {@link #SELECTED_OBJECTS_KEY}.
	 *
	 * @return {@link Set} of {@link Serializable} values representing the ids of the selected objects
	 */
	// this should be simplified/removed at some point, when the widget configurations are unified
	public Set<Serializable> getAllSelectedObjects() {
		Set<Serializable> ids = new HashSet<>();
		if (hasSelectedObject()) {
			ids.add(config.get(SELECTED_OBJECT_KEY).getAsString());
		}

		if (hasSelectedObjects()) {
			ids.addAll(getArrayPropertyAsList(SELECTED_OBJECTS_KEY));
		}

		return ids;
	}

	/**
	 * Cleans up all properties values for selected objects. The properties that will be cleaned:
	 * {@link #SELECTED_OBJECTS_KEY}, {@link #SELECTED_OBJECT_KEY}. The method will check if the configuration contains
	 * the property first.
	 *
	 * @return current widget configuration with cleaned properties for selected objects
	 */
	public WidgetConfiguration cleanUpAllSelectedObjects() {
		if (hasSelectedObject()) {
			config.addProperty(SELECTED_OBJECT_KEY, "");
		}

		if (hasSelectedObjects()) {
			config.add(SELECTED_OBJECTS_KEY, new JsonArray());
		}

		return this;
	}

	/**
	 * Checks if the current configuration has property with key 'selectedObjects'.
	 *
	 * @return <code>true</code> if the configuration has property, <code>false</code> otherwise
	 */
	public boolean hasSelectedObjects() {
		return config.has(SELECTED_OBJECTS_KEY);
	}

	/**
	 * Checks if the current configuration has property with key 'seletedObject'.
	 *
	 * @return <code>true</code> if the configuration has property, <code>false</code> otherwise
	 */
	public boolean hasSelectedObject() {
		return config.has(SELECTED_OBJECT_KEY);
	}

	/**
	 * Sets values for configuration property with key 'selectedObjects' in the current configuration.
	 *
	 * @param identifiers
	 *            the values for the property, usually collection of instance ids
	 */
	public void setSelectedObjects(Collection<Serializable> identifiers) {
		if (isEmpty(identifiers)) {
			return;
		}

		config.add(SELECTED_OBJECTS_KEY, DEFAULT_GSON_OBJECT.toJsonTree(identifiers, STRING_COLLECTION_TYPE));
	}

	/**
	 * Getter for configuration property for key 'selectedObjects'.
	 *
	 * @return the values for the key 'selectedObjects' from the current configuration
	 */
	public Collection<String> getSelectedObjects() {
		return getArrayPropertyAsList(SELECTED_OBJECTS_KEY);
	}

	/**
	 * Sets value for configuration property with key 'selectedObject' in the current configuration.
	 *
	 * @param identifier
	 *            the value for the property, usualy instance id
	 */
	public void setSelectedObject(Serializable identifier) {
		String id = Objects.toString(identifier, null);
		if (StringUtils.isBlank(id)) {
			return;
		}

		config.addProperty(SELECTED_OBJECT_KEY, id);
	}

	/**
	 * Getter for configuration property for key 'selectedObject'.
	 *
	 * @return the value for the key 'selectedObject' from the current configuration
	 */
	public String getSelectedObject() {
		return getProperty(SELECTED_OBJECT_KEY, String.class);
	}

	/**
	 * Gets for the widget search results. This results are retrieved by {@link SearchContentNodeHandler} and they will
	 * be stored temporary in the widget configuration, if we need to use it further.
	 *
	 * @return the results from the widget search, if any
	 */
	public WidgetResults getSearchResults() {
		return resultsStore;
	}

	/**
	 * Sets the search results for the widget. This is temporary store and it could be used, if the results are used for
	 * further processing(for example versions or server-side rendering). This results are stored by
	 * {@link SearchContentNodeHandler}.
	 *
	 * @param results the search results that should be stored for the widget
	 */
	public void setSearchResults(WidgetResults results) {
		if (results == null) {
			// restore the class default value
			resultsStore = WidgetResults.EMPTY;
		} else {
			resultsStore = results;
		}
	}

	/**
	 * Provides access to the map of the selected properties to be displayed in the particular widget.
	 *
	 * @return json object containing the selected properties.
	 */
	public JsonObject getSelectedProperties() {
		return getConfiguration().getAsJsonObject(SELECTED_PROPERTIES_KEY);
	}

}
