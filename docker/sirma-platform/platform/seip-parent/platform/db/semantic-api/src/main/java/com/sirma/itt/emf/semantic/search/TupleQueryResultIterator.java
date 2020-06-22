package com.sirma.itt.emf.semantic.search;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;

import com.sirma.itt.seip.concurrent.collections.FixedBatchSpliteratorBase;

/**
 * Acts as a proxy for the {@link TupleQueryResult} that implements a {@link Spliterator} of fixed batch size
 * {@link FixedBatchSpliteratorBase} and {@link Iterator}.
 *
 * @author BBonev
 */
public class TupleQueryResultIterator extends FixedBatchSpliteratorBase<BindingSet>
		implements Iterator<BindingSet>, TupleQueryResult, Iterable<BindingSet>, AutoCloseable {

	private static final int CHARACTERISTICS = Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE;
	private final TupleQueryResult source;
	private int count;
	private Boolean hasNext;
	
	/**
	 * Instantiates a new tuple query result iterator.
	 *
	 * @param source
	 *            the source
	 */
	public TupleQueryResultIterator(TupleQueryResult source) {
		this(source, 512);
	}

	/**
	 * Instantiates a new tuple query result iterator.
	 *
	 * @param source
	 *            the source
	 * @param batchSize
	 *            the batch size
	 */
	public TupleQueryResultIterator(TupleQueryResult source, int batchSize) {
		super(CHARACTERISTICS, batchSize);
		this.source = source;
	}

	@Override
	public boolean hasNext() {
		hasNext = source.hasNext();
		return hasNext;
		// THIS SHOULD NOT CATCH ANY EXCEPTION
		// Catching exception here may causes partial data received from the repository
	}

	@Override
	public BindingSet next() {
		if (hasNext == null) {
			hasNext = hasNext();
		}
		if (hasNext) {
			count++;
			return source.next();
		}
		throw new NoSuchElementException("No more results in the TupleQueryResult");
	}

	/**
	 * Creates a stream from the current instance. This may be called once per instance.
	 *
	 * @param isParallel
	 *            if the returned stream should be parallel
	 * @return the stream
	 */
	public Stream<BindingSet> stream(boolean isParallel) {
		return StreamSupport.stream(this, isParallel).onClose(this::close);
	}

	@Override
	public void close() {
		source.close();
	}

	@Override
	public void remove() {
		source.remove();
	}

	@Override
	public List<String> getBindingNames() {
		return source.getBindingNames();
	}

	/**
	 * Returns the count of the entities in this iterator after it is processed
	 *
	 * @return The count of the entities
	 */
	public int getCount() {
		return count;
	}

	@Override
	public void forEachRemaining(Consumer<? super BindingSet> action) {
		Objects.requireNonNull(action, "Action on forEachRemaining could not be null");
		while (hasNext()) {
			action.accept(next());
		}
	}

	@Override
	public boolean tryAdvance(Consumer<? super BindingSet> action) {
		if (hasNext()) {
			action.accept(next());
			return true;
		}
		return false;
	}

	@Override
	public Iterator<BindingSet> iterator() {
		return this;
	}
}
