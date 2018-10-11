package com.sirma.sep.content.idoc.nodes.layout;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.Layout;

/**
 * The Class LayoutManagerBuilder builder class for LayoutManagerNode.
 *
 * @author Hristo Lungov
 */
public class LayoutManagerBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasClass(Layout.LAYOUT_MANAGER_CLASS) || element.hasClass(Layout.LAYOUT_MANAGER_CONTAINER_CLASS);
	}

	@Override
	public ContentNode build(Element element) {
		return new LayoutManagerNode(element);
	}

}
