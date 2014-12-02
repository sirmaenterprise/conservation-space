package com.sirma.itt.emf.web.treeHeader;

/**
 * The size of the tree header icons.
 * 
 * @author svelikov
 */
public enum Size {

	/** The small. */
	SMALL("small", 16),
	/** The medium. */
	MEDIUM("medium", 24),
	/** The big. */
	BIG("big", 32),
	/** The bigger. */
	BIGGER("bigger", 64);

	/** The size. */
	private String size;

	/** The icon size. */
	private int iconSize;

	/**
	 * Instantiates a new size.
	 * 
	 * @param size
	 *            the size
	 * @param iconSize
	 *            the icon size
	 */
	private Size(String size, int iconSize) {
		this.size = size;
		this.iconSize = iconSize;
	}

	/**
	 * Getter method for size.
	 * 
	 * @return the size
	 */
	public String getSize() {
		return size;
	}

	/**
	 * Setter method for size.
	 * 
	 * @param size
	 *            the size to set
	 */
	public void setSize(String size) {
		this.size = size;
	}

	/**
	 * Getter method for iconSize.
	 * 
	 * @return the iconSize
	 */
	public int getIconSize() {
		return iconSize;
	}

	/**
	 * Setter method for iconSize.
	 * 
	 * @param iconSize
	 *            the iconSize to set
	 */
	public void setIconSize(int iconSize) {
		this.iconSize = iconSize;
	}

	/**
	 * Gets the size by string.
	 * 
	 * @param size
	 *            the size
	 * @return the size by string
	 */
	public static Size getSizeByString(String size) {
		Size[] params = values();
		for (Size param : params) {
			if (param.name().equalsIgnoreCase(size)) {
				return param;
			}
		}

		return null;
	}

	/**
	 * Gets the icon size value.
	 * 
	 * @param size
	 *            the size
	 * @return the icon size value
	 */
	public static int getIconSizeValue(String size) {
		Size[] params = values();
		for (Size param : params) {
			if (param.name().equalsIgnoreCase(size)) {
				return param.iconSize;
			}
		}

		return 0;
	}

}
