package com.sirma.sep.content.idoc.nodes;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Widget;

/**
 * Default node builder. When no other builder accepts a node this will be used. The builder produces {@link TextNode}
 * or {@link WidgetNode} depending on the attribute presence {@link Widget#WIDGET_NAME}
 *
 * @author BBonev
 */
public class DefaultNodeBuilder implements ContentNodeBuilder {

	public static final DefaultNodeBuilder INSTANCE = new DefaultNodeBuilder();

	/**
	 * Instantiates a new text node builder.
	 */
	public DefaultNodeBuilder() {
		// no need to be added to cdi
	}

	@Override
	public boolean accept(Element element) {
		return false;
	}

	@Override
	public ContentNode build(Element element) {
		if (element.hasAttr(Widget.WIDGET_NAME)) {
			return new WidgetNode(element);
		}
		return new TextNode(element);
	}

	/**
	 * Gets the single instance of TextNodeBuilder.
	 *
	 * @return single instance of TextNodeBuilder
	 */
	public static DefaultNodeBuilder getInstance() {
		return INSTANCE;
	}

}
