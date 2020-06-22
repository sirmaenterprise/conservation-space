package com.sirma.sep.instance.batch;

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Batch request that provides a stream of data that should be written before executing the specified batch job.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/06/2017
 */
public class StreamBatchRequest extends BatchRequest {
	private final Supplier<Stream<Serializable>> streamSupplier;

	/**
	 * Initialize the instance with a supplier that provides the data streaming.
	 *
	 * @param streamSupplier provides the data stream to that should be persisted
	 */
	public StreamBatchRequest(Supplier<Stream<Serializable>> streamSupplier) {
		this.streamSupplier = streamSupplier;
	}

	public Supplier<Stream<Serializable>> getStreamSupplier() {
		return streamSupplier;
	}
}
