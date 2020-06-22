package com.sirma.sep.instance.batch.reader;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.batch.api.chunk.AbstractItemReader;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;

import com.sirma.sep.instance.batch.BatchDataService;
import com.sirma.sep.instance.batch.BatchProperties;

/**
 * Reads the current chunk of data from the database.
 *
 * @param <T> type of the objects fetched by the reader.
 * @author Adrian Mitev
 */
public abstract class BaseItemReader<T> extends AbstractItemReader {

	@Inject
	private BatchDataService batchDataService;

	@Inject
	private JobContext jobContext;

	@Inject
	private BatchProperties batchProperties;

	private Queue<Item<T>> loaded;
	private List<Item<T>> processed = new LinkedList<>();
	private Integer checkpointIndex;

	@Override
	public void open(Serializable checkpoint) throws Exception {
		if (checkpoint == null) {
			// in case of new job the initial value will be 0
			// in case of job restart the index will be the number of already processed data entries
			checkpointIndex = batchDataService.getJobProgress(batchProperties.getJobId(jobContext.getExecutionId()));
		} else {
			checkpointIndex = (Integer) checkpoint;
		}

		loadMoreData();
	}

	@Override
	public Object readItem() throws Exception {
		if (loaded.isEmpty() && !loadMoreData()) {
			// end of the data is reached
			return null;
		}
		Item<T> item = loaded.poll();
		processed.add(item);
		return item.getValue();
	}

	private boolean loadMoreData() {
		int chunkSize = getChunkSize();
		// if the loaded/returned items are less then the chunk size
		// we may come here before the batch api calls the checkpointInfo method to update checkpointIndex
		// so we add to the index the currently processed items not to read them again
		int offset = checkpointIndex + processed.size();
		List<String> batchData = batchDataService.getBatchData(jobContext.getExecutionId(), offset, chunkSize);
		loaded = new LinkedList<>();

		loadBatchData(batchData, (id, value) -> loaded.add(new Item<>(id, value)));
		// we have skipped items, if we do not add them as processed these items will not be marked in the database as such
		// also this will cause the checkpointIndex to grow less than the actually read data and cause same item
		// to be read more than once
		if (batchData.size() != loaded.size()) {
			Set<String> data = new HashSet<>(batchData);
			loaded.forEach(item -> data.remove(item.getId()));
			data.forEach(id -> processed.add(new Item<>(id, null)));
		}
		return !loaded.isEmpty();
	}

	@Override
	public Serializable checkpointInfo() throws Exception {
		// collect the identifiers for the processed items only, the non processed (loaded) items will be left as such
		// for future investigation
		List<String> processedIds = processed.stream()
				.filter(Objects::nonNull)
				.map(Item::getId)
				.collect(Collectors.toList());
		batchDataService.markJobDataAsProcessed(jobContext.getExecutionId(), processedIds);

		// update the checkpoint when we process the whole chunk
		checkpointIndex += processed.size();

		processed.clear();

		// no need to return any checkpoints, the next read will begin from where we are leaving
		return checkpointIndex;
	}

	@Override
	public void close() throws Exception {
		loaded = null;
		processed = null;
	}

	/**
	 * Provides the data required to process the current chunk.
	 *
	 * @param instanceIds ids of the instances contained in the current chunk.
	 * @param onLoadedItem the function that should be called for each loaded item
	 */
	protected abstract void loadBatchData(Collection<String> instanceIds, BiConsumer<String, T> onLoadedItem);

	/**
	 * Provides the size of the chunk to load.
	 *
	 * @return size of the chunk.
	 */
	protected int getChunkSize() {
		return batchProperties.getChunkSize(jobContext.getExecutionId()).orElse(BatchProperties.DEFAULT_CHUNK_SIZE);
	}

	/**
	 * Represents an item for processing that wraps the original identifier and the loaded resource for that identifier.
	 *
	 * @param <I> item type
	 */
	private class Item<I> {
		private final String id;
		private final I value;

		Item(String id, I value) {
			this.id = id;
			this.value = value;
		}

		String getId() {
			return id;
		}

		I getValue() {
			return value;
		}
	}

}
