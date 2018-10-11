package com.sirma.sep.content.idoc.nodes.widgets.recentactivities;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Represents configuration of recent activities widget. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class RecentActivitiesWidgetConfiguration extends WidgetConfiguration {

	/**
	 * Used to store selected object for the {@link RecentActivitiesWidget}, when the selection mode is manually.
	 * <p>
	 * Type: json array that contains json objects with ids
	 */
	private static final String SELECTED_ITEMS_KEY = "selectedItems";
	private static final String PAGE_SIZE_KEY = "pageSize";
	private static final String ALL = "All";
	private static final String ID_KEY = "id";

	/**
	 * Constructor for widget configuration passed as string. It is encoded/decoded by super class.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as string
	 */
	public RecentActivitiesWidgetConfiguration(Widget widget, String configuration) {
		super(widget, configuration);
	}

	/**
	 * Constructor for widget configuration passed {@link JsonObject}.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as json
	 */
	public RecentActivitiesWidgetConfiguration(Widget widget, JsonObject configuration) {
		super(widget, configuration);
	}

	/**
	 * Retrieves the limit of the object that should be displayed in the widget.
	 *
	 * @param defaultValue
	 *            the limit that will be returned, if the property is not passed in the configuration
	 * @return the limit configuration or given default value, if the configuration is missing. If "All" is passed as
	 *         value for this configuration, the method will return -1 (unlimited)
	 */
	public int getLimit(int defaultValue) {
		JsonElement element = getConfiguration().get(PAGE_SIZE_KEY);
		if (element != null && element.isJsonPrimitive()) {
			JsonPrimitive primitive = (JsonPrimitive) element;
			if (primitive.isNumber()) {
				return primitive.getAsInt();
			}

			// unlimited is passed as "All"
			if (primitive.isString() && ALL.equalsIgnoreCase(primitive.getAsString())) {
				return -1;
			}
		}

		return defaultValue;
	}

	/**
	 * Gets the ids of the objects stored in 'selectedItems' property.
	 *
	 * @return {@link Collection} of ids
	 */
	// should be removed when widget configurations are migrated
	@Override
	public Collection<String> getSelectedObjects() {
		return getArrayPropertyAsList(SELECTED_ITEMS_KEY)
				.stream()
					.map(DEFAULT_GSON_OBJECT::toJsonTree)
					.filter(JsonElement::isJsonObject)
					.map(JsonElement::getAsJsonObject)
					.map(obj -> obj.get(ID_KEY).getAsString())
					.collect(Collectors.toSet());
	}

	/**
	 * Sets passed ids to 'selectedItems' property.
	 *
	 * @param identifiers
	 *            the ids that should be set for the property
	 */
	// should be removed when widget configurations are migrated
	@Override
	public void setSelectedObjects(Collection<Serializable> identifiers) {
		JsonArray items = new JsonArray();
		for (Serializable id : identifiers) {
			JsonObject item = new JsonObject();
			item.addProperty(ID_KEY, (String) id);
			items.add(item);
		}

		getConfiguration().add(SELECTED_ITEMS_KEY, items);
	}

	@Override
	public Set<Serializable> getAllSelectedObjects() {
		Set<Serializable> ids = super.getAllSelectedObjects();
		if (getConfiguration().has(SELECTED_ITEMS_KEY)) {
			// extracts the ids from the selected items, because the items are represented by objects with 'id'
			ids.addAll(getSelectedObjects());
		}

		return ids;
	}

	@Override
	public WidgetConfiguration cleanUpAllSelectedObjects() {
		if (getConfiguration().has(SELECTED_ITEMS_KEY)) {
			getConfiguration().add(SELECTED_ITEMS_KEY, new JsonArray());
		}

		return super.cleanUpAllSelectedObjects();
	}

}
