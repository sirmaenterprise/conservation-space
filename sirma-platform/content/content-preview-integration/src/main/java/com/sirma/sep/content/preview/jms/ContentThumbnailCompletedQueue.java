package com.sirma.sep.content.preview.jms;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.sep.content.rendition.ThumbnailService;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.api.CommunicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * Queue for receiving generated thumbnail of {@link com.sirma.sep.content.Content}
 *
 * @author Mihail Radkov
 */
@Singleton
public class ContentThumbnailCompletedQueue {

	@DestinationDef
	public static final String CONTENT_THUMBNAIL_COMPLETED_QUEUE = "java:/jms.queue.ContentThumbnailCompletedQueue";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String THUMBNAIL_PREFIX = "data:image/png;base64,";

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private ThumbnailService thumbnailService;

	/**
	 * Listens to when a thumbnail is generated for specific {@link com.sirma.sep.content.Content}.
	 *
	 * @param message
	 * 		JMS message carrying the generated thumbnail and information about which instance it belongs to
	 * @throws JMSException
	 * 		if the message cannot be consumed
	 */
	@QueueListener(value = CONTENT_THUMBNAIL_COMPLETED_QUEUE)
	void onContentThumbnailCompleted(Message message) throws JMSException {
		byte[] thumbnailBytes = getThumbnailBytes(message);
		String thumbnail = THUMBNAIL_PREFIX + new String(thumbnailBytes);

		String instanceId = message.getStringProperty(InstanceCommunicationConstants.INSTANCE_ID);
		InstanceReference instanceReference = fetchInstance(instanceId);
		thumbnailService.addThumbnail(instanceReference, thumbnail);
		LOGGER.info("Added thumbnail for instance with id={}", instanceId);

		String instanceVersionId = message.getStringProperty(InstanceCommunicationConstants.INSTANCE_VERSION_ID);
		if (instanceVersionId != null) {
			InstanceReference instanceVersionReference = fetchInstance(instanceVersionId);
			thumbnailService.addThumbnail(instanceVersionReference, thumbnail);
			LOGGER.debug("Added thumbnail for instance version with id={}", instanceVersionId);
		}
	}

	private InstanceReference fetchInstance(String instanceId) {
		return instanceTypeResolver.resolveReference(instanceId)
				.orElseThrow(
						() -> new EmfRuntimeException("Cannot add thumbnail, missing instance with id=" + instanceId));
	}

	private static byte[] getThumbnailBytes(Message message) throws JMSException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				BufferedOutputStream bufferedOutput = new BufferedOutputStream(byteArrayOutputStream)) {
			message.setObjectProperty(CommunicationConstants.JMS_SAVE_STREAM, bufferedOutput);
			bufferedOutput.flush();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException ex) {
			String instanceId = message.getStringProperty(InstanceCommunicationConstants.INSTANCE_ID);
			throw new EmfRuntimeException("Cannot download thumbnail for instance with id=" + instanceId, ex);
		}
	}
}
