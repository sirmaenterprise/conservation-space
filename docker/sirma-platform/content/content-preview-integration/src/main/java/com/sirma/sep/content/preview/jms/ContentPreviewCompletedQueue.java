package com.sirma.sep.content.preview.jms;

import static com.sirma.itt.seip.util.EqualsHelper.*;
import static java.util.stream.Collectors.toSet;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentEntity;
import com.sirma.sep.content.ContentEntityDao;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.api.CommunicationConstants;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Queue receiving generated previews of {@link Content}.
 * <p>
 * Should have an existing {@link Content} record for the {@link ContentCommunicationConstants#CONTENT_ID} in the
 * incoming {@link Message}.
 *
 * @author Mihail Radkov
 */
@Singleton
public class ContentPreviewCompletedQueue {

	@DestinationDef
	public static final String CONTENT_PREVIEW_COMPLETED_QUEUE = "java:/jms.queue.ContentPreviewCompletedQueue";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String TMP_SUFFIX = ".tmp";

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private ContentEntityDao contentEntityDao;

	/**
	 * Listens for generated preview of {@link Content}.
	 * <p>
	 * Extracts the generated preview and stores it to its associated {@link Content} record with {@link
	 * Content#PRIMARY_CONTENT_PREVIEW} purpose. Also inserts preview entries for other instances referencing the same
	 * content used for the preview generation
	 *
	 * @param message JMS message carrying information about the {@link Content} and the generated preview and the preview itself
	 * @throws JMSException if the message cannot be consumed
	 */
	@QueueListener(value = CONTENT_PREVIEW_COMPLETED_QUEUE)
	void onContentPreviewCompleted(Message message) throws JMSException {
		File outputFile = downloadPreview(message);

		String mimetype = message.getStringProperty(InstanceCommunicationConstants.MIMETYPE);
		String instanceId = message.getStringProperty(InstanceCommunicationConstants.INSTANCE_ID);
		String instanceVersionId = message.getStringProperty(InstanceCommunicationConstants.INSTANCE_VERSION_ID);
		String contentId = message.getStringProperty(ContentCommunicationConstants.CONTENT_ID);
		if (contentId == null) {
			// backward compatibility for old messages
			contentId = instanceId;
		}
		String contentPreviewContentId =
				message.getStringProperty(ContentPreviewMessageAttributes.CONTENT_PREVIEW_CONTENT_ID);
		ContentInfo previewContentInfo = updateContent(instanceId, contentPreviewContentId, outputFile, mimetype);

		// fetch the concrete version of the content that this preview belongs to
		ContentInfo content = instanceContentService.getContent(contentId, Content.PRIMARY_CONTENT);
		Collection<ContentEntity> instancesWithoutPreview = Collections.emptyList();
		if (content.exists()) {
			instancesWithoutPreview = getInstancesWithoutPreview(content, instanceVersionId);
			// remove the current instance as we just updated it
			final String contentIdCopy = contentId;
			instancesWithoutPreview.removeIf(entity -> nullSafeEquals(entity.getId(), contentIdCopy)
					|| nullSafeEquals(entity.getInstanceId(), instanceId));
		}

		if (StringUtils.isBlank(previewContentInfo.getContentId()) || !previewContentInfo.exists()) {
			throw new EmfRuntimeException("Content with id=" + contentPreviewContentId
					+ " does not exist for instance with id=" + instanceId);
		}

		// import the content for the rest of the instances
		instancesWithoutPreview.stream()
				.map(entity -> ContentImport.copyFrom(previewContentInfo)
						.setInstanceId(entity.getInstanceId())
						.setContentId(entity.getId()))
				.forEach(instanceContentService::importContent);

		LOGGER.info("Stored content preview for instance with id={}, and similar instances {}", instanceId,
				instancesWithoutPreview.stream().map(ContentEntity::getInstanceId).collect(Collectors.toSet()));
	}

	private File downloadPreview(Message message) throws JMSException {
		File preview = getTempFile(message);
		try (BufferedOutputStream bufferedOutput = new BufferedOutputStream(new FileOutputStream(preview))) {
			message.setObjectProperty(CommunicationConstants.JMS_SAVE_STREAM, bufferedOutput);
		} catch (IOException ex) {
			throw new EmfRuntimeException(
					"Failed to read content preview for instance with id=" + message.getStringProperty(
							InstanceCommunicationConstants.INSTANCE_ID), ex);
		}
		return preview;
	}

	private File getTempFile(Message message) throws JMSException {
		// Underscore will separate the instance id from the unique temporary identifier (easy to distinguish)
		String filename = message.getStringProperty(InstanceCommunicationConstants.INSTANCE_ID) + "_";
		return tempFileProvider.createTempFile(filename, TMP_SUFFIX);
	}

	private ContentInfo updateContent(String instanceId, String contentId, File preview, String mimetype) {
		ContentInfo previewContentInfo = instanceContentService.getContent(contentId, Content.PRIMARY_CONTENT_PREVIEW);
		if (StringUtils.isBlank(previewContentInfo.getContentId())) {
			throw new EmfRuntimeException(
					"Content with id=" + contentId + " does not exist for instance with id=" + instanceId);
		}
		Content previewContent = Content.createFrom(previewContentInfo);
		previewContent.setContent(preview);
		previewContent.setMimeType(mimetype);
		Instance dummyInstance = new EmfInstance(instanceId);
		return instanceContentService.updateContent(contentId, dummyInstance, previewContent);
	}

	private Collection<ContentEntity> getInstancesWithoutPreview(ContentInfo content, String instanceVersionId) {
		List<ContentEntity> instancesWithSameContent = contentEntityDao.getEntityByRemoteId(content.getRemoteSourceName(),
				content.getRemoteId());
		String baseInstanceId = content.getInstanceId().toString();
		// make sure that the version if present will be available
		// it may not be available if instance version is delayed and the method above may not found it
		if (instanceVersionId != null) {
			boolean versionNotPresent = instancesWithSameContent.stream()
					.noneMatch(entity -> instanceVersionId.equals(entity.getInstanceId()));
			if (versionNotPresent) {
				ContentEntity dummyVersionEntity = new ContentEntity();
				dummyVersionEntity.setInstanceId(instanceVersionId);
				instancesWithSameContent.add(dummyVersionEntity);
			}
		}
		return instancesWithSameContent.stream()
				.filter(entity -> entity.getInstanceId().startsWith(baseInstanceId))
				.filter(this::withoutContentPreview)
				.collect(toSet());
	}

	private boolean withoutContentPreview(ContentEntity entity) {
		String id = entity.getId();
		if (id == null) {
			id = entity.getInstanceId();
		}
		return !instanceContentService.getContent(id, Content.PRIMARY_CONTENT_PREVIEW).exists();
	}
}
