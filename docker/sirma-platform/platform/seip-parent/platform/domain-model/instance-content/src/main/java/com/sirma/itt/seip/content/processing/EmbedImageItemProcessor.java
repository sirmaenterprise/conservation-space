package com.sirma.itt.seip.content.processing;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.batch.api.chunk.ItemProcessor;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.instance.batch.BatchProperties;

/**
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/12/2018
 */
@Named(EmbedImageItemProcessor.NAME)
public class EmbedImageItemProcessor implements ItemProcessor {

	public static final String NAME = "embedImageItemProcessor";
	public static final String ALLOW_ADDRESSES_PARAM = "allowedAddress";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private EmbeddedImageExtractor imageExtractor;

	@Inject
	private JobContext context;

	@Inject
	private BatchProperties properties;

	/**
	 * Creates a value that can be set as batch job parameter under {@link #ALLOW_ADDRESSES_PARAM} specifying the
	 * allowed addresses to be processed.
	 *
	 * @param allowedAddresses the addresses to use
	 * @return a property ready to be set in the job context.
	 */
	public static String buildAllowedAddressesParam(String... allowedAddresses) {
		return Arrays.stream(allowedAddresses).collect(Collectors.joining(";"));
	}

	@Override
	public Object processItem(Object item) throws Exception {
		if (!(item instanceof ContentInfo)) {
			throw new IllegalArgumentException(
					"Expects argument of type " + ContentInfo.class + " but got " + item.getClass());
		}
		ContentInfo info = (ContentInfo) item;
		Document document = Jsoup.parseBodyFragment(info.asString());
		String addresses = properties.getJobProperty(context.getExecutionId(), ALLOW_ADDRESSES_PARAM);
		UriFilter addressFilter = UriFilter.acceptAll();
		if (StringUtils.isNotBlank(addresses)) {
			addressFilter = UriFilter.whiteList(addresses.split(";"));
		}
		try {
			long downloaded = imageExtractor.extractImages(info.getInstanceId(), document, addressFilter);
			if (downloaded <= 0) {
				// nothing processed, skip the content
				return null;
			}
			LOGGER.info("Downloaded for {} {} files", info.getContentId(), downloaded);
		} catch (Exception e) {
			LOGGER.warn("Failed image extraction for instance {} view {}. Skipping it", info.getInstanceId(),
					info.getContentId(), e);
		}
		return Content.createFrom(info)
				.setContent(getIdocContent(document), StandardCharsets.UTF_8)
				.setContentId(info.getContentId());
	}

	private static String getIdocContent(Document document) {
		return document.body().outerHtml();
	}
}
