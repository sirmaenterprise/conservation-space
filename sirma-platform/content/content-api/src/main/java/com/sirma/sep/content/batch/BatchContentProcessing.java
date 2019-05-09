package com.sirma.sep.content.batch;

/**
 * Management service endpoint for executing operation on the stored content in persisted manner over huge number of
 * entries. The service works by creating a batch job that processes the content selected by the given
 * {@link ContentInfoMatcher} obtained via {@link ContentProcessingRequest#getContentSelector()}. <br>
 * The content is read by named bean of type {@link javax.batch.api.chunk.ItemReader}. If no reader is set then a
 * default reader will be used. It will read the content record and pass an instance of
 * {@link com.sirma.sep.content.ContentInfo} down the line. If other behaviour is needed then custom reader could be
 * set as  parameter via {@link ContentProcessingRequest#setContentReader(String)} <br>
 * The content is processed by named bean of type {@link javax.batch.api.chunk.ItemProcessor} set as parameter via
 * {@link ContentProcessingRequest#setContentProcessorName(String)}. <br>
 * The processed content is then written by a
 * named bean of type {@link javax.batch.api.chunk.ItemWriter} set as parameter via
 * {@link ContentProcessingRequest#setContentWriter(String)}.<br>
 * All bean methods accept a CDI bean name.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/12/2018
 */
public interface BatchContentProcessing {

	/**
	 * Create and run a batch content process request. The processing is done asynchronously so when the content is
	 * selected the method will return.
	 *
	 * @param processingRequest the processing request to execute.
	 * @return the estimated content entities that will be processed by the given request.
	 */
	int processContent(ContentProcessingRequest processingRequest);
}
