/**
 *
 */
package com.sirma.itt.emf.semantic.search;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.collections.FixedBatchSpliteratorBase;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Acts as a proxy for the {@link TupleQueryResult} that implements a {@link Spliterator} of fixed batch size
 * {@link FixedBatchSpliteratorBase} and {@link Iterator}.
 *
 * @author BBonev
 */
public class TupleQueryResultIterator extends FixedBatchSpliteratorBase<BindingSet>
		implements Iterator<BindingSet>, TupleQueryResult, Iterable<BindingSet>, AutoCloseable {

	private static final int CHARACTERISTICS = Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE;
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final TupleQueryResult source;
	private int count;
	
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
		try {
			return source.hasNext();
		} catch (QueryEvaluationException e) {
			LOGGER.warn("", e);
			return false;
		}
	}

	@Override
	public BindingSet next() {
		try {
			count++;
			return source.next();
		} catch (QueryEvaluationException e) {
			LOGGER.warn("", e);
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
		try {
			source.close();
		} catch (QueryEvaluationException e) {
			throw new EmfRuntimeException(e);
		}
	}

	@Override
	public void remove() {
		try {
			source.remove();
		} catch (QueryEvaluationException e) {
			LOGGER.warn("", e);
		}
	}

	@Override
	public List<String> getBindingNames() throws QueryEvaluationException {
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
