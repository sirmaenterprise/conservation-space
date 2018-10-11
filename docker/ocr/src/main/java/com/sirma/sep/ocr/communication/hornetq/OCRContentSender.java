package com.sirma.sep.ocr.communication.hornetq;

import java.io.File;
import java.lang.invoke.MethodHandles;

import javax.jms.BytesMessage;
import javax.jms.Destination;

import com.sirma.sep.ocr.entity.InputDocument;
import com.sirma.sep.ocr.entity.MessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

/**
 * {@link OCRContentSender} is jms sender for recognized content to either the
 * ReplyTo address or by default to 'CompletedOCRQueue' HornetQ queue. Message
 * is send as {@link BytesMessage} with all needed headers data
 *
 * @author bbanchev
 */
@Component
@Profile("service")
public class OCRContentSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final JmsTemplate jmsTemplate;

	/**
	 * Injects the beans in the current class.
	 *
	 * @param jmsTemplate the jms template.
	 * @see JmsTemplate
	 */
	@Autowired
	public OCRContentSender(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	/**
	 * Constructs JMS message and sends the OCR content.
	 *
	 * @param document the document
	 * @param jmsReplyTo the jms reply to
	 * @param ocredFile the already OCRed file, which needs to be sent.
	 */
	public void sendOCRContent(InputDocument document, Destination jmsReplyTo, File ocredFile) {
		if (ocredFile == null) {
			LOGGER.error("File was not generated. Got nothing to send back.");
			return;
		}

		MessageCreator message = session -> MessageBuilder.buildOutputMessage(document, session, ocredFile);
		if (jmsReplyTo != null) {
			LOGGER.debug("Document with details: [{}] is send to queue [{}]!", document, jmsReplyTo);
			jmsTemplate.send(jmsReplyTo, message);
		} else {
			LOGGER.info("Document with details: [{}] is send to queue [CompletedOCRQueue]!", document);
			jmsTemplate.send("CompletedOCRQueue", message);
		}
	}

}
