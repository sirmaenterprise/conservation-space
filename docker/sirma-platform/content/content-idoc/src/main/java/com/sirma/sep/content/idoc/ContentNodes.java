package com.sirma.sep.content.idoc;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.select.Elements;

/**
 * Represents a collection of nodes that form a {@link SectionNode}
 *
 * @author BBonev
 */
public class ContentNodes implements Iterable<ContentNode> {
	private final Elements contentElements;
	private final List<ContentNode> items;

	/**
	 * Instantiates a new content nodes.
	 *
	 * @param contentElements
	 *            the content elements
	 */
	public ContentNodes(Elements contentElements) {
		this.contentElements = contentElements;
		items = parse();

	}

	private List<ContentNode> parse() {
		return contentElements.stream().map(ContentNodeFactory.getInstance()::getContentItem).collect(Collectors.toList());
	}

	/**
	 * Gets a mutable collection of all nodes
	 *
	 * @return the nodes
	 */
	public List<ContentNode> getNodes() {
		return items;
	}

	/**
	 * Removes all content nodes from the IDOC
	 */
	public void remove() {
		items.forEach(ContentNode::remove);
		items.clear();
	}

	/**
	 * Mutable iterator for all content nodes.
	 *
	 * @return the iterator
	 */
	@Override
	public Iterator<ContentNode> iterator() {
		return new ContentNodeIterator<>(items.iterator());
	}

}
