package com.sirma.sep.content.preview;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirma.sep.content.preview.jms.ContentPreviewMessageAttributes;
import com.sirmaenterprise.sep.jms.api.CommunicationConstants;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Optional;

/**
 * Handy utility methods for easy creation of stubbed behaviour.
 *
 * @author Mihail Radkov
 */
public class PreviewIntegrationTestUtils {

	public static void stubInstanceTypeResolver(InstanceTypeResolver instanceTypeResolver, Instance instance) {
		if (instance == null) {
			Mockito.when(instanceTypeResolver.resolveReference(Matchers.any(Serializable.class)))
					.thenReturn(Optional.empty());
		} else {
			InstanceReference instanceReference = Mockito.mock(InstanceReference.class);
			Mockito.when(instanceReference.toInstance()).thenReturn(instance);
			Mockito.when(instanceTypeResolver.resolveReference(Matchers.any(Serializable.class)))
					.thenReturn(Optional.of(instanceReference));
		}
	}

	public static Message stubMessage(String payload, boolean blowDuringCopy) throws JMSException {
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getStringProperty(InstanceCommunicationConstants.MIMETYPE))
				.thenReturn(ContentType.APPLICATION_JSON.getMimeType());
		Mockito.when(message.getStringProperty(InstanceCommunicationConstants.INSTANCE_ID)).thenReturn("emf:123");
		Mockito.when(message.getStringProperty(ContentCommunicationConstants.CONTENT_ID)).thenReturn("content:2");
		Mockito.when(message.getStringProperty(ContentPreviewMessageAttributes.CONTENT_PREVIEW_CONTENT_ID))
				.thenReturn("content:1");
		Mockito.when(message.getStringProperty(InstanceCommunicationConstants.INSTANCE_VERSION_ID))
				.thenReturn("emf:123-v1.0");

		if (blowDuringCopy) {
			Mockito.doThrow(IOException.class)
					.when(message)
					.setObjectProperty(Matchers.eq(CommunicationConstants.JMS_SAVE_STREAM),
									   Matchers.any(BufferedOutputStream.class));
		} else {
			Mockito.doAnswer(invocation -> {
				ByteArrayInputStream source = new ByteArrayInputStream(payload.getBytes());
				OutputStream target = (OutputStream) invocation.getArguments()[1];
				IOUtils.copy(source, target);
				return null;
			})
					.when(message)
					.setObjectProperty(Matchers.eq(CommunicationConstants.JMS_SAVE_STREAM),
									   Matchers.any(OutputStream.class));
		}

		return message;
	}
}
