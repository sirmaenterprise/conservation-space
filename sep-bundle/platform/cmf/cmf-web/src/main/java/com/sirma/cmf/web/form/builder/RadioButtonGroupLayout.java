package com.sirma.cmf.web.form.builder;

/**
 * The Enum RadioButtonGroupLayout.
 * 
 * @author svelikov
 */
public enum RadioButtonGroupLayout {

	/** The page direction. */
	PAGE_DIRECTION("pageDirection"),

	/** The line direction. */
	LINE_DIRECTION("lineDirection");

	/** The layout. */
	private String layout;

	/**
	 * Instantiates a new layout.
	 * 
	 * @param layout
	 *            the layout
	 */
	private RadioButtonGroupLayout(String layout) {
		this.layout = layout;
	}

	/**
	 * Getter method for layout.
	 * 
	 * @return the layout
	 */
	public String getLayout() {
		return layout;
	}

	/**
	 * Setter method for layout.
	 * 
	 * @param layout
	 *            the layout to set
	 */
	public void setLayout(String layout) {
		this.layout = layout;
	}

	/**
	 * Gets the layout type.
	 * 
	 * @param layoutName
	 *            the layout name
	 * @return the layout
	 */
	public static RadioButtonGroupLayout getLayoutType(String layoutName) {
		RadioButtonGroupLayout[] layouts = values();
		for (RadioButtonGroupLayout layout : layouts) {
			if (layout.layout.equals(layoutName)) {
				return layout;
			}
		}

		return null;
	}

}
