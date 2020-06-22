package com.sirma.sep.export.renders.objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class RenderProperty used to represent instance property.
 */
public class RowRenderProperty extends RenderProperty {

	private Map<String, Serializable> values = new HashMap<>(1);
	private RowRenderPropertyType type;

	/**
	 * Instantiates a new row render property.
	 *
	 * @param name
	 *            the name of property. for example "title" or "compact_header" ...
	 * @param label
	 *            label which will be displayed.
	 */
	public RowRenderProperty(String name, String label) {
		super(name, label);
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public RowRenderPropertyType getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the new type
	 */
	public void setType(RowRenderPropertyType type) {
		this.type = type;
	}

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public Map<String, Serializable> getValues() {
		return values;
	}

	/**
	 * Adds the value.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void addValue(String key, Serializable value) {
		values.put(key, value);
	}

	/**
	 * Checks if is hyper link.
	 *
	 * @return true, if is hyper link
	 */
	public boolean isHyperLink() {
		return RowRenderPropertyType.HYPERLINK.equals(type);
	}

	/**
	 * Checks if is html.
	 *
	 * @return true, if is html field
	 */
	public boolean isHtml() {
		return RowRenderPropertyType.HTML.equals(type);
	}

	/**
	 * Checks if is hidden type row.
	 *
	 * @return true, if is hyper link
	 */
	public boolean isHidden() {
		return RowRenderPropertyType.HIDDEN.equals(type);
	}
}