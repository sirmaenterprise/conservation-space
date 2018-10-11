package com.sirma.itt.seip.definition;

import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.definition.label.Displayable;

/**
 * Defines a definition of a transition group in the definitions.
 *
 * @author Adrian Mitev
 */
public interface TransitionGroupDefinition extends Ordinal, Displayable, Identity {

	// TODO define Enum for all definition attributes
	String LABEL_ATTRIBUTE = "label";
	String TOOLTIP_ATTRIBUTE = "tooltip";
	String TYPE_ATTRIBUTE = "type";

	/**
	 * Provides the parent group for the particular group.
	 *
	 * @return the group parent.
	 */
	String getParent();

	/**
	 * Provides the type of the group
	 *
	 * @return type of the group.
	 */
	String getType();

	/**
	 * Null safe returns the given TransitionGroupDefinition's properties in map.
	 *
	 * @param group
	 *            the TransitionGroupDefinition that will be used
	 * @return Map with the TransitionGroupDefinition information
	 */
	static Map<String, String> getProperties(TransitionGroupDefinition group) {
		Map<String, String> map = new HashMap<>(3);
		addNotNullValue(map, LABEL_ATTRIBUTE, group.getLabel());
		addNotNullValue(map, TOOLTIP_ATTRIBUTE, group.getTooltip());
		addNotNullValue(map, TYPE_ATTRIBUTE, group.getType());
		return map;
	}

	/**
	 * Adds not null values to the passed map.
	 *
	 * @param map
	 *            the map to which values will be added
	 * @param key
	 *            the key to which the value will be assigned
	 * @param value
	 *            the value to be added
	 */
	static void addNotNullValue(Map<String, String> map, String key, String value) {
		if (value == null) {
			return;
		}

		map.put(key, value);
	}
}