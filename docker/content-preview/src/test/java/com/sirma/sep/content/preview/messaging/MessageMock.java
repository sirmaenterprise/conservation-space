package com.sirma.sep.content.preview.messaging;

import org.mockito.Mockito;

import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;

/**
 * Helper class to produce mocked {@link Message} for use of {@link com.sirma.sep.content.preview.ContentPreviewApplication}
 * tests.
 *
 * @author Mihail Radkov
 */
public class MessageMock {

	/**
	 * Mocks a JMS {@link Message} by assigning specific {@link String} attributes.
	 *
	 * @param mimetype
	 * 		- predefined mimetype to be assigned.
	 * @return mocked {@link Message}
	 * @throws JMSException
	 * 		- in case the {@link Message} cannot be mocked
	 */
	public static Message mockMessage(String mimetype) throws JMSException {
		Message message = Mockito.mock(Message.class);

		Mockito.when(message.getStringProperty(ContentMessageAttributes.MIMETYPE)).thenReturn(mimetype);
		Mockito.when(message.getStringProperty(ContentMessageAttributes.FILE_NAME)).thenReturn("text");
		Mockito.when(message.getStringProperty(ContentMessageAttributes.FILE_EXTENSION)).thenReturn(".txt");
		Mockito.when(message.getStringProperty(ContentMessageAttributes.REQUEST_ID)).thenReturn("request-id");
		Mockito.when(message.getStringProperty(ContentMessageAttributes.INSTANCE_ID)).thenReturn("instance-id");
		Mockito.when(message.getStringProperty(ContentMessageAttributes.CONTENT_ID)).thenReturn("content-id");
		Mockito.when(message.getStringProperty(ContentMessageAttributes.INSTANCE_VERSION_ID))
			   .thenReturn("instance-version-id");
		Mockito.when(message.getStringProperty(ContentMessageAttributes.AUTHENTICATED_USER))
			   .thenReturn("auth-user");
		Mockito.when(message.getStringProperty(ContentMessageAttributes.EFFECTIVE_USER))
			   .thenReturn("effective-user");
		Mockito.when(message.getStringProperty(ContentMessageAttributes.TENANT_ID)).thenReturn("tenant-id");

		return message;
	}
}
