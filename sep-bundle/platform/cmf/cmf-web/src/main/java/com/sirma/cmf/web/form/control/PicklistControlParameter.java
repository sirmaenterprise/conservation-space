package com.sirma.cmf.web.form.control;

/**
 * The Enum PicklistControlParameter.
 * 
 * @author svelikov
 */
public enum PicklistControlParameter {

	// for listbox
	/** The filter name. */
	FILTER_NAME("filterName"),
	// for listbox
	/** The keyword. */
	KEYWORD("keyword"),

	//
	/** The items filter. */
	ITEMS_FILTER("itemsFilter"),

	/** The style class. */
	STYLE_CLASS("styleClass"),

	/** The style. */
	STYLE("style"),

	/** The panel width. */
	PANEL_WIDTH("pklWidth"),

	/** The panel height. */
	PANEL_HEIGHT("pklHeight"),

	/** The show footer buttons. */
	SHOW_FOOTER_BUTTONS("showFooterButtons"),

	/** The mask click. */
	MASK_CLICK("maskClick"),

	/** The functional mode. */
	FUNCTIONAL_MODE("pklMode"),
	/** The element type - group, user,... */
	ITEM_TYPE("itemType"),
	/** The items list. */
	ITEMS_LIST("itemsList"),

	/** The clear on close. */
	CLEAR_ON_CLOSE("clearOnClose"),

	/** The header title. */
	HEADER_TITLE("headerTitle"),

	/** The trigger button title. */
	TRIGGER_BUTTON_TITLE("triggerButtonTitle"),

	/** The ok button title. */
	OK_BUTTON_TITLE("okButtonTitle"),

	/** The cancel button title. */
	CANCEL_BUTTON_TITLE("cancelButtonTitle");

	/** The param. */
	private String param;

	/**
	 * Instantiates a new picklist control parameter.
	 * 
	 * @param param
	 *            the param
	 */
	private PicklistControlParameter(String param) {
		this.param = param;
	}

	/**
	 * Getter method for param.
	 * 
	 * @return the param
	 */
	public String getParam() {
		return param;
	}

	/**
	 * Setter method for param.
	 * 
	 * @param param
	 *            the param to set
	 */
	public void setParam(String param) {
		this.param = param;
	}

	/**
	 * Gets the picklist param.
	 * 
	 * @param requestedParam
	 *            the requested param
	 * @return the picklist param
	 */
	public static PicklistControlParameter getPicklistParam(String requestedParam) {
		PicklistControlParameter[] params = values();
		for (PicklistControlParameter param : params) {
			if (param.name().equals(requestedParam)) {
				return param;
			}
		}

		return null;
	}

	/**
	 * Gets the enum value.
	 * 
	 * @param name
	 *            the name
	 * @return the enum value
	 */
	public static PicklistControlParameter getEnumValue(String name) {
		PicklistControlParameter enumValue = null;
		enumValue = valueOf(name);
		return enumValue;
	}

}
