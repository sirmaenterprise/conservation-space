package com.sirma.sep.content.idoc.extensions.widgets.utils;

import org.jsoup.nodes.Element;

import com.google.gson.JsonParser;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Widget implementation for test purposes only.
 *
 * @author A. Kunchev
 */
public class WidgetMock implements Widget {

	private WidgetConfiguration configuration;

	/**
	 * Initialize new widget mock.
	 */
	public WidgetMock() {
		// default
	}

	/**
	 * Initialize new widget mock with configuration.
	 *
	 * @param newConfiguration
	 *            {@link WidgetConfiguration} that should be set for the widget
	 */
	public WidgetMock(WidgetConfiguration newConfiguration) {
		configuration = newConfiguration;
	}

	@Override
	public boolean isWidget() {
		return false;
	}

	@Override
	public boolean isLayout() {
		return false;
	}

	@Override
	public boolean isTextNode() {
		return false;
	}

	@Override
	public boolean isLayoutManager() {
		return false;
	}

	@Override
	public String getProperty(String key) {
		return null;
	}

	@Override
	public boolean setProperty(String key, String value) {
		return false;
	}

	@Override
	public void addProperty(String key, String value) {
		// empty
	}

	@Override
	public void removeProperty(String key) {
		// empty
	}

	@Override
	public void remove() {
		// empty
	}

	@Override
	public Element getElement() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <C extends WidgetConfiguration> C getConfiguration() {
		return (C) configuration;
	}

	@Override
	public void setConfiguration(String config) {
		configuration = new WidgetConfiguration(this, new JsonParser().parse(config).getAsJsonObject());
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public void setId(String id) {
		// empty
	}
}