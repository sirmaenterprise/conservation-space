package com.sirma.sep.content.idoc.nodes.widgets.comments;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Represents configuration of comments widget. Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class CommentsWidgetConfiguration extends WidgetConfiguration {

	private static final String SELECT_CURRENT_OBJECT_KEY = "selectCurrentObject";
	private static final String SELECTED_USERS_KEY = "selectedUsers";
	private static final String FILTER_PROPERTIES_KEY = "filterProperties";
	private static final String FILTER_CRITERIA_KEY = "filterCriteria";
	private static final String LIMIT_KEY = "limit";
	private static final String STATUS_KEY = "status";
	private static final String TEXT_KEY = "text";

	/**
	 * Constructor for widget configuration passed as string. It is encoded/decoded by super class.
	 *
	 * @param widget
	 *            the widget for which is the configuration
	 * @param configuration
	 *            widget configuration as string
	 */
	public CommentsWidgetConfiguration(Widget widget, String configuration) {
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
	public CommentsWidgetConfiguration(Widget widget, JsonObject configuration) {
		super(widget, configuration);
	}

	/**
	 * Shows if current object should be included in results.
	 *
	 * @return <code>true</code> if the current object should be included, <code>false</code> otherwise
	 */
	public boolean isCurrentObjectIncluded() {
		JsonElement element = getConfiguration().get(SELECT_CURRENT_OBJECT_KEY);
		return element != null && element.getAsBoolean();
	}

	/**
	 * Retrieves selected users, which comments should be shown. Note that the method could return <code>null</code>
	 *
	 * @return {@link List} containing selected users, which comments should be shown
	 */
	public List<String> getSelectedUsers() {
		return getArrayPropertyAsList(SELECTED_USERS_KEY);
	}

	/**
	 * Retrieves 'filterProperties' object from the current configuration.
	 *
	 * @return {@link JsonObject} containing filter properties
	 */
	public JsonObject getFilterProperties() {
		return getConfiguration().getAsJsonObject(FILTER_PROPERTIES_KEY);
	}

	/**
	 * Retrieves 'filterCriteria' object from the current configuration.
	 *
	 * @return {@link JsonObject} containing filter criteria
	 */
	public JsonObject getFilterCriteria() {
		return getConfiguration().getAsJsonObject(FILTER_CRITERIA_KEY);
	}

	/**
	 * Sets 'filterCriteria' object to current configuration.
	 *
	 * @param filterCriteria
	 *            the filter criteria that will be set in the current configuration
	 */
	public void setFilterCriteria(JsonObject filterCriteria) {
		addNotNullProperty(FILTER_CRITERIA_KEY, filterCriteria);
	}

	/**
	 * Retrieves 'limit' property from the current configuration. If there is no limit in defined in the configuration
	 * <code>-1</code> will be returned.
	 *
	 * @return integer value for the comments limit
	 */
	public int getLimit() {
		JsonElement element = getConfiguration().get(LIMIT_KEY);
		return element == null ? -1 : element.getAsInt();
	}

	/**
	 * Retrieves 'status' property from the current configuration. If there is no status property defined in the
	 * configuration <code>null</code> will be returned.
	 *
	 * @return the value of the property with key 'status'
	 */
	public String getStatus() {
		return getProperty(STATUS_KEY, String.class);
	}

	/**
	 * Retrieves 'text' property from the current configuration. If there is no status property defined in the
	 * configuration <code>null</code> will be returned.
	 *
	 * @return the value of the property with key 'text'
	 */
	public String getText() {
		return getProperty(TEXT_KEY, String.class);
	}

}
