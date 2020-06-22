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
		if ("editable".equals(s) || "public".equals(s)) {
			return EDITABLE;
		} else if ("hidden".equals(s)) {
			return HIDDEN;
		} else if ("readonly".equals(s) || "read_only".equals(s) || "protected".equals(s)) {
			return READ_ONLY;
		} else if ("system".equals(s)) {
			return SYSTEM;
		} else if ("delete".equals(s)) {
			return DELETE;
		} else {
			return HIDDEN;
		}
	}
}
