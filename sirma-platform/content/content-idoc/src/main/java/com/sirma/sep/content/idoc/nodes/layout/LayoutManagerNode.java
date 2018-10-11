package com.sirma.sep.content.idoc.nodes.layout;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;

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

	/**
	 * Selects top most container since layouts might contain wrapper with either class 'layoutmanager' or
	 * 'layout-container', or just 'layoutmanager' class, or just 'layout-container'. In order to avoid duplication of
	 * layouts in export all cases must be taken into account {@inheritDoc}
	 */
	@Override
	public boolean isLayoutManager() {
		if (node.hasClass(LAYOUT_MANAGER_CLASS) && !node.children().isEmpty()
				&& node.child(0).hasClass(LAYOUT_MANAGER_CONTAINER_CLASS)) {
			return true;
		} else if (node.hasClass(LAYOUT_MANAGER_CLASS) && !node.children().isEmpty()
				&& !node.child(0).hasClass(LAYOUT_MANAGER_CONTAINER_CLASS)) {
			return true;
		} else if (node.hasClass(LAYOUT_MANAGER_CONTAINER_CLASS)) {
			// do not match child of already wrapped 'layoutmanager' layout
			return !node.parent().hasClass(LAYOUT_MANAGER_CLASS);
		}
		return false;
	}

}
