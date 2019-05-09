package com.sirma.itt.seip.content.processing;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.content.IdResolver;
import com.sirma.sep.content.InstanceViewPreProcessor;
import com.sirma.sep.content.ViewPreProcessorContext;

/**
 * Extract embedded images from idoc view as new files. The files are stored as embedded content to the instance and
 * will be statically loaded via rest end point: {@code /content/static/<contentId>?tenant=<tenantId>}. The links will
 * be generated in the UI before idoc renderer and prepared for lazy loading.
 * <p>
 * The implementation will use own thread pool to process and save the available images and will update the Idoc. The
 * source attribute will be removed and new attribute {@code data-embedded-id} with the content id of the file.
 *
 * @author BBonev
 */
@Extension(target = InstanceViewPreProcessor.TARGET_NAME, order = 50)
public class EmbeddedImageExtraction implements InstanceViewPreProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private EmbeddedImageExtractor  embeddedImageExtractor;

	@Inject
	private IdResolver idResolver;

	@Override
	@SuppressWarnings("boxing")
	public void process(ViewPreProcessorContext context) {
		Document document = context.getParsedView();
		if (document == null) {
			return;
		}
		try {
			TimeTracker tracker = TimeTracker.createAndStart();
			// do not process external links, not managed from the application
			// the only processed are:
			// base64 data without data-embedded-id
			// have data-embedded-id but not found in the system - managed by the system but from some other server/tenant
			// no data-embedded-id with external links are not processed
			long images = embeddedImageExtractor.extractImages(context.getOwner(), document, UriFilter.denyAll());
			if (images == 0L) {
				return;
			}
			// marks the view as modified so that the document changes to be flushed back to the content to be persisted
			context.setViewUpdated();
			LOGGER.debug("Found and processed {} embedded images for {} ms for instance {}", images, tracker.stop(),
					idResolver.resolve(context.getOwner()));
		} catch (InterruptedException e) {
			LOGGER.warn("Image extraction aborted", e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			LOGGER.warn("Could not process image extraction for idoc {}", context.getOwner(), e);
		}
	}
}
