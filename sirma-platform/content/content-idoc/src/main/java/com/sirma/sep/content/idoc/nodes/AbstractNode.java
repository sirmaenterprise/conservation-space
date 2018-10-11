package com.sirma.sep.content.idoc.nodes;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import org.jsoup.nodes.Element;

import com.sirma.sep.content.idoc.ContentNode;

/**
 * Represents base implementation of {@link ContentNode} to provide generic property retrieval and modifications over
 * the nodes.
 *
 * @author BBonev
 */
public abstract class AbstractNode implements ContentNode {

	protected final Element node;

	/**
	 * Instantiate new instance that wraps the given {@link Element}
	 * 
	 * @param node
	 *            the node to wrap
	 */
	public AbstractNode(Element node) {
		this.node = node;
	}

	@Override
	public String getId() {
		if (node.hasAttr(ELEMENT_ID)) {
			return node.attr(ELEMENT_ID);
		}
		return null;
	}

	@Override
	public void setId(String id) {
		if (node.hasAttr(ELEMENT_ID)) {
			node.attr(ELEMENT_ID, getOrDefault(id, ""));
		}
	}

	@Override
	public String getProperty(String key) {
		if (node.hasAttr(key)) {
			return node.attr(key);
		}
		return null;
	}

	@Override
	public boolean setProperty(String key, String value) {
		if (node.hasAttr(key)) {
			node.attr(key, value);
			return true;
		}
		return false;
	}

	@Override
	public void addProperty(String key, String value) {
		node.attr(key, value);
	}

	@Override
	public void removeProperty(String key) {
		node.removeAttr(key);
	}

	@Override
	public void remove() {
		node.remove();
	}

	@Override
	public Element getElement() {
		return node;
	}

}