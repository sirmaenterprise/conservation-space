package com.sirma.sep.instance.batch;

import java.util.Properties;

/**
 * Base for different job triggering requests.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/06/2017
 */
public class BatchRequest {
	private String batchName;
	private String jobAlias;
	private Properties properties;
	private int chunkSize;
	private int partitionsCount = 1;

	public String getBatchName() {
		return batchName;
	}

	/**
	 * Specify the job name that need to be started
	 *
	 * @param batchName the job name
	 */
	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	public Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
		}
		return properties;
	}

	/**
	 * Set any properties that can be retrieved from the executed job steps
	 *
	 * @param properties the properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Gets the job chunk size to use.
	 *
	 * @return the chunk size
	 */
	public int getChunkSize() {
		return chunkSize;
	}

	/**
	 * Set the batch job chunk size or the number of items to be processed in a single transaction before writing the
	 * data. If not specified a default value will be used.<br>
	 * Note that in order to use this parameter the requested job should have the following fragment in it's job
	 * definition:
	 * <pre>
	 * <code>&lt;chunk item-count="#{jobParameters['chunk_size']}"&gt;</code>
	 * </pre>
	 *
	 * @param chunkSize the chunk size to set
	 */
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	/**
	 * Gets the configured permissions count. The default value is 1 (single thread in single partition)
	 * @return the pertions count
	 */
	public int getPartitionsCount() {
		return partitionsCount;
	}

	/**
	 * Sets the number of partitions that the current job should run one. The default value is 1 if not set.<br>
	 * Note that in order to use this parameter the requested job should have the following fragment in it's job
	 * definition:
	 * <pre>
	 * <code>&lt;partition&gt;
	 *     &lt;reducer ref="partitionSecurity" /&gt;
	 *     &lt;plan partitions="#{jobParameters['partitions']}" /&gt;
	 * &lt;/partition&gt;
	 * </code>
	 * </pre>
	 * @param partitionsCount the partition count to set
	 */
	public void setPartitionsCount(int partitionsCount) {
		this.partitionsCount = partitionsCount;
	}

	/**
	 * Returns any set job alias.
	 *
	 * @return the job alias
	 */
	public String getJobAlias() {
		return jobAlias;
	}

	/**
	 * Specifies an alternative job name. Mainly used for visualization purposes when the job name is too generic. <br>
	 * If nothing is set it will default to the job name
	 *
	 * @param jobAlias the job alias to set
	 */
	public void setJobAlias(String jobAlias) {
		this.jobAlias = jobAlias;
	}
}
