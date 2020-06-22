package com.sirma.itt.seip.expressions.conditions;

/**
 * Enum for conditions RenderAsType
 */
public enum ConditionType {

	/** The hidden. */
	HIDDEN("HIDDEN"),

	/** The required. */
	REQUIRED("REQUIRED"),

	/** The readonly. */
	READONLY("READONLY"),

	/** The optional. */
	OPTIONAL("OPTIONAL"),

	DISABLE_SAVE("DISABLE_SAVE"),

	/** The mandatory. */
	MANDATORY("MANDATORY");

	private String renderAsType;

	/**
	 * Instantiates a new condition render types.
	 *
	 * @param conditionType
	 *            the condition type
	 */
	ConditionType(String renderAsType) {
		this.renderAsType = renderAsType;
	}

	/**
	 * Gets the render as type.
	 *
	 * @return the render as type
	 */
	public String getRenderAs() {
		return renderAsType;
	}

}