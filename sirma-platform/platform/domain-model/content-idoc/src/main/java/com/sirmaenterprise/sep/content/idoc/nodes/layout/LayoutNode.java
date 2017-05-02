package com.sirmaenterprise.sep.content.idoc.nodes.layout;

import org.jsoup.nodes.Element;

import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.Layout;
import com.sirmaenterprise.sep.content.idoc.nodes.TextNode;

/**
 * The Class LayoutNode class for each layout column.
 * 
 * @author Hristo Lungov
 */
public class LayoutNode extends TextNode implements Layout {

	/**
	 * Instantiates a new layout node.
	 *
	 * @param node
	 *            the DOM element to wrap with this {@link ContentNode} implementation.
	 */
	public LayoutNode(Element node) {
		super(node);
	}

	@Override
	public boolean isWidget() {
		return false;
	}

	@Override
	public boolean isTextNode() {
		return false;
	}

	@Override
	public boolean isLayout() {
		return node.hasClass(LAYOUT_COLUMN_CLASS);
	}

}
