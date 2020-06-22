package com.sirma.itt.cmf.alfresco4.content;

import static java.util.stream.Collectors.toList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentEntity;
import com.sirma.sep.content.ContentEntityDao;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.LocalStore;
import com.sirma.sep.content.event.ContentMovedEvent;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirma.sep.content.preview.jms.ContentPreviewMessageAttributes;
import com.sirma.sep.content.preview.jms.ContentPreviewQueue;
import com.sirma.sep.content.preview.remote.ContentPreviewRemoteService;
import com.sirma.sep.content.preview.remote.mimetype.MimeTypeSupport;
import com.sirma.sep.content.rendition.ThumbnailService;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.convert.BytesMessageWriter;

/**
 * Note: This class should be deleted with the module when we remove alfresco support<br>
 *
 * Observer for moved content from alfresco store to copy the primary's content preview to a separate content entry. <br>
 * If the preview is not generated in alfresco it will be. In order not to stress the alfresco server only single
 * request at a time will be allowed. The rest will be blocked for up to 15 minutes.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2018
 */
@ApplicationScoped
public class ContentPreviewMigration {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private InstanceContentService contentService;
	@Inject
	private ContentEntityDao contentEntityDao;
	@Inject
	private ThumbnailService thumbnailService;
	@Inject
	private SenderService senderService;
	@Inject
	private ContentPreviewRemoteService previewRemoteService;

	void onContentMove(@Observes ContentMovedEvent event) {
		ContentInfo contentInfo = contentService.getContent(event.getContentId(), "any");
		if (!isSupported(contentInfo)) {
			LOGGER.trace("Preview migration for content {} for instance {} with purpose {} is not supported.",
					contentInfo.getContentId(), contentInfo.getInstanceId(), contentInfo.getContentPurpose());
			return;
		}
		Collection<String> instancesWithoutPreview = getInstancesWithoutPreview(contentInfo);
		if (instancesWithoutPreview.isEmpty()) {
			LOGGER.trace("No previews to migrate for content {} for instance {}.", contentInfo.getContentId(),
					contentInfo.getInstanceId());
			return;
		}
		String instanceId = contentInfo.getInstanceId().toString();

		String mimetype = contentInfo.getMimeType();
		MimeTypeSupport mimeTypeSupport = previewRemoteService.getMimeTypeSupport(mimetype);

		instancesWithoutPreview.remove(instanceId);
		// Some mime types like the PDF is a preview itself -> directly store it with the new purpose.
		if (mimeTypeSupport.isSelfPreview()) {
			saveContentAsPreview(instanceId, contentInfo);
			instancesWithoutPreview.forEach(id -> saveContentAsPreview(id, contentInfo));
			LOGGER.debug("Imported primary content with id={} and mimetype={} as primary content preview",
					contentInfo.getContentId(), mimetype);
		}

		if (!mimeTypeSupport.supportsThumbnail()) {
			thumbnailService.removeSelfThumbnail(instanceId);
			instancesWithoutPreview.forEach(thumbnailService::removeSelfThumbnail);
		}

		if (mimeTypeSupport.supportsPreview() || mimeTypeSupport.supportsThumbnail()) {
			scheduleForPreview(contentInfo, mimeTypeSupport);
			LOGGER.debug("Scheduled preview and thumbnail for instance with id={} and mimetype={}", instanceId,
					mimetype);
		}
	}

	private void saveContentAsPreview(Serializable instanceId, ContentInfo contentInfo) {
		ContentImport contentPreview = ContentImport.copyFrom(contentInfo)
				.setPurpose(Content.PRIMARY_CONTENT_PREVIEW)
				.setInstanceId(instanceId);
		contentService.importContent(contentPreview);
	}

	private void scheduleForPreview(ContentInfo contentInfo, MimeTypeSupport mimeTypeSupport) {
		Pair<String, String> nameAndExtension = FileUtil.splitNameAndExtension(contentInfo.getName());
		String instanceId = contentInfo.getInstanceId().toString();
		SendOptions sendOptions = SendOptions.create()
				.withWriter(BytesMessageWriter.instance())
				.withProperty(InstanceCommunicationConstants.MIMETYPE, contentInfo.getMimeType())
				.withProperty(InstanceCommunicationConstants.INSTANCE_ID, instanceId)
				.withProperty(ContentCommunicationConstants.CONTENT_ID, contentInfo.getContentId())
				.withProperty(ContentCommunicationConstants.FILE_NAME, nameAndExtension.getFirst())
				.withProperty(ContentCommunicationConstants.FILE_EXTENSION, "." + nameAndExtension.getSecond());

		if (mimeTypeSupport.supportsPreview()) {
			String contentPreviewContentId = createEmptyContent(contentInfo, instanceId);
			sendOptions.withProperty(ContentPreviewMessageAttributes.CONTENT_PREVIEW_CONTENT_ID,
					contentPreviewContentId);
		}

		try (BufferedInputStream contentStream = new BufferedInputStream(contentInfo.getInputStream())) {
			senderService.send(ContentPreviewQueue.CONTENT_PREVIEW_QUEUE, contentStream, sendOptions);
		} catch (IOException e) {
			LOGGER.error("Cannot consume the content stream because of: {}", e.getMessage());
			throw new EmfRuntimeException(e);
		}
	}

	private String createEmptyContent(ContentInfo content, Serializable instanceId) {
		ContentImport emptyContent = ContentImport.createEmpty()
				.setName(content.getName())
				.setPurpose(Content.PRIMARY_CONTENT_PREVIEW)
				.setInstanceId(instanceId)
				.setRemoteSourceName(LocalStore.NAME);
		return contentService.importContent(emptyContent);
	}

	private boolean isSupported(ContentInfo content) {
		return content.exists()
				&& Alfresco4ContentStore.STORE_NAME.equals(content.getRemoteSourceName())
				&& Content.PRIMARY_CONTENT.equals(content.getContentPurpose())
				&& content.getInstanceId() != null
				&& content.getLength() > 0L;
	}

	private Collection<String> getInstancesWithoutPreview(ContentInfo content) {
		return contentEntityDao.getEntityByRemoteId(content.getRemoteSourceName(), content.getRemoteId())
				.stream()
				.map(ContentEntity::getInstanceId)
				.filter(instanceId -> instanceId.startsWith(content.getInstanceId().toString()))
				.filter(instanceId -> !contentService.getContent(instanceId, Content.PRIMARY_CONTENT_PREVIEW).exists())
				.collect(toList());
	}
}
