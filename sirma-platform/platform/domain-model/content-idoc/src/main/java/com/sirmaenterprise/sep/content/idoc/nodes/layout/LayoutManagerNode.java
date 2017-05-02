package com.sirmaenterprise.sep.content.idoc.nodes.layout;

import org.jsoup.nodes.Element;

import com.sirmaenterprise.sep.content.idoc.ContentNode;

/**
 * The Class LayoutManagerNode represents the layouts in iDoc, layouts are like inner iDocs, where you can add text and
 * widgets.
 *
 * @author Hristo Lungov
 */
public class LayoutManagerNode extends LayoutNode {

	/**
	 * Instantiates a new layout manager node.
	 *
	 * @param node
	 *            the DOM element to wrap with this {@link ContentNode} implementation.
	 */
	public LayoutManagerNode(Element node) {
		super(node);
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
		return node.hasClass(LAYOUT_MANAGER_CLASS);
	}

}
