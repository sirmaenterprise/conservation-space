package com.sirma.sep.content.idoc;

/**
 * Abstraction for IDOC Widget. Provides means of manipulating existing widgets in an IDOC.
 *
 * @author BBonev
 */
public interface Widget extends ContentNode {

	/** The widget configuration attribute. */
	String WIDGET_CONFIG = "config";
	/** The widget name attribute. */
	String WIDGET_NAME = "widget";

	/**
	 * Gets the widget name. ('object-data-widget' for example)
	 *
	 * @return the widget name
	 */
	String getName();

	/**
	 * Gets the widget configuration.
	 *
	 * @return the configuration
	 */
	<C extends WidgetConfiguration> C getConfiguration();

	/**
	 * Sets the configuration.
	 *
	 * @param config
	 *            the new configuration
	 */
	void setConfiguration(String config);
}
