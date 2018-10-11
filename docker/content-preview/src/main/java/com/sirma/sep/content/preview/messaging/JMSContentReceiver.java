package com.sirma.sep.content.preview.messaging;

import com.sirma.sep.content.preview.configuration.ContentPreviewConfiguration;
import com.sirma.sep.content.preview.messaging.logging.JMSLoggerContext;
import com.sirma.sep.content.preview.model.ContentPreviewRequest;
import com.sirma.sep.content.preview.model.ContentPreviewResponse;
import com.sirma.sep.content.preview.service.ContentPreviewService;
import com.sirma.sep.content.preview.util.FileUtils;
import com.sirma.sep.content.preview.util.TimeTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

/**
 * Remote queue JMS {@link Message} listener for incoming content for preview and thumbnail generation.
 * <p>
 * It sets diagnostic logging context from the incoming JMS {@link Message} properties which can be accessed by the
 * logger implementation.
 *
 * @author Mihail Radkov
 */
@Component
public class JMSContentReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String CONTENT_PREVIEW_QUEUE = "ContentPreviewQueue";

	private final ContentPreviewService contentPreviewService;
	private final JMSPreviewSender jmsPreviewSender;
	private final ContentPreviewConfiguration contentPreviewConfiguration;

	/**
	 * Instantiates new remote queue JMS listener to consume {@link Message} for content preview and/or thumbnail
	 * generation.
	 *
	 * @param contentPreviewService
	 * 		- the preview service to use for generation
	 * @param jmsPreviewSender
	 * 		- the sender used to send back generated previews and thumbnails
	 * @param contentPreviewConfiguration
	 * 		- configurations to be used by the {@link JMSContentReceiver}
	 */
	@Autowired
	public JMSContentReceiver(ContentPreviewService contentPreviewService, JMSPreviewSender jmsPreviewSender,
			ContentPreviewConfiguration contentPreviewConfiguration) {
		this.contentPreviewService = contentPreviewService;
		this.jmsPreviewSender = jmsPreviewSender;
		this.contentPreviewConfiguration = contentPreviewConfiguration;
	}

	/**
	 * Processes incoming content messages and sends the results back.
	 *
	 * @param message
	 * 		- the incoming JMS {@link Message} with content for processing
	 * @throws IOException
	 * 		- indicates a problem with the incoming content document processing.
	 * @throws JMSException
	 * 		- indicates a problem with the content processing related to JMS operations
	 */
	@JmsListener(destination = CONTENT_PREVIEW_QUEUE, containerFactory = JMSContentFactory.CONTENT_PREVIEW_FACTORY)
	public void receiveContentMessage(Message message) throws IOException, JMSException {
		File content = null;
		try {
			JMSLoggerContext.onMessage(message);

			String instanceId = message.getStringProperty(ContentMessageAttributes.INSTANCE_ID);
			String mimetype = message.getStringProperty(ContentMessageAttributes.MIMETYPE);

			if (!contentPreviewService.isContentSupported(mimetype)) {
				LOGGER.warn("The content for instance [id={}] with {} for mimetype is not supported!", instanceId, mimetype);
				return;
			}

			LOGGER.info("Receiving content for instance [id={}] with mimetype {}", instanceId, mimetype);

			content = downloadContent(message);

			ContentPreviewRequest contentPreviewRequest = new ContentPreviewRequest(content, mimetype,instanceId)
					.setTimeoutMultiplier(message.getIntProperty(ContentMessageAttributes.JMS_DELIVERY_COUNT));

			ContentPreviewResponse contentPreviewResponse = contentPreviewService.processRequest(contentPreviewRequest);

			try {
				this.jmsPreviewSender.sendPreviewResponse(message, contentPreviewResponse);
			} finally {
				FileUtils.deleteFile(contentPreviewResponse.getPreview());
			}
		} catch (Exception ex) {
			LOGGER.error("Preview/Thumbnail content generation failed because of: {}", ex.getMessage(), ex);
			throw ex;
		} finally {
			FileUtils.deleteFile(content);
		}
	}

	/**
	 * Extracts the incoming content from {@link Message}'s {@link ContentMessageAttributes#SAVE_STREAM} byte stream
	 * property into a temporary file.
	 *
	 * @param message
	 * 		- incoming JMS {@link Message} with the content for extraction
	 * @return a temporary {@link File} with the extracted content
	 * @throws JMSException
	 * 		- indicates that the {@link Message} cannot be read for some reason
	 * @throws IOException
	 * 		- indicates that the content cannot be extracted to a temporary file for some reason
	 */
	private File downloadContent(Message message) throws JMSException, IOException {
		TimeTracker tracker = TimeTracker.create();

		String instanceId = message.getStringProperty(ContentMessageAttributes.INSTANCE_ID);
		String tempFileName = generateUniqueFileName(instanceId);
		String fileExtension = message.getStringProperty(ContentMessageAttributes.FILE_EXTENSION);

		File file = File.createTempFile(tempFileName, fileExtension, contentPreviewConfiguration.getTempFolder());
		try (BufferedOutputStream bufferedOutput = new BufferedOutputStream(new FileOutputStream(file))) {
			message.setObjectProperty(ContentMessageAttributes.SAVE_STREAM, bufferedOutput);
		}

		LOGGER.debug("Downloading content for instance [id={}] took {} ms", instanceId, tracker.stopInMs());
		return file;
	}

	private static String generateUniqueFileName(String instanceId) {
		return UUID.randomUUID() + "_" + instanceId.replace(':', '-') + "_";
	}
}
