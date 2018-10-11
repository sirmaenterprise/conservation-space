package com.sirma.sep.instance.batch;

import java.util.List;

/**
 * Service for managing the batch runtime data. Has methods for loading data for processing, updating processed data
 * and removing data after completion.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/06/2017
 */
public interface BatchDataService {

	/**
	 * Assigns data item to a job with the given name and job id. Data added via this method will be returned for
	 * by the method {@link #getBatchData(long, int, int)}
	 * @param jobName the name of the batch job that should process the data
	 * @param jobId the internal job identifier that can be retrieved via {@link BatchProperties#getJobId(long)}
	 * @param data the data item to add
	 */
	void addData(String jobName, String jobId, String data);

	/**
	 * Loads chunk of data for processing. This method should be called from a
	 * {@link javax.batch.api.chunk.ItemReader ItemReader} implementation to read data for processing to pass it down
	 * batch pipeline. <br>Note that if the items are not marked as processed using the method
	 * {@link #markJobDataAsProcessed(long, List)} they will be returned again by this method.
	 * <br>The preferred use should be reading batch of items, using the current method and marking them as processed in
	 * {@link javax.batch.api.chunk.ItemReader#checkpointInfo() ItemReader.checkpointInfo()} method. As this method
	 * is called on successful data processing and writing.
	 *
	 * @param jobExecutionId the job instance id that is currently executing, obtained from
	 * {@link javax.batch.runtime.context.JobContext#getExecutionId()}
	 * @param offset the offset to use when loading batch data. The offset is zero based so the first call should be
	 * {@code getBatchData(jobExecutionId, 0, 10)} that will return items in positions from 0 till 9 and the next call
	 * should be {@code getBatchData(jobExecutionId, 10, 10)} that will return items from 10 to 19 including and so on
	 * @param itemsToLoad the maximum items to load.
	 * @return the loaded items
	 */
	List<String> getBatchData(long jobExecutionId, int offset, int itemsToLoad);

	/**
	 * Marks the given data assigned to the given job id as successfully processed. If not marked as process
	 *
	 * @param jobExecutionId the job instance id that is currently executing, obtained from
	 * {@link javax.batch.runtime.context.JobContext#getExecutionId()}
	 * @param processedIds the list of processed items. The list arguments should be the safe format returned from
	 * the method {@link #getBatchData(long, int, int)}
	 */
	void markJobDataAsProcessed(long jobExecutionId, List<String> processedIds);

	/**
	 * Clears all job data identified by the job instance id fetched from {@link BatchProperties#getJobId(long)}
	 *
	 * @param jobExecutionId the job identifier to remove the assigned data
	 */
	void clearJobData(String jobExecutionId);

	/**
	 * Clears all job data identified by the job instance id fetched from {@link javax.batch.runtime.context.JobContext#getExecutionId()}
	 *
	 * @param jobExecutionId the job identifier to remove the assigned data
	 */
	void clearJobData(long jobExecutionId);
}
