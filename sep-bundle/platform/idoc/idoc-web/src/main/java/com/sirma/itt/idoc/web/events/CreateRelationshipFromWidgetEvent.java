package com.sirma.itt.idoc.web.events;

import java.util.Set;

import org.json.JSONObject;

import com.sirma.itt.emf.instance.model.InstanceReference;

/**
 * Event payload object.
 * 
 * @author yasko
 */
public class CreateRelationshipFromWidgetEvent {

	/** The widget name. */
	private final String widgetName;
	/** The widget config. */
	private final JSONObject widgetConfig;
	/** The widget value. */
	private final JSONObject widgetValue;
	/** The from. */
	private final InstanceReference from;
	/** The new linked instances. */
	private final Set<String> newLinkedInstances;

	/**
	 * Constructor.
	 * 
	 * @param widgetName
	 *            Widget name.
	 * @param widgetConfig
	 *            Widget configuration.
	 * @param widgetValue
	 *            Widget specific configuration, instance references, etc.
	 * @param from
	 *            Instance from which to create the relationship.
	 * @param newLinkedInstances
	 *            the new linked instances
	 */
	public CreateRelationshipFromWidgetEvent(String widgetName, JSONObject widgetConfig,
			JSONObject widgetValue, InstanceReference from, Set<String> newLinkedInstances) {
		this.widgetName = widgetName;
		this.widgetConfig = widgetConfig;
		this.widgetValue = widgetValue;
		this.from = from;
		this.newLinkedInstances = newLinkedInstances;
	}

	/**
	 * Gets the widget name.
	 * 
	 * @return the widgetName
	 */
	public String getWidgetName() {
		return widgetName;
	}

	/**
	 * Gets the widget config.
	 * 
	 * @return the widgetConfig
	 */
	public JSONObject getWidgetConfig() {
		return widgetConfig;
	}

	/**
	 * Gets the widget value.
	 * 
	 * @return the widgetValue
	 */
	public JSONObject getWidgetValue() {
		return widgetValue;
	}

	/**
	 * Gets the from.
	 * 
	 * @return the from
	 */
	public InstanceReference getFrom() {
		return from;
	}

	/**
	 * Getter method for newLinkedInstances.
	 * 
	 * @return the newLinkedInstances
	 */
	public Set<String> getNewLinkedInstances() {
		return newLinkedInstances;
	}

}
