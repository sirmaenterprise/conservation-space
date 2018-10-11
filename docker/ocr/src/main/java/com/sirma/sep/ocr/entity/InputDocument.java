package com.sirma.sep.ocr.entity;

import java.lang.invoke.MethodHandles;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data object used to hold all the needed data for ocr data that was received from the SES's jms queue.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 20/10/2017
 */
public class InputDocument {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final Message originalMessage;

	private InputDocument(Message originalMessage) {
		this.originalMessage = originalMessage;
	}

	/**
	 * Builds a new input document object for a given message.
	 *
	 * @param originalMessage the original message
	 * @return the input document.
	 */
	public static InputDocument buildInputDocument(Message originalMessage) {
		return new InputDocument(originalMessage);
	}

	public Message getOriginalMessage() {
		return originalMessage;
	}

	public String getFileName() {
		return getParameter(ContentMessageAttributes.FILE_NAME.toString());
	}

	public String getFileExtension() {
		return getParameter(ContentMessageAttributes.FILE_EXTENSION.toString());
	}

	public String getMimetype() {
		return getParameter(ContentMessageAttributes.MIMETYPE.toString());
	}

	String getInstanceId() {
		return getParameter(ContentMessageAttributes.INSTANCE_ID.toString());
	}

	public String getTenantId() {
		return getParameter(ContentMessageAttributes.TENANT_ID_KEY.toString());
	}

	String getRequestId() {
		return getParameter(ContentMessageAttributes.REQUEST_ID_KEY.toString());
	}

	String getAuthenticatedUser() {
		return getParameter(ContentMessageAttributes.AUTHENTICATED_USER_KEY.toString());
	}

	String getEffectiveUser() {
		return getParameter(ContentMessageAttributes.EFFECTIVE_USER_KEY.toString());
	}

	String getOcredContentId() {
		return getParameter(ContentMessageAttributes.OCRED_CONTENT_ID.toString());
	}

	public String getOcrLanguage() {
		return getParameter(ContentMessageAttributes.OCR_LANGUAGE.toString());
	}

	String getOcredVersionContentId() {
		return getParameter(ContentMessageAttributes.OCRED_VERSION_CONTENT_ID.toString());
	}

	String getInstanceVersionId() {
		return getParameter(ContentMessageAttributes.INSTANCE_VERSION_ID.toString());
	}

	private String getParameter(String name) {
		try {
			return originalMessage.getStringProperty(name);
		} catch (JMSException e) {
			LOGGER.error("Was unable to read mandatory message property from the received message: ", e);
			return "";
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
				.append("InputDocument [fileName=")
				.append(getFileName())
				.append(", fileExtension=")
				.append(getFileExtension())
				.append(", mimetype=")
				.append(getMimetype())
				.append(", instanceId=")
				.append(getInstanceId());
		builder.append("]");
		return builder.toString();
	}
}
