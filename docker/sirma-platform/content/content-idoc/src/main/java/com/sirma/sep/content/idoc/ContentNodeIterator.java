package com.sirma.sep.content.idoc;

import java.util.Iterator;

/**
 * Iterator for ContentNodes.
 *
 * @param <C>
 *            the generic type
 */
final class ContentNodeIterator<C extends ContentNode> implements Iterator<C> {
	private final Iterator<C> iterator;
	private C lastNode;

	/**
	 * Instantiates a new content node iterator.
	 *
	 * @param iterator
	 *            the iterator
	 */
	public ContentNodeIterator(Iterator<C> iterator) {
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public C next() {
		lastNode = iterator.next();
		return lastNode;
	}

	@Override
	public void remove() {
		if (lastNode != null) {
			lastNode.remove();
		}
		iterator.remove();
	}
}