package com.sirma.sep.instance.batch.reader;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * Reads the current chunk of data from the database.
 *
 * @author Adrian Mitev
 */
public abstract class DefaultItemReader extends BaseItemReader<String> {

	@Override
	protected void loadBatchData(Collection<String> instanceIds, BiConsumer<String, String> consumer) {
		instanceIds.forEach(id -> consumer.accept(id, id));
	}

}
