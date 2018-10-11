package com.sirma.sep.content.idoc.nodes.layout;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Layout;

/**
 * The Class LayoutNodeBuilder.
 * 
 * @author Hristo Lungov
 */
public class LayoutNodeBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasClass(Layout.LAYOUT_COLUMN_CLASS);
	}

	@Override
	public ContentNode build(Element element) {
		return new LayoutNode(element);
	}

}
