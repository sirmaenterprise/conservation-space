package com.sirma.sep.export.renders.objects;

/**
 * Represent property with name and label.
 *  
 * @author Boyan Tonchev
 *
 */
public class RenderProperty {

	private String name;
	private String label;
	
	/**
	 * Instantiates a new render property.
	 *
	 * @param name the name of property. for example "title" or "compact_header" ...
	 * @param label label which will be displayed.
	 */
	public RenderProperty(String name, String label) { 
		this.name = name;
		this.label = label;
	}
	
	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label
	 *            the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * @return the columnTitle
	 */
	public String getName() {
		return name;
	}
}
