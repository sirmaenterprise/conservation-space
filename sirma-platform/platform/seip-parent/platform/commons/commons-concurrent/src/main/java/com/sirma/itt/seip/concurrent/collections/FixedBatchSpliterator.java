package com.sirma.itt.seip.concurrent.collections;

import static java.util.stream.StreamSupport.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Simple implementation of fixed sized split iterator.
 * <p>
 * Before use read this:
 * <a href= "https://www.airpair.com/java/posts/parallel-processing-of-io-based-data-with-java-streams" > https://www.
 * airpair.com/java/posts/parallel-processing-of-io-based-data-with-java-streams</a>
 *
 * @param <T>
 *            the generic type
 * @author BBonev
 */
public class FixedBatchSpliterator<T> extends FixedBatchSpliteratorBase<T> {

	/** The proxied spliterator that will be processed in batches. */
	private final Spliterator<T> spliterator;

	/**
	 * Instantiates a new fixed batch spliterator that wraps the given iterator and forces the execution if batches of
	 * the given size.
	 *
	 * @param toWrap
	 *            the iterator to wrap
	 * @param batchSize
	 *            the batch size
	 */
	public FixedBatchSpliterator(Spliterator<T> toWrap, int batchSize) {
		super(toWrap.characteristics(), batchSize, toWrap.estimateSize());
		this.spliterator = toWrap;
	}

	/**
	 * Creates new {@link Spliterator} that will wrap the given one and will force it's execution id batches of the
	 * given size.
	 *
	 * @param <T>
	 *            the generic type
	 * @param toWrap
	 *            the to wrap
	 * @param batchSize
	 *            the batch size
	 * @return the fixed batch spliterator
	 */
	public static <T> FixedBatchSpliterator<T> batchedSpliterator(Spliterator<T> toWrap, int batchSize) {
		return new FixedBatchSpliterator<>(toWrap, batchSize);
	}

	/**
	 * Returns a parallel stream that reads the input stream in batches of the given size.
	 *
	 * @param <T>
	 *            the stream elements type
	 * @param in
	 *            the source stream
	 * @param batchSize
	 *            the batch size
	 * @return new parallel stream
	 */
	public static <T> Stream<T> withBatchSize(Stream<T> in, int batchSize) {
		return stream(batchedSpliterator(in.spliterator(), batchSize), true);
	}

	@Override
	public boolean tryAdvance(Consumer<? super T> action) {
		return spliterator.tryAdvance(action);
	}

	@Override
	public void forEachRemaining(Consumer<? super T> action) {
		spliterator.forEachRemaining(action);
	}
}
