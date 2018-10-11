package com.sirma.sep.ocr.communication.hornetq;

import java.io.File;
import java.lang.invoke.MethodHandles;

import javax.jms.Message;

import com.sirma.sep.ocr.entity.InputDocument;
import com.sirma.sep.ocr.exception.OCRFailureException;
import com.sirma.sep.ocr.service.DocumentProcessor;
import com.sirma.sep.ocr.service.TesseractOCRProperties;
import com.sirma.sep.ocr.service.TesseractOCRProperties.Mimetype;
import com.sirma.sep.ocr.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * {@link OCRContentReceiver} is listener for remote queue JMS messages that are read and send for OCR processing to
 * {@link DocumentProcessor}.
 *
 * @author bbanchev
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
@Component
@Profile("service")
public class OCRContentReceiver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final DocumentProcessor docProcessor;
	private final TesseractOCRProperties ocrProperties;
	private final OCRContentSender ocrSender;

	/**
	 * Injects the beans.
	 *
	 * @param docProcessor {@link DocumentProcessor} class.
	 * @param ocrProperties the {@link TesseractOCRProperties} which hold the configurations.
	 * @see DocumentProcessor
	 * @see TesseractOCRProperties
	 */
	@Autowired
	public OCRContentReceiver(DocumentProcessor docProcessor, TesseractOCRProperties ocrProperties,
			OCRContentSender ocrSender) {
		this.docProcessor = docProcessor;
		this.ocrProperties = ocrProperties;
		this.ocrSender = ocrSender;
	}

	/**
	 * Invoked on received message configured to container "remoteOcrQueueFactory". The method expects
	 * {@link Message} with set headers.
	 * <ul>
	 * <li>"fileName"</li>
	 * <li>"fileExtension"</li>
	 * <li>"mimetype"</li>
	 * <li>"instanceId"</li>
	 * <li>"issuer" - the tenant id.</li>
	 * </ul>
	 * {@link DocumentProcessor} is invoked for further message processing.
	 *
	 * @param content is the {@link Message} that is received from the remove queue.
	 * @throws OCRFailureException indicates any error during OCR processing with the specified details
	 * @see Mimetype
	 */
	@JmsListener(destination = "OCRQueue", containerFactory = "remoteOcrQueueFactory")
	public void receiveMessage(Message content) throws OCRFailureException {
		File ocrFile = null;
		try {
			InputDocument inputDocument = InputDocument.buildInputDocument(content);
			LOGGER.info("Received a new message from tenant [{}] and with file named: [{}]", inputDocument
					.getTenantId(), inputDocument.getFileName());
			String mimetype = inputDocument.getMimetype();
			if (!ocrProperties.getMimetype().getPattern().matcher(mimetype).matches()) {
				return;
			}

			// OCR the document.
			LOGGER.debug("Document with details: [{}] is going to be processed!", inputDocument);
			ocrFile = docProcessor.process(inputDocument);

			// Send back the OCRed document.
			Message originalMessage = inputDocument.getOriginalMessage();
			ocrSender.sendOCRContent(inputDocument, originalMessage.getJMSReplyTo(), ocrFile);
		} catch (Exception e) {
			throw new OCRFailureException(e);
		} finally {
			FileUtils.deleteFile(ocrFile);
		}
	}
}