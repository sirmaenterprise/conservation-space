package com.sirma.sep.instance.batch;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;

/**
 * Helper class for buildig {@link StreamBatchRequest} instances
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/06/2017
 */
public class BatchRequestBuilder {

	private BatchRequestBuilder() {
		// nothing to do
	}

	/**
	 * Build request from a collection of data
	 *
	 * @param jobId
	 *            the job name to set
	 * @param collection
	 *            the collection to stream
	 * @return a new batch request instance
	 */
	public static StreamBatchRequest fromCollection(String jobId, Collection<? extends Serializable> collection) {
		StreamBatchRequest request = new StreamBatchRequest(() -> collection.stream().map(Serializable.class::cast));
		request.setBatchName(jobId);
		return request;
	}

	/**
	 * Build request that loads the data by executing a search and returning a single value from the projection list.
	 * The method does not call the search but rather builds an instance that will be executed on schedule.
	 *
	 * @param jobId
	 *            the job id to set to set
	 * @param query
	 *            the query to execute
	 * @param parameters
	 *            query parameters to pass
	 * @param projection
	 *            the projection property name that should be read from the result stream
	 * @param searchService
	 *            the search service instance to use
	 * @return a batch request that will invoke the given query to fetch the needed data.
	 */
	public static StreamBatchRequest fromSearch(String jobId, String query, Map<String, Serializable> parameters,
			String projection, SearchService searchService) {

		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setStringQuery(query);
		if (isNotEmpty(parameters)) {
			searchArguments.getArguments().putAll(parameters);
		}

		return fromSearch(jobId, searchArguments, projection, searchService);
	}

	/**
	 * Build request that loads the data by executing a search and returning a single value from the projection list.
	 * The method does not call the search but rather builds an instance that will be executed on schedule.
	 *
	 * @param jobId
	 *            the job id to set to set
	 * @param searchArguments
	 *            arguments passed to the SearchService
	 * @param projection
	 *            the projection property name that should be read from the result stream
	 * @param searchService
	 *            the search service instance to use
	 * @return a batch request that will invoke the given query to fetch the needed data.
	 */
	public static StreamBatchRequest fromSearch(String jobId, SearchArguments<Instance> searchArguments,
			String projection, SearchService searchService) {
		searchArguments.setMaxSize(-1);
		searchArguments.setGroupBy(null);

		StreamBatchRequest request = new StreamBatchRequest(
				() -> searchService.stream(searchArguments, ResultItemTransformer.asSingleValue(projection)));
		request.setBatchName(jobId);
		return request;
	}

	/**
	 * Build a batch request for executing a custom batch job. The batch job does not need to be defined in a XML file
	 * and could be created dynamically. It will consist of the given item reader, processor and writer. <br>
	 * The method expects that the given names correspond to a CDI resolvable named beans of the corresponding
	 * interfaces of the batch api.
	 * <br>The created batch job will be used to process the given data set. The data set will be fed via the given
	 * reader to the rest of the job processing flow. <br>
	 * The created batch request will have a default batch size. To change the batch size the method
	 * {@link BatchRequest#setChunkSize(int)} could be used.
	 *
	 * @param reader the name of the CDI bean of type {@link javax.batch.api.chunk.ItemReader} that can interpret the
	 * input data and prepare it for processing by the given processor
	 * @param processor the name of the CDI bean of type {@link javax.batch.api.chunk.ItemProcessor} that can handle the
	 * data loaded from the specified reader.
	 * @param writer the name of the CDI bean of type {@link javax.batch.api.chunk.ItemWriter} that can write the result,
	 * produced by the given processor.
	 * @param collection the set of data identifiers to be processed by the batch job.
	 * @return a new batch request instance
	 */
	public static StreamBatchRequest customJob(String reader, String processor, String writer,
			Collection<? extends Serializable> collection) {
		Objects.requireNonNull(reader, "Reader name is mandatory");
		Objects.requireNonNull(processor, "Processor name is mandatory");
		Objects.requireNonNull(writer, "Writer name is mandatory");

		StreamBatchRequest request = new StreamBatchRequest(() -> collection.stream().map(Serializable.class::cast));
		request.setBatchName("genericBatchJob");
		request.getProperties().setProperty(BeanItemReader.READER_NAME, reader);
		request.getProperties().setProperty(BeanItemProcessor.PROCESSOR_NAME, processor);
		request.getProperties().setProperty(BeanItemWriter.WRITER_NAME, writer);
		return request;
	}
}
