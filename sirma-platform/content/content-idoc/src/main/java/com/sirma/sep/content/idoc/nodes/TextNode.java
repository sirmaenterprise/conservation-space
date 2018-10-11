package com.sirma.sep.content.idoc.nodes;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;

/**
 * Default {@link ContentNode} implementation. Will be used to represent every tag/element which have no other specific
 * implemenation.
 *
 * @author Hristo Lungov
 */
public class TextNode extends AbstractNode {

	/**
	 * Instantiates a new text node.
	 *
	 * @param node
	 *            the DOM element to wrap with this {@link ContentNode} implementation.
	 */
	public TextNode(Element node) {
		super(node);
	}

	@Override
	public boolean isTextNode() {
		return node.ownText().length() > 0;
	}
}
