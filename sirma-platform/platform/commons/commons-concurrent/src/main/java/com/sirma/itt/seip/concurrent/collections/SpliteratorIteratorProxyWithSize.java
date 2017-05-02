/**
 *
 */
package com.sirma.itt.seip.concurrent.collections;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;

/**
 * {@link Spliterator} proxy of {@link Iterator} interface then the size is known but only means of accessing the
 * elements is via {@link Iterator}.
 * <p>
 * Note that if the size is unknown better use {@link Spliterators#spliteratorUnknownSize(Iterator, int)}
 *
 * @author BBonev
 * @param <T>
 *            the generic type
 */
public class SpliteratorIteratorProxyWithSize<T> extends AbstractSpliterator<T> {

	private final Iterator<T> iterator;

	/**
	 * Instantiates a new spliterator iterator proxy with size.
	 *
	 * @param iterator
	 *            the iterator
	 * @param est
	 *            the est
	 * @param additionalCharacteristics
	 *            the additional characteristics
	 */
	public SpliteratorIteratorProxyWithSize(Iterator<T> iterator, long est, int additionalCharacteristics) {
		super(est, additionalCharacteristics | Spliterator.SIZED);
		this.iterator = iterator;
	}

	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		if (iterator.hasNext()) {
			action.accept(iterator.next());
			return true;
		}
		return false;
	}

}
