package com.sirma.sep.content.extract;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentPersister;
import com.sirma.sep.content.TextExtractor;

/**
 * Observer for content changes that calls content extraction via
 * {@link ContentExtractor#extractContent(String, FileDescriptor)} and persist the content via the
 * {@link ContentPersister}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ContentExtractor {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private javax.enterprise.inject.Instance<ContentPersister> contentPersister;
	@Inject
	private TextExtractor contentService;

	/**
	 * Extract the content from the given {@link Content} and persist it to the given {@code instanceId}.
	 * <p>
	 * In order for the extraction to happen the {@link Content} must provide:
	 * <ul>
	 * <li>a if the content is a view ( {@link Content#isView()}
	 * <li>mimetype ({@link Content#getMimeType()})
	 * <li>the content itself ({@link Content#getContent()})
	 * </ul>
	 *
	 * @param instanceId
	 *            the instance id to assign the extracted content to
	 * @param content
	 *            the content source to extract from
	 * @return true, if successfully extracted and persisted the content
	 */
	public boolean extractAndPersist(Serializable instanceId, Content content) {
		if (!isContentValidForExtraction(content)) {
			return false;
		}
		return doExtractAndPersist(instanceId, content.getMimeType(), content.getContent());
	}

	/**
	 * Extract the content from the given {@link ContentInfo} and persist it to the given {@code instanceId}.
	 * <p>
	 * In order for the extraction to happen the {@link ContentInfo} must provide:
	 * <ul>
	 * <li>a if the content is a view ( {@link ContentInfo#isView()}
	 * <li>mimetype ( {@link ContentInfo#getMimeType()} )
	 * <li>the content itself will be read from ( {@link ContentInfo#getInputStream()})
	 * </ul>
	 *
	 * @param instanceId
	 *            the instance id to assign the extracted content to
	 * @param contentInfo
	 *            the content source to extract from
	 * @return true, if successfully extracted and persisted the content
	 */
	public boolean extractAndPersist(Serializable instanceId, ContentInfo contentInfo) {
		if (!isContentValidForExtraction(contentInfo)) {
			return false;
		}
		return doExtractAndPersist(instanceId, contentInfo.getMimeType(), contentInfo);
	}

	@SuppressWarnings("boxing")
	private boolean doExtractAndPersist(Serializable instanceId, String mimetype,
			FileDescriptor descriptor) {
		if (isExtractionViable(instanceId)) {
			TimeTracker tracker = TimeTracker.createAndStart();
			Optional<String> extractedContent = extractContent(mimetype, descriptor);
			if (!extractedContent.isPresent()) {
				return false;
			}
			contentPersister.get().savePrimaryContent(instanceId, extractedContent.get());
			LOGGER.debug("Content extraction and save took {} ms", tracker.stop());
			return true;
		}
		return false;
	}

	private Optional<String> extractContent(String mimetype, FileDescriptor content) {
		return contentService.extract(mimetype, content);
	}

	private boolean isExtractionViable(Serializable instanceId) {
		boolean isPersisterPresent = !contentPersister.isUnsatisfied();
		boolean isValidInstance = instanceId != null;
		return isPersisterPresent && isValidInstance;
	}

	private static boolean isContentValidForExtraction(Content content) {
		if (content == null) {
			return false;
		}
		return content.isIndexable() && content.getContent() != null && content.getMimeType() != null;
	}

	/**
	 * Check if <code>context</code> is valid for extraction.
	 * @param content to be checked.
	 * @return true if <code>content</code> is valid for extraction.
	 */
	public static boolean isContentValidForExtraction(ContentInfo content) {
		return content != null && content.getMimeType() != null && content.isIndexable();
	}

}
