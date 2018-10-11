package com.sirma.sep.content.preview.messaging;

import com.sirma.sep.content.preview.configuration.ContentPreviewConfiguration;
import com.sirma.sep.content.preview.model.ContentPreviewResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

/**
 * JMS sender for generated content previews and/or thumbnails.
 *
 * @author Mihail Radkov
 */
@Component
public class JMSPreviewSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String CONTENT_PREVIEW_COMPLETED_QUEUE = "ContentPreviewCompletedQueue";
	public static final String CONTENT_THUMBNAIL_COMPLETED_QUEUE = "ContentThumbnailCompletedQueue";

	private final JmsTemplate jmsTemplate;
	private final ContentPreviewConfiguration contentPreviewConfiguration;

	/**
	 * Instantiates new {@link JMSPreviewSender} with the provided {@link JmsTemplate}.
	 *
	 * @param jmsTemplate
	 * 		- the {@link JmsTemplate} used for sending {@link Message}s.
	 * @param contentPreviewConfiguration
	 * 		- global configurations for the preview application
	 */
	@Autowired
	public JMSPreviewSender(JmsTemplate jmsTemplate, ContentPreviewConfiguration contentPreviewConfiguration) {
		this.jmsTemplate = jmsTemplate;
		this.contentPreviewConfiguration = contentPreviewConfiguration;
	}

	/**
	 * Sends any provided content preview and/or thumbnail to {@link #CONTENT_PREVIEW_COMPLETED_QUEUE} and {@link
	 * #CONTENT_THUMBNAIL_COMPLETED_QUEUE} queues respectfully with a reply {@link Message}
	 *
	 * @param receivedMessage
	 * 		- the original JMS {@link Message} to copy attributes from into the reply {@link Message}
	 * @param previewResponse
	 * 		- a {@link ContentPreviewResponse} containing generated content preview and/or thumbnail to be send back
	 * @throws JMSException
	 * 		- in case {@link JMSPreviewSender} cannot construct or send {@link Message}s
	 * @throws IOException
	 * 		- in case {@link JMSPreviewSender} cannot read the provided {@link File} in {@link ContentPreviewResponse}
	 */
	public void sendPreviewResponse(Message receivedMessage, ContentPreviewResponse previewResponse)
			throws JMSException, IOException {
		File generatedPreview = previewResponse.getPreview();
		if (generatedPreview != null && generatedPreview.exists()) {
			LOGGER.debug("Sending generated preview for instance [id={}]",
						 receivedMessage.getStringProperty(ContentMessageAttributes.INSTANCE_ID));
			sendPreview(receivedMessage, generatedPreview);
		}

		if (previewResponse.getThumbnail() != null) {
			LOGGER.debug("Sending generated thumbnail for instance [id={}]",
						 receivedMessage.getStringProperty(ContentMessageAttributes.INSTANCE_ID));
			sendThumbnail(receivedMessage, previewResponse.getThumbnail());
		}
	}

	private void sendPreview(Message receivedMessage, File preview) throws IOException {
		try (BufferedInputStream fileStream = new BufferedInputStream(new FileInputStream(preview))) {
			MessageCreator thumbnailMessage = session -> constructReplyMessage(receivedMessage, session, fileStream,
																			   MediaType.APPLICATION_PDF_VALUE);
			jmsTemplate.send(CONTENT_PREVIEW_COMPLETED_QUEUE, thumbnailMessage);
		}
	}

	private void sendThumbnail(Message receivedMessage, String thumbnail) throws IOException {
		try (BufferedInputStream thumbnailStream = new BufferedInputStream(
				new ByteArrayInputStream(thumbnail.getBytes()))) {
			MessageCreator thumbnailMessage = session -> constructReplyMessage(receivedMessage, session,
																			   thumbnailStream, getImageMimetype());
			jmsTemplate.send(CONTENT_THUMBNAIL_COMPLETED_QUEUE, thumbnailMessage);
		}
	}

	private static Message constructReplyMessage(Message receivedMessage, Session session, InputStream dataStream,
			String mimetype) throws JMSException {
		BytesMessage bytesMessage = session.createBytesMessage();

		bytesMessage.setObjectProperty(ContentMessageAttributes.JMS_INPUT_STREAM, dataStream);
		bytesMessage.setStringProperty(ContentMessageAttributes.MIMETYPE, mimetype);

		ContentMessageAttributes.STRING_ATTRIBUTES
				.forEach(key -> transferStringProperty(key, receivedMessage, bytesMessage));

		return bytesMessage;
	}

	private static void transferStringProperty(String propertyKey, Message originalMessage, Message newMessage) {
		try {
			newMessage.setStringProperty(propertyKey, originalMessage.getStringProperty(propertyKey));
		} catch (JMSException e) {
			throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
		}
	}

	private String getImageMimetype() {
		return "image/" + contentPreviewConfiguration.getThumbnailFormat();
	}
}
