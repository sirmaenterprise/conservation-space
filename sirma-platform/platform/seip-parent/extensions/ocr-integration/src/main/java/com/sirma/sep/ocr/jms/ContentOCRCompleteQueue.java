package com.sirma.sep.ocr.jms;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentPersister;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.TextExtractor;
import com.sirma.sep.content.event.ContentAssignedEvent;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.api.CommunicationConstants;

/**
 * Content OCR complete queue listener responsible for consuming any OCR-ed content messages from
 * the content complete topic and persisting them using the content service with an 'ocr' purpose. A
 * {@link ContentAssignedEvent} will be fired after the content has been uploaded so the tika
 * content extractor could extract the ocred text and put in the instance in the semantic db.
 *
 * @author nvelkov
 */
public class ContentOCRCompleteQueue {

	@DestinationDef
	public static final String CONTENT_COMPLETED_OCR_QUEUE = "java:/jms.queue.CompletedOCRQueue";

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private ContentPersister contentPersister;

	@Inject
	private TextExtractor textExtractor;

	/**
	 * Listen on the content completed ocr queue and persist the content with purpose ocr.
	 *
	 * @param message
	 *            the message containing the ocr-ed content
	 * @throws JMSException
	 *             if the JMS provider fails to get some property value from the incoming message
	 *             due to some internal error.
	 */
	@QueueListener(value = CONTENT_COMPLETED_OCR_QUEUE)
	public void onContentOCRDoneMessage(Message message) throws JMSException {
		if (message instanceof BytesMessage) {
			BytesMessage receivedMessage = (BytesMessage) message;
			File outputFile = tempFileProvider.createTempFile(
					receivedMessage.getStringProperty(ContentCommunicationConstants.FILE_NAME),
					receivedMessage.getStringProperty(ContentCommunicationConstants.FILE_EXTENSION));
			try (BufferedOutputStream bufferedOutput = new BufferedOutputStream(new FileOutputStream(outputFile))) {
				// This will read the file from the message into the output.
				// This will block until the entire content is saved on disk
				receivedMessage.setObjectProperty(CommunicationConstants.JMS_SAVE_STREAM, bufferedOutput);
			} catch (IOException e) {
				JMSException exception = new JMSException("Failed to open downloaded ocred content "
						+ receivedMessage.getStringProperty(ContentCommunicationConstants.FILE_NAME));
				exception.setLinkedException(e);
				throw exception;
			}

			String mimetype = receivedMessage.getStringProperty(InstanceCommunicationConstants.MIMETYPE);
			// Update the instance content.
			updateContent(receivedMessage.getStringProperty(OCRContentMessageAttributes.OCRED_CONTENT_ID),
					receivedMessage.getStringProperty(InstanceCommunicationConstants.INSTANCE_ID), outputFile, mimetype);

			// Update the instance version content.
			updateContent(receivedMessage.getStringProperty(OCRContentMessageAttributes.OCRED_VERSION_CONTENT_ID),
					receivedMessage.getStringProperty(InstanceCommunicationConstants.INSTANCE_VERSION_ID), outputFile,
					mimetype);
		}
	}

	private void updateContent(String contentId, Serializable instanceId, File ocredFile, String mimetype) {
		Content ocredContent = Content.createEmpty()
								.setName(ocredFile.getName())
									.setContent(ocredFile)
									.setIndexable(true)
									.setVersionable(true)
									.setMimeType(mimetype)
									.setPurpose("ocr");
		Instance instance = new EmfInstance(instanceId.toString());
		// Update the prepared empty ocred content entry with the actual ocred content.
		instanceContentService.updateContent(contentId, instance, ocredContent);

		textExtractor.extract(mimetype, ocredContent.getContent())
				.ifPresent(ocrText -> contentPersister.saveOcrContent(instanceId, ocrText));
	}
}
