package com.sirma.sep.content.idoc;

import org.apache.commons.lang3.StringUtils;

/**
 * Contains all supported selection modes for the currently supported widgets.
 *
 * @author A. Kunchev
 */
public enum WidgetSelectionMode {

	MANUALLY,

	AUTOMATICALLY,

	CURRENT;

	/**
	 * Gets {@link WidgetSelectionMode} by name.
	 *
	 * @param mode
	 *            the name of the mode which should be returned
	 * @return {@link WidgetSelectionMode} if found such, otherwise <code>null</code>
	 */
	public static WidgetSelectionMode getMode(String mode) {
		if (StringUtils.isBlank(mode)) {
			return null;
		}

		for (WidgetSelectionMode element : values()) {
			if (element.toString().equalsIgnoreCase(mode)) {
				return element;
			}
		}

		return null;
	}

}
