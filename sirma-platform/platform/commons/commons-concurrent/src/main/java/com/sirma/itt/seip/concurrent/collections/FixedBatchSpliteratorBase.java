/**
 *
 */
package com.sirma.itt.seip.concurrent.collections;

import static java.util.Spliterators.spliterator;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Split iterator that provides data a parallel stream processing with fixed batches
 * <p>
 * Copied from
 * <a href= "https://www.airpair.com/java/posts/parallel-processing-of-io-based-data-with-java-streams" > https://www.
 * airpair.com/java/posts/parallel-processing-of-io-based-data-with-java-streams</a>
 * <p>
 * Note: This should be used when parallelism is required but the elements are not enough (less than 1024) to trigger
 * with certainty the parallel processing. The batch size should be chosen depending on expected elements count so that
 * all actual batches to have the optimal number of elements to be run on current number of cores
 *
 * @param <T>
 *            the generic type
 * @author BBonev
 */
public abstract class FixedBatchSpliteratorBase<T> implements Spliterator<T> {

	/** The batch size. */
	private final int batchSize;

	/** The characteristics. */
	private final int characteristics;
	/** Size estimations. */
	private long est;

	/**
	 * Instantiates a new fixed batch spliterator base.
	 *
	 * @param characteristics
	 *            the characteristics
	 * @param batchSize
	 *            the batch size
	 * @param est
	 *            the est
	 */
	public FixedBatchSpliteratorBase(int characteristics, int batchSize, long est) {
		if (batchSize <= 0) {
			throw new IllegalArgumentException("The batch size should be greater than zero");
		}
		this.characteristics = characteristics | SUBSIZED;
		this.batchSize = batchSize;
		this.est = est;
	}

	/**
	 * Instantiates a new fixed batch spliterator base.
	 *
	 * @param characteristics
	 *            the characteristics
	 * @param batchSize
	 *            the batch size
	 */
	public FixedBatchSpliteratorBase(int characteristics, int batchSize) {
		this(characteristics, batchSize, Long.MAX_VALUE);
	}

	/**
	 * Instantiates a new fixed batch spliterator base.
	 *
	 * @param characteristics
	 *            the characteristics
	 */
	public FixedBatchSpliteratorBase(int characteristics) {
		this(characteristics, 128, Long.MAX_VALUE);
	}

	/**
	 * Instantiates a new fixed batch spliterator base.
	 */
	public FixedBatchSpliteratorBase() {
		this(IMMUTABLE | ORDERED | NONNULL);
	}

	@Override
	public Spliterator<T> trySplit() {
		final HoldingConsumer<T> holder = new HoldingConsumer<>();
		if (!tryAdvance(holder)) {
			return null;
		}
		final Object[] a = new Object[batchSize];
		int j = 0;
		do {
			a[j] = holder.value;
		} while (++j < batchSize && tryAdvance(holder));
		if (est != Long.MAX_VALUE) {
			est -= j;
		}
		return spliterator(a, 0, j, characteristics() | SIZED);
	}

	@Override
	public Comparator<? super T> getComparator() {
		if (hasCharacteristics(SORTED)) {
			return null;
		}
		throw new IllegalStateException();
	}

	@Override
	public long estimateSize() {
		return est;
	}

	@Override
	public int characteristics() {
		return characteristics;
	}

	/**
	 * Consumer that stores the last accepted value
	 *
	 * @param <T>
	 *            the generic type
	 */
	static final class HoldingConsumer<T> implements Consumer<T> {

		/** The value. */
		Object value;

		@Override
		public void accept(T toHold) {
			this.value = toHold;
		}
	}
}