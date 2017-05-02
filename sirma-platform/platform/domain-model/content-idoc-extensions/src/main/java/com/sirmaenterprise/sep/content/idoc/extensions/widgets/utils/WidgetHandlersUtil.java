package com.sirmaenterprise.sep.content.idoc.extensions.widgets.utils;

import java.io.Serializable;
import java.util.Collection;

import com.sirmaenterprise.sep.content.idoc.WidgetConfiguration;

/**
 * Utility class for widget handlers common logic.
 *
 * @author A. Kunchev
 */
public class WidgetHandlersUtil {

	private WidgetHandlersUtil() {
		// FSS
	}

	/**
	 * Sets passed ids in the widget configurations based on that, which property is set in the configuration -
	 * 'selectedObject' or 'selectedObjects'.
	 *
	 * @param ids
	 *            the ids that should be set in the configuration
	 * @param configuration
	 *            the configuration in which the ids will be set
	 */
	public static void setObjectIdsToConfiguration(Collection<Serializable> ids, WidgetConfiguration configuration) {
		if (configuration.hasSelectedObject()) {
			configuration.setSelectedObject(ids.iterator().next());
		} else if (configuration.hasSelectedObjects()) {
			configuration.setSelectedObjects(ids);
		} else {
			// this should be removed, when the widgets configurations are migrated and unified, because there are
			// to many differences between the widgets and how they work at the moment ...
			setObjectIdsByWidgetSelection(ids, configuration);
		}
	}

	/**
	 * Sets version ids depending on 'selection' property in the widget configuration. Some widgets don't have
	 * 'selectedObjects' or 'selectedObject' property, so we are using this to check, which of both should be set.
	 * <p>
	 * TODO we need to create generic widget configuration(web & backed) that handles all cases for all widgets, so that
	 * we could simplify the widgets handling
	 */
	private static void setObjectIdsByWidgetSelection(Collection<Serializable> ids, WidgetConfiguration configuration) {
		String selection = configuration.getProperty("selection", String.class);
		if ("single".equalsIgnoreCase(selection) && ids.size() == 1) {
			configuration.setSelectedObject(ids.iterator().next());
		} else {
			configuration.setSelectedObjects(ids);
		}
	}

}
