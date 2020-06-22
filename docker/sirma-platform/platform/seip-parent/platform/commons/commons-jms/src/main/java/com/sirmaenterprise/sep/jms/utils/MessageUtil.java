package com.sirmaenterprise.sep.jms.utils;

import java.io.Serializable;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;

import com.sirmaenterprise.sep.jms.exception.JmsRuntimeException;

/**
 * Contains common logic used in {@link Message} processing.
 *
 * @author A. Kunchev
 */
public class MessageUtil {

	private MessageUtil() {
		throw new InstantiationError("Utility classes should not be instantiated.");
	}

	/**
	 * Adds additional properties to the passed {@link Message}.
	 *
	 * @param message to enrich
	 * @param attributes to be added to the message
	 */
	public static void enrichMessage(Map<String, Serializable> attributes, Message message) {
		attributes.forEach((k, v) -> {
			try {
				message.setObjectProperty(k, v);
			} catch (JMSException e) {
				throw new JmsRuntimeException("An error occurred while entiching the message headers.", e);
			}
		});
	}
}