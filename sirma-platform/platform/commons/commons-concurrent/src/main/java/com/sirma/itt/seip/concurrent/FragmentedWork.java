package com.sirma.itt.seip.concurrent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Helper class to run data processing on fragments. Useful when executing queries to load huge amount of data. The
 * implementer source provide the data to be fragmented and the fragment size. The implementation will call an abstract
 * method with the data fragment to be processed.<br>
 * The implementation is done with a class to that the user could pass any number of arguments to use in the actual
 * processing method.
 * <p>
 * For future implementation could provide option for parallel job execution.
 *
 * @author BBonev
 */
public class FragmentedWork {

	/**
	 * Instantiates a new fragmented work.
	 */
	private FragmentedWork() {
		// utility class
	}

	/**
	 * Compute the batch size so that for the given data the methods in this class will produce the given number of
	 * fragments.
	 *
	 * @param inputData
	 *            the size of the input data that will be processed
	 * @param expectedFragments
	 *            the expected fragments count. Must be greater than 0
	 * @return the suggested batch size that will produce the given number of batches
	 */
	public static int computeBatchForNFragments(int inputData, int expectedFragments) {
		if (expectedFragments <= 0) {
			throw new IllegalArgumentException("Invalid fragments number. Must be greater than 0");
		}
		if (inputData > expectedFragments) {
			return (int) BigDecimal
					.valueOf((double) inputData / (double) expectedFragments)
						.setScale(0, RoundingMode.UP)
						.floatValue();
		}
		// the fragment size will be less than 1 element per fragment so limit it to 1
		return 1;
	}

	/**
	 * Splits the data in chunks and calls the consumer with each chunk.
	 *
	 * @param <I>
	 *            the generic type
	 * @param data
	 *            the data
	 * @param fragmentSize
	 *            the fragment size
	 * @param fragmentConsumer
	 *            the fragment consumer
	 */
	public static <I> void doWork(Collection<I> data, int fragmentSize, Consumer<Collection<I>> fragmentConsumer) {
		if (data == null || data.isEmpty()) {
			return;
		}
		boolean isQueryFragmentingNeeded = data.size() > fragmentSize;
		if (isQueryFragmentingNeeded) {
			List<I> keys = new ArrayList<>(data);
			int size = data.size();
			int index = 0;
			while (index < size) {
				int step = Math.min(index + fragmentSize, size);
				List<I> fragment = keys.subList(index, step);
				fragmentConsumer.accept(fragment);
				index = step;
			}
		} else {
			fragmentConsumer.accept(data);
		}
	}

	/**
	 * Splits the data in chunks and calls the transformer function with each chunk. The transformed results are
	 * collected and returned at the end of the data.
	 *
	 * @param <I>
	 *            the input type
	 * @param <R>
	 *            the output type
	 * @param data
	 *            the data to process
	 * @param fragmentSize
	 *            the fragment size
	 * @param fragmentTransformer
	 *            the fragment consumer. It should not return <code>null</code> collections.
	 * @return result collection that contains the transformed data.
	 */
	public static <I, R> Collection<R> doWorkWithResult(Collection<I> data, int fragmentSize,
			Function<Collection<I>, Collection<R>> fragmentTransformer) {
		if (CollectionUtils.isEmpty(data)) {
			return Collections.emptyList();
		}
		boolean isQueryFragmentingNeeded = data.size() > fragmentSize;
		if (isQueryFragmentingNeeded) {
			Collection<R> result = new ArrayList<>(data.size());
			List<I> keys = new ArrayList<>(data);
			int size = data.size();
			int index = 0;
			while (index < size) {
				int step = Math.min(index + fragmentSize, size);
				List<I> fragment = keys.subList(index, step);
				result.addAll(fragmentTransformer.apply(fragment));
				index = step;
			}
			return result;
		}
		return fragmentTransformer.apply(data);
	}

	/**
	 * Splits the data in chunks and calls the transformer function with each chunk to reduce the data to a single
	 * element. The transformed results are collected and returned at the end of the call.
	 *
	 * @param <I>
	 *            the input collection elements type
	 * @param <R>
	 *            the output elements type
	 * @param data
	 *            the data to process
	 * @param fragmentSize
	 *            the fragment size
	 * @param fragmentReducer
	 *            the fragment reducer
	 * @return the collection of reduced items
	 */
	public static <I, R> Collection<R> doWorkAndReduce(Collection<I> data, int fragmentSize,
			Function<Collection<I>, R> fragmentReducer) {
		if (CollectionUtils.isEmpty(data)) {
			return Collections.emptyList();
		}
		boolean isQueryFragmentingNeeded = data.size() > fragmentSize;
		if (isQueryFragmentingNeeded) {
			Collection<R> result = new ArrayList<>(data.size() / fragmentSize + 1);
			List<I> keys = new ArrayList<>(data);
			int size = data.size();
			int index = 0;
			while (index < size) {
				int step = Math.min(index + fragmentSize, size);
				List<I> fragment = keys.subList(index, step);
				result.add(fragmentReducer.apply(fragment));
				index = step;
			}
			return result;
		}
		return Collections.singletonList(fragmentReducer.apply(data));
	}

}
