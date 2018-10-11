package com.sirma.sep.ocr.entity;

import static com.sirma.sep.ocr.entity.ContentMessageAttributes.AUTHENTICATED_USER_KEY;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.EFFECTIVE_USER_KEY;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.FILE_EXTENSION;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.FILE_NAME;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.INSTANCE_ID;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.INSTANCE_VERSION_ID;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.MIMETYPE;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.OCRED_CONTENT_ID;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.OCRED_VERSION_CONTENT_ID;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.REQUEST_ID_KEY;
import static com.sirma.sep.ocr.entity.ContentMessageAttributes.TENANT_ID_KEY;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder that allows transformation from {@link Message} to data object {@link InputDocument} and via versa.
 * Basically this class is used to transform the messages coming from and going to jms queue.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 20/10/2017
 */
public class MessageBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String OCRED_DOC_MIMETYPE = "application/pdf";

	/**
	 * Hide util constructor.
	 */
	private MessageBuilder() {
	}

	/**
	 * Builds a jms {@link Message}.
	 *
	 * @param document a valid {@link InputDocument} object.
	 * @param session a jms {@link Session}
	 * @param ocredFile the file that was already ocr-ed and has to be sent.
	 * @return Message the jms message object
	 */
	public static Message buildOutputMessage(InputDocument document, Session session, File ocredFile) {
		BytesMessage objectMessage;
		try {
			objectMessage = session.createBytesMessage();
			// Fill ocr data
			objectMessage.writeBytes(Files.readAllBytes(Paths.get(ocredFile.getPath())));
			objectMessage.setStringProperty(FILE_NAME.toString(), document.getFileName());
			objectMessage.setStringProperty(FILE_EXTENSION.toString(), "." + FilenameUtils.getExtension(ocredFile.getName()));
			objectMessage.setStringProperty(MIMETYPE.toString(), OCRED_DOC_MIMETYPE);

			// Fill SES specific data
			objectMessage.setStringProperty(INSTANCE_ID.toString(), document.getInstanceId());
			objectMessage.setStringProperty(TENANT_ID_KEY.toString(), document.getTenantId());
			objectMessage.setStringProperty(REQUEST_ID_KEY.toString(), document.getRequestId());
			objectMessage.setStringProperty(AUTHENTICATED_USER_KEY.toString(),
					document.getAuthenticatedUser());
			objectMessage.setStringProperty(EFFECTIVE_USER_KEY.toString(), document.getEffectiveUser());
			objectMessage.setStringProperty(OCRED_CONTENT_ID.toString(), document.getOcredContentId());
			objectMessage.setStringProperty(OCRED_VERSION_CONTENT_ID.toString(), document.getOcredVersionContentId());
			objectMessage.setStringProperty(INSTANCE_VERSION_ID.toString(), document.getInstanceVersionId());
		} catch (IOException | JMSException e) {
			LOGGER.error("Error writing message: ", e);
			throw new MessageBuildException("Could not build message for the OCR complete queue, cause:");
		}
		return objectMessage;
	}

}
