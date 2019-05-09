package com.sirma.sep.content.batch;

import java.util.Properties;

/**
 * Request data object used to trigger batch content processing.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/12/2018
 */
public class ContentProcessingRequest {
	private ContentInfoMatcher contentSelector;
	private String contentReader;
	private String contentProcessorName;
	private String contentWriter;
	private int batchSize;
	private Properties properties = new Properties();

	public ContentInfoMatcher getContentSelector() {
		return contentSelector;
	}

	/**
	 * A mandatory content selector that will be used to select the content that should be processed
	 *
	 * @param contentSelector
	 */
	public void setContentSelector(ContentInfoMatcher contentSelector) {
		this.contentSelector = contentSelector;
	}

	public String getContentReader() {
		return contentReader;
	}

	/**
	 * Set the bean name of the content reader to be used. The bean must be of type {@link javax.batch.api.chunk.ItemReader}.
	 * This is optional parameter. If no value is passed then a default reader will be used that reads the content
	 * entry and passes a ContentInfo instance down the line.
	 *
	 * @param contentReader the reader bean name
	 */
	public void setContentReader(String contentReader) {
		this.contentReader = contentReader;
	}

	public String getContentProcessorName() {
		return contentProcessorName;
	}

	/**
	 * Set the bean name of the content processor to be used. The bean must be of type {@link javax.batch.api.chunk.ItemProcessor}
	 *
	 * @param contentProcessorName the processor bean name
	 */
	public void setContentProcessorName(String contentProcessorName) {
		this.contentProcessorName = contentProcessorName;
	}

	public String getContentWriter() {
		return contentWriter;
	}

	/**
	 * Set the bean name of the content writer to be used. The bean must be of type {@link javax.batch.api.chunk.ItemWriter}
	 *
	 * @param contentWriter writer bean name
	 */
	public void setContentWriter(String contentWriter) {
		this.contentWriter = contentWriter;
	}

	public Properties getProperties() {
		return properties;
	}

	/**
	 * Set any properties that should be passed to the processing steps. They will be retrievable in them using the
	 * {@code com.sirma.sep.instance.batch.BatchProperties#getJobProperty(long, String} method.
	 *
	 * @param key the property key
	 * @param value the value to set
	 * @return the previous value set
	 */
	public Object setProperty(String key, String value) {
		return properties.setProperty(key, value);
	}

	public int getBatchSize() {
		return batchSize;
	}

	/**
	 * Specify how may contents to be processed at once before being stored back to the repository. If nothing is
	 * specified then the processing will be done for content per content.
	 *
	 * @param batchSize the batch size to use
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
