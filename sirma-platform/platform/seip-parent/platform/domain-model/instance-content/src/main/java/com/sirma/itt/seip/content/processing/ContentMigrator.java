package com.sirma.itt.seip.content.processing;

import javax.inject.Inject;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.batch.BatchContentProcessing;
import com.sirma.sep.content.batch.ContentInfoMatcher;
import com.sirma.sep.content.batch.ContentProcessingRequest;

/**
 * Offers different supported content migration endpoints.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/12/2018
 */
public class ContentMigrator {

	@Inject
	private BatchContentProcessing contentProcessing;

	/**
	 * Perform image download from the given allowed addresses over all known idoc views. The migration will process all
	 * idoc files and download and embed images that are from one of the given sources. The source addresses should be
	 * described as regex patterns. <br>
	 * If all addresses should be downloaded and embedded a patter {@code .+} could be used.
	 *
	 * @param allowedAddresses the set of allowed addresses. If image is from origin different than the given filter it
	 * will not be downloaded.
	 * @return the number of content instances that will be processed.
	 */
	int downloadEmbeddedImages(String[] allowedAddresses) {
		if (allowedAddresses == null || allowedAddresses.length == 0) {
			return -1;
		}
		ContentProcessingRequest processingRequest = new ContentProcessingRequest();
		// this cannot be merged in the same PR this functionality is added as creates dependency cycle to content-api
		// processingRequest.setJobName("Download embedded images")
		processingRequest.setContentProcessorName(EmbedImageItemProcessor.NAME);
		processingRequest.setContentWriter(IdocContentItemWriter.NAME);
		ContentInfoMatcher contentSelector = new ContentInfoMatcher().setPurpose(Content.PRIMARY_VIEW);
		processingRequest.setContentSelector(contentSelector);
		String addressFilter = EmbedImageItemProcessor.buildAllowedAddressesParam(allowedAddresses);
		processingRequest.setProperty(EmbedImageItemProcessor.ALLOW_ADDRESSES_PARAM, addressFilter);
		return contentProcessing.processContent(processingRequest);
	}
}
