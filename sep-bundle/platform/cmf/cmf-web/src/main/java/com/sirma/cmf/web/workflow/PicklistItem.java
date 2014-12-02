package com.sirma.cmf.web.workflow;

/**
 * The Class PicklistItem.
 */
public class PicklistItem {

	/** The id. */
	private long id;

	/** The item label. */
	private String itemLabel;

	/** The item value. */
	private String itemValue;

	/** icon location. */
	private String iconPath;

	/** The type of the item. */
	private String type;

	/**
	 * Instantiates a new picklist item.
	 * 
	 * @param id
	 *            the id
	 * @param itemLabel
	 *            the item label
	 * @param itemValue
	 *            the item value
	 * @param type
	 *            the type
	 */
	public PicklistItem(long id, String itemLabel, String itemValue, String type) {
		this.itemLabel = itemLabel;
		this.itemValue = itemValue;
		this.id = id;
		this.type = type;
	}

	/**
	 * Instantiates a new picklist item.
	 * 
	 * @param itemValue
	 *            the item value
	 */
	public PicklistItem(String itemValue) {
		this.itemValue = itemValue;
	}

	/**
	 * Getter method for id.
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Getter method for itemValue.
	 * 
	 * @return the itemValue
	 */
	public String getItemValue() {
		return itemValue;
	}

	/**
	 * Setter method for id.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Setter method for itemValue.
	 * 
	 * @param itemValue
	 *            the itemValue to set
	 */
	public void setItemValue(String itemValue) {
		this.itemValue = itemValue;
	}

	/**
	 * Getter method for itemLabel.
	 * 
	 * @return the itemLabel
	 */
	public String getItemLabel() {
		return itemLabel;
	}

	/**
	 * Setter method for itemLabel.
	 * 
	 * @param itemLabel
	 *            the itemLabel to set
	 */
	public void setItemLabel(String itemLabel) {
		this.itemLabel = itemLabel;
	}

	/**
	 * @return the iconPath
	 */
	public String getIconPath() {
		return iconPath;
	}

	/**
	 * @param iconPath
	 *            the iconPath to set
	 */
	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	/**
	 * Getter method for type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter method for type.
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "PicklistItem [id=" + id + ", itemLabel=" + itemLabel + ", itemValue=" + itemValue
				+ ", iconPath=" + iconPath + ", type=" + type + "]";
	}

}
