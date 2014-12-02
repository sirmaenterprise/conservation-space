package com.sirma.itt.emf.util;

import java.util.Collection;
import java.util.Iterator;

import com.sirma.itt.emf.domain.Link;

/**
 * Implementation for {@link Iterable} interface to provide automatic iteration of a collection of
 * {@link Link}s. When creating new instance provide the source collection of {@link Link}.
 * 
 * @author BBonev
 * @param <E>
 *            the collection element type
 * @param <L>
 *            the link type
 */
public class LinkIterable<E, L extends Link<E, E>> implements Iterable<E>, Collection<E> {

	/** The source. */
	private Collection<L> source;

	/** The is from. */
	private boolean isFrom;

	/**
	 * Instantiates a new link reference iterator that will return {@link Link#getTo()} instances.
	 * 
	 * @param source
	 *            the source collection to iterate
	 */
	public LinkIterable(Collection<L> source) {
		this(source, false);
	}

	/**
	 * Instantiates a new link reference iterator that could iterate over {@link Link#getTo()} or
	 * {@link Link#getFrom()} references.
	 * 
	 * @param source
	 *            the source collection to iterate
	 * @param isFrom
	 *            if <code>true</code> the iterator will return only {@link Link#getFrom()}
	 *            references and if <code>false</code> only {@link Link#getTo()}
	 */
	public LinkIterable(Collection<L> source, boolean isFrom) {
		this.source = source;
		this.isFrom = isFrom;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<E> iterator() {
		return new LinkReferenceIterator<>(source.iterator(), isFrom);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LinkIterable [isFrom=");
		builder.append(isFrom);
		builder.append(", source=");
		builder.append(source);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int size() {
		return source.size();
	}

	@Override
	public boolean isEmpty() {
		return source.isEmpty();
	}

	@Override
	public boolean contains(Object paramObject) {
		return source.contains(paramObject);
	}

	@Override
	public Object[] toArray() {
		return new Object[0];
	}

	@Override
	public <A> A[] toArray(A[] paramArrayOfT) {
		return paramArrayOfT;
	}

	@Override
	public boolean add(E paramE) {
		return false;
	}

	@Override
	public boolean remove(Object paramObject) {
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> paramCollection) {
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> paramCollection) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> paramCollection) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> paramCollection) {
		return false;
	}

	@Override
	public void clear() {
		source.clear();
	}

	/**
	 * Iterator proxy implementation to iterate a iterator of {@link Link}s
	 * 
	 * @author BBonev
	 */
	private static class LinkReferenceIterator<I, T extends Link<I, I>> implements Iterator<I> {

		/** The iterator. */
		private final Iterator<T> iterator;

		/** The from. */
		private final boolean from;

		/**
		 * Instantiates a new link reference iterator.
		 *
		 * @param iterator
		 *            the iterator
		 * @param isFrom
		 *            the is from
		 */
		public LinkReferenceIterator(Iterator<T> iterator, boolean isFrom) {
			this.iterator = iterator;
			from = isFrom;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public I next() {
			T next = iterator.next();
			return from ? next.getFrom() : next.getTo();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove() {
			iterator.remove();
		}
	}
}
