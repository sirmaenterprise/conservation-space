package com.sirma.sep.content.batch;

import static com.sirma.itt.seip.util.EqualsHelper.*;
import static java.util.Objects.*;

import java.util.Collection;

import javax.inject.Inject;

import com.sirma.sep.content.ContentEntityDao;
import com.sirma.sep.instance.batch.BatchRequestBuilder;
import com.sirma.sep.instance.batch.BatchService;
import com.sirma.sep.instance.batch.StreamBatchRequest;

/**
 * Base implementation of {@link BatchContentProcessing}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/12/2018
 */
public class BatchContentProcessingImpl implements BatchContentProcessing {

	@Inject
	private BatchService batchService;

	@Inject
	private ContentEntityDao contentDao;

	@Override
	public int processContent(ContentProcessingRequest processingRequest) {

		String itemReader = getOrDefault(processingRequest.getContentReader(), "contentJobReader");
		String itemProcessor = requireNonNull(processingRequest.getContentProcessorName(),
				"Content item processor is required");
		String itemWriter = requireNonNull(processingRequest.getContentWriter(), "Content item writer is required");

		Collection<String> contentsForProcessing = getContentsForProcessing(processingRequest.getContentSelector());
		StreamBatchRequest request = BatchRequestBuilder.customJob(itemReader, itemProcessor, itemWriter,
				contentsForProcessing);
		request.setChunkSize(Math.max(processingRequest.getBatchSize(), 1));
		processingRequest.getProperties().forEach(request.getProperties()::put);

		batchService.execute(request);
		return contentsForProcessing.size();
	}

	private Collection<String> getContentsForProcessing(ContentInfoMatcher contentSelector) {
		return contentDao.getContentIdBy(contentSelector);
	}
}
