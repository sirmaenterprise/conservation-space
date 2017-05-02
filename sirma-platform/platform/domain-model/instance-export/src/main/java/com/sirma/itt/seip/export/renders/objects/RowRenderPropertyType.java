package com.sirma.itt.seip.export.renders.objects;

/**
 * The Enum PropertyTypes used to specify RowRenderProperty type.
 * 
 * @author Hristo Lungov
 */
public enum RowRenderPropertyType {

	HYPERLINK("hyperlink"), TEXT("text"), HIDDEN("hidden");

	String type;

	/**
	 * Instantiates a new property types.
	 *
	 * @param type
	 *            the type
	 */
	private RowRenderPropertyType(String type) {
		this.type = type;
	}
}
