package com.sirma.sep.content.preview.jms;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.LocalStore;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirma.sep.content.jms.ContentDestinations;
import com.sirma.sep.content.preview.ContentPreviewConfigurations;
import com.sirma.sep.content.preview.remote.ContentPreviewRemoteService;
import com.sirma.sep.content.preview.remote.mimetype.MimeTypeSupport;
import com.sirma.sep.content.rendition.RenditionService;
import com.sirma.sep.content.rendition.ThumbnailService;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.TopicListener;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.convert.BytesMessageWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;

/**
 * Schedules {@link Content} for preview and/or thumbnail generation over JMS queues.
 * <p>
 * Listens to {@link ContentDestinations#CONTENT_TOPIC} when a {@link Content} is assigned and determines via {@link
 * ContentPreviewRemoteService} if it supports a preview/thumbnail or not.
 * <p>
 * If true, it will prepare empty {@link Content} with {@link Content#PRIMARY_CONTENT_PREVIEW} purpose which can be used
 * later when the preview is generated.
 * <p>
 * For special cases where a  {@link Content} is a preview itself, a new {@link Content} is created with the original
 * one but with {@link Content#PRIMARY_CONTENT_PREVIEW} purpose.
 * <p>
 * {@link Content} is always scheduled if it supports a preview OR thumbnail generation.
 *
 * @author Mihail Radkov
 */
@Singleton
public class ContentPreviewQueue {

	@DestinationDef(maxRedeliveryAttempts = 3)
	public static final String CONTENT_PREVIEW_QUEUE = "java:/jms.queue.ContentPreviewQueue";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String CONTENT_SELECTOR =
			ContentCommunicationConstants.PURPOSE + " = '" + Content.PRIMARY_CONTENT + "' AND ("
					+ ContentCommunicationConstants.REMOTE_SOURCE_NAME + " = 'alfresco4' OR "
			+ ContentCommunicationConstants.REMOTE_SOURCE_NAME + " = '" + LocalStore.NAME + "')";

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private ThumbnailService thumbnailService;

	@Inject
	private SenderService senderService;

	@Inject
	private ContentPreviewRemoteService previewRemoteService;

	@Inject
	private ContentPreviewConfigurations previewConfigurations;

	/**
	 * Listens when a {@link Content} is assigned and can be consumed.
	 * <p>
	 * Consuming consists of determining if the content is supported for generation and the scheduling of it.
	 *
	 * @param contentMessage
	 * 		the message from the topic with the {@link Content} information
	 * @throws JMSException
	 * 		if the message cannot be consumed OR a preview generation scheduled
	 */
	@TopicListener(jndi = ContentDestinations.CONTENT_TOPIC, subscription = "contentPreviewListener",
			selector = CONTENT_SELECTOR)
	void onContentAssigned(Message contentMessage) throws JMSException {
		// Feature toggle
		if (!previewConfigurations.isIntegrationEnabled().get()) {
			return;
		}

		String contentId = contentMessage.getStringProperty(ContentCommunicationConstants.CONTENT_ID);
		String contentPurpose = contentMessage.getStringProperty(ContentCommunicationConstants.PURPOSE);

		ContentInfo contentInfo = instanceContentService.getContent(contentId, contentPurpose);
		if (!contentInfo.exists()) {
			throw new EmfRuntimeException("Missing content with id=" + contentId + " and purpose=" + contentPurpose);
		}

		if (contentInfo.getLength() < 1) {
			LOGGER.debug("Skipping empty content from preview generation for instance with id={}",
						 contentInfo.getInstanceId());
			return;
		}

		String instanceId = contentInfo.getInstanceId().toString();
		String instanceVersionId = getInstanceVersionId(instanceId);

		String mimetype = contentInfo.getMimeType();
		MimeTypeSupport mimeTypeSupport = previewRemoteService.getMimeTypeSupport(mimetype);

		// Some mime types like the PDF is a preview itself -> directly store it with the new purpose.
		if (mimeTypeSupport.isSelfPreview()) {
			saveContentAsPreview(instanceId, contentInfo);
			saveContentAsPreview(instanceVersionId, contentInfo);
			LOGGER.debug("Imported primary content with id={} and mimetype={} as primary content preview",
						 contentInfo.getContentId(), mimetype);
		}

		if (!mimeTypeSupport.supportsThumbnail()) {
			thumbnailService.removeSelfThumbnail(instanceId);
			thumbnailService.removeSelfThumbnail(instanceVersionId);
		}

		if (mimeTypeSupport.supportsPreview() || mimeTypeSupport.supportsThumbnail()) {
			scheduleForPreview(contentInfo, instanceVersionId, mimeTypeSupport);
			LOGGER.debug("Scheduled preview and thumbnail for instance with id={} and mimetype={}", instanceId,
						 mimetype);
		}
	}

	private String getInstanceVersionId(String instanceId) {
		InstanceReference instanceReference = instanceTypeResolver.resolveReference(instanceId)
				.orElseThrow(() -> new EmfRuntimeException("Missing instance with id=" + instanceId));
		Instance instance = instanceReference.toInstance();
		return InstanceVersionService.buildVersionId(instance).toString();
	}

	private void saveContentAsPreview(Serializable instanceId, ContentInfo contentInfo) {
		ContentImport contentPreview = ContentImport.copyFrom(contentInfo)
				.setPurpose(Content.PRIMARY_CONTENT_PREVIEW)
				.setInstanceId(instanceId);
		instanceContentService.importContent(contentPreview);
	}

	private void scheduleForPreview(ContentInfo contentInfo, Serializable instanceVersionId,
			MimeTypeSupport mimeTypeSupport) {
		Pair<String, String> nameAndExtension = FileUtil.splitNameAndExtension(contentInfo.getName());
		String instanceId = contentInfo.getInstanceId().toString();
		SendOptions sendOptions = SendOptions.create()
				.withWriter(BytesMessageWriter.instance())
				.withProperty(InstanceCommunicationConstants.MIMETYPE, contentInfo.getMimeType())
				.withProperty(InstanceCommunicationConstants.INSTANCE_ID, instanceId)
				.withProperty(ContentCommunicationConstants.CONTENT_ID, contentInfo.getContentId())
				.withProperty(ContentCommunicationConstants.FILE_NAME, nameAndExtension.getFirst())
				.withProperty(ContentCommunicationConstants.FILE_EXTENSION, "." + nameAndExtension.getSecond())
				.withProperty(InstanceCommunicationConstants.INSTANCE_VERSION_ID, instanceVersionId);

		if (mimeTypeSupport.supportsPreview()) {
			String contentPreviewContentId = createEmptyContent(contentInfo, instanceId);
			sendOptions.withProperty(ContentPreviewMessageAttributes.CONTENT_PREVIEW_CONTENT_ID,
									 contentPreviewContentId);
		}

		try (BufferedInputStream contentStream = new BufferedInputStream(contentInfo.getInputStream())) {
			senderService.send(CONTENT_PREVIEW_QUEUE, contentStream, sendOptions);
		} catch (IOException e) {
			LOGGER.error("Cannot consume the content stream because of: ", e.getMessage());
			throw new EmfRuntimeException(e);
		}
	}

	private String createEmptyContent(ContentInfo content, Serializable instanceId) {
		ContentImport emptyContent = ContentImport.createEmpty()
				.setName(content.getName())
				.setPurpose(Content.PRIMARY_CONTENT_PREVIEW)
				.setInstanceId(instanceId)
				.setRemoteSourceName(LocalStore.NAME);
		return instanceContentService.importContent(emptyContent);
	}

}
