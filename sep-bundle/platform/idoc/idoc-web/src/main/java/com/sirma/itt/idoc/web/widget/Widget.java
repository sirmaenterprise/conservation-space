package com.sirma.itt.idoc.web.widget;

/**
 * Represents an information about a discovered widget.
 * 
 * @author Adrian Mitev
 */
public final class Widget {

	private final String path;

	private final Boolean containsCSS;

	/**
	 * Initializes fields.
	 * 
	 * @param path
	 *            path to the widget
	 * @param containsCSS
	 *            if true, this widget contains custom styles.
	 */
	public Widget(String path, Boolean containsCSS) {
		this.path = path;
		this.containsCSS = containsCSS;
	}

	/**
	 * Getter method for path.
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Getter method for containsCSS.
	 * 
	 * @return the containsCSS
	 */
	public Boolean getContainsCSS() {
		return containsCSS;
	}

}
