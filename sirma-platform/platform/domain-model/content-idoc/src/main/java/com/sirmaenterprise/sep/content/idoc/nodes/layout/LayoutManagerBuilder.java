package com.sirmaenterprise.sep.content.idoc.nodes.layout;

import org.jsoup.nodes.Element;

import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.ContentNodeBuilder;
import com.sirmaenterprise.sep.content.idoc.Layout;

/**
 * The Class LayoutManagerBuilder builder class for LayoutManagerNode.
 * 
 * @author Hristo Lungov
 */
public class LayoutManagerBuilder implements ContentNodeBuilder {

	@Override
	public boolean accept(Element element) {
		return element.hasClass(Layout.LAYOUT_MANAGER_CLASS);
	}

	@Override
	public ContentNode build(Element element) {
		return new LayoutManagerNode(element);
	}

}
