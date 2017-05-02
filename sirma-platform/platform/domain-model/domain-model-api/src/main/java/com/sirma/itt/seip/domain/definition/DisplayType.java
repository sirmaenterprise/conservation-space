package com.sirma.itt.seip.domain.definition;

/**
 * Defines the possible configuration values for displayable element.
 *
 * @author BBonev
 */
public enum DisplayType {

	/** The configured item is visible and can be changed by the user. */
	EDITABLE, /** The read only - visible in edit and preview but not editable. */
	READ_ONLY, /** The hidden. Never visible in edit mode, only in preview mode */
	HIDDEN, /** Always hidden */
	SYSTEM, /** When compiling definitions all elements with this display type will be removed. */
	DELETE;

	/**
	 * Parses the given string to display type value.
	 *
	 * @param value
	 *            the value
	 * @return the display type
	 */
	public static DisplayType parse(String value) {
		if (value == null) {
			return null;
		}
		String s = value.toLowerCase();
		if (s.equals("editable") || s.equals("public")) {
			return EDITABLE;
		} else if (s.equals("hidden")) {
			return HIDDEN;
		} else if (s.equals("readonly") || s.equals("read_only") || s.equals("protected")) {
			return READ_ONLY;
		} else if (s.equals("system")) {
			return SYSTEM;
		} else {
			return HIDDEN;
		}
	}
}
