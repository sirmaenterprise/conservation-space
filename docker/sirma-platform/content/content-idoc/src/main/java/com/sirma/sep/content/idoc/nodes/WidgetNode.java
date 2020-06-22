package com.sirma.sep.content.idoc.nodes;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import org.jsoup.nodes.Element;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Widget;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Default implementation of {@link Widget}, it could be used for base widget implementations. Supports widget and non
 * widget nodes.
 *
 * @author BBonev
 */
public class WidgetNode extends TextNode implements Widget {

	private WidgetConfiguration configuration;

	/**
	 * Instantiates a new WidgetNode.
	 *
	 * @param node
	 *            the DOM element to wrap with this {@link ContentNode} implementation.
	 */
	public WidgetNode(Element node) {
		super(node);
	}

	@Override
	public boolean isWidget() {
		return node.hasAttr(WIDGET_NAME);
	}

	@Override
	public boolean isLayout() {
		return false;
	}

	@Override
	public String getName() {
		if (node.hasAttr(WIDGET_NAME)) {
			return node.attr(WIDGET_NAME);
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <C extends WidgetConfiguration> C getConfiguration() {
		if (configuration == null) {
			if (node.hasAttr(WIDGET_CONFIG)) {
				configuration = buildConfig(node.attr(WIDGET_CONFIG));
			} else {
				// create empty configuration instance
				configuration = buildConfig(new JsonObject());
			}
		}
		return (C) configuration;
	}

	/**
	 * Builds new {@link WidgetConfiguration} object with provided configuration as string and the current widget.
	 *
	 * @param config
	 *            widget configuration as string
	 * @return {@link WidgetConfiguration}
	 */
	protected WidgetConfiguration buildConfig(String config) {
		return new WidgetConfiguration(this, config);
	}

	/**
	 * Builds new {@link WidgetConfiguration} object with provided configuration as json and the current widget.
	 *
	 * @param config
	 *            widget configuration as json
	 * @return {@link WidgetConfiguration}
	 */
	protected WidgetConfiguration buildConfig(JsonObject config) {
		return new WidgetConfiguration(this, config);
	}

	@Override
	public void setConfiguration(String config) {
		if (node.hasAttr(WIDGET_CONFIG)) {
			node.attr(WIDGET_CONFIG, getOrDefault(config, "{}"));
		}
	}

	@Override
	public void remove() {
		super.remove();
		configuration = null;
	}

	@Override
	public boolean isTextNode() {
		return false;
	}
}
