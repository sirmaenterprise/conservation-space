package com.sirma.sep.content.preview.messaging.logging;

import com.sirma.sep.content.preview.messaging.ContentMessageAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.jms.JMSException;
import javax.jms.Message;
import java.lang.invoke.MethodHandles;

/**
 * Utility class that sets {@link ContentMessageAttributes} attributes from {@link Message} into the {@link MDC}
 * diagnostic context which later can be used in logging implementations to provide additional debugging information.
 *
 * @author Mihail Radkov
 */
public class JMSLoggerContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private JMSLoggerContext() {
		// Hidden constructor for utility
	}

	/**
	 * Reads string attributes from the provided {@link Message} into the {@link MDC} diagnostic context.
	 *
	 * @param message
	 * 		- the message to be used for setting attributes
	 * @throws JMSException
	 * 		- in case the message cannot be read
	 */
	public static void onMessage(Message message) throws JMSException {
		if (message != null) {
			try {
				setStringProperty(message, ContentMessageAttributes.REQUEST_ID);
				setStringProperty(message, ContentMessageAttributes.INSTANCE_ID);
				setStringProperty(message, ContentMessageAttributes.CONTENT_ID);
				setStringProperty(message, ContentMessageAttributes.INSTANCE_VERSION_ID);
				setStringProperty(message, ContentMessageAttributes.AUTHENTICATED_USER);
				setStringProperty(message, ContentMessageAttributes.EFFECTIVE_USER);
				setStringProperty(message, ContentMessageAttributes.TENANT_ID);
			} catch (JMSException jmsException) {
				LOGGER.error("Cannot set diagnostic context from JMS message!", jmsException.getMessage());
				throw jmsException;
			}
		}
	}

	private static void setStringProperty(Message message, String key) throws JMSException {
		MDC.put(key, message.getStringProperty(key));
	}
}
