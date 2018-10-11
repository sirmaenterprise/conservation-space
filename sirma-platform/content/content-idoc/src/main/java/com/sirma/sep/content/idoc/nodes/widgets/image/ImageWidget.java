package com.sirma.sep.content.idoc.nodes.widgets.image;

import org.jsoup.nodes.Element;

import com.google.gson.JsonObject;
import com.sirma.sep.content.idoc.nodes.WidgetNode;

/**
 * Default image widget implementation. It will be used as server side image widget with own configuration and logic.
 * Could be extended further, if needed.
 *
 * @author A. Kunchev
 */
public class ImageWidget extends WidgetNode {

	public static final String NAME = "image-widget";

	private static final String CONTENTEDITABLE_ATTR_KEY = "contenteditable";

	/**
	 * Default widget constructor.
	 *
	 * @param node
	 *            the element representing widget
	 */
	public ImageWidget(Element node) {
		super(node);
	}

	@Override
	protected ImageWidgetConfiguration buildConfig(JsonObject config) {
		return new ImageWidgetConfiguration(this, config);
	}

	@Override
	protected ImageWidgetConfiguration buildConfig(String config) {
		return new ImageWidgetConfiguration(this, config);
	}

	/**
	 * Makes the current widget content editable or uneditable by changing widget attribute. If <code>null</code> is
	 * passed the configuration will be unchanged.
	 *
	 * @param editable
	 *            <code>true</code> if the content should be editable, <code>false</code> otherwise
	 */
	public void setContentEditable(Boolean editable) {
		if (editable != null) {
			addProperty(CONTENTEDITABLE_ATTR_KEY, editable.toString());
		}
	}

}
