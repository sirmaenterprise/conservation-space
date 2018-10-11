package com.sirma.sep.export.renders.html.table;

/**
 * Interface for all html builders.
 * 
 * @author Boyan Tonchev
 */
public interface HtmlBuilder {

	/**
	 * Add attribute to tag. For example: name -> "style", value -> "color:red"
	 * 
	 * @param name
	 *            - name of attribute.
	 * @param value
	 *            - value of attribute.
	 */
	void addAttribute(String name, String value);

	/**
	 * Add additional style.
	 * 
	 * @param style
	 *            to be added.
	 */
	void addStyle(String style);

	/**
	 * Add class to element.
	 * 
	 * @param className
	 */
	void addClass(String className);

}
