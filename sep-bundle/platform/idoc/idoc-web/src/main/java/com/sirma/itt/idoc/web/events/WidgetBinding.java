package com.sirma.itt.idoc.web.events;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Widget bing by name.
 * 
 * @author yasko
 * 
 */
public class WidgetBinding extends AnnotationLiteral<Widget> implements Widget {

	private String name;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Widget name.
	 */
	public WidgetBinding(String name) {
		this.name = name;
	}

	/**
	 * Getter for the widget name.
	 * @return Widget name.
	 */
	@Override
	public String name() {
		return name;
	}

}
