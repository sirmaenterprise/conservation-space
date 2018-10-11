package com.sirma.sep.content;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Plugin for text extraction of documents. Implementation might include tika or ocr
 *
 * @author BBonev
 */
@ApplicationScoped
public class TextExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@ExtensionPoint(TextExtractorExtension.TARGET_NAME)
	private Plugins<TextExtractorExtension> extractors;

	/**
	 * Extract the text content from the given {@link FileDescriptor}. The actual extraction will be done by
	 * {@link TextExtractorExtension} that supports the given mimetype of the source content.
	 *
	 * @param mimetype
	 *            the mimetype of the content that is provided by the descriptor argument
	 * @param descriptor
	 *            the descriptor means to fetch the content for text extraction
	 * @return the optional that contains the extracted content if found valid extractor
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Optional<String> extract(String mimetype, FileDescriptor descriptor) {
		if (descriptor == null) {
			LOGGER.warn("Invalid extract content request: invalid request parameters");
			return Optional.empty();
		}
		TimeTracker tracker = TimeTracker.createAndStart();

		try {
			return extractors
					.stream()
						.filter(ext -> ext.isApplicable(mimetype, descriptor))
						.map(ext -> callExtractor(ext, descriptor))
						.filter(Objects::nonNull)
						.findAny();
		} finally {
			LOGGER.debug("Content extraction took {} ms", tracker.stop());
		}
	}

	private static String callExtractor(TextExtractorExtension extractor, FileDescriptor descriptor) {
		try {
			return extractor.extract(descriptor);
		} catch (Exception e) {
			// this is changed to throwable due to the fact the tika extractor
			// throws an Error
			// for missing module and we could ignore it for now
			LOGGER.debug("Failed to extract content for descritor {} using {}", descriptor.getId(), extractor, e);
		}
		return null;
	}
}
