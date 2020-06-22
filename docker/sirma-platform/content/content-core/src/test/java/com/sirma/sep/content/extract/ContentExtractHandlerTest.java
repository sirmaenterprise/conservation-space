package com.sirma.sep.content.extract;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.itt.seip.rest.exceptions.HTTPClientRuntimeException;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentNotFoundRuntimeException;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Test for {@link ContentExtractHandlerTest}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 05/07/2018
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentExtractHandlerTest {
	@InjectMocks
	private ContentExtractHandler handler;

	@Mock
	private InstanceContentService instanceContentService;
	@Mock
	private ContentExtractor contentExtractor;
	@Mock
	private SenderService senderService;

	@Test
	public void onContent() throws Exception {
		stubContentService(Boolean.TRUE);
		handler.onContent(buildMessage("emf:instanceId", "emf:contentId"));
		verify(contentExtractor).extractAndPersist(eq("emf:instanceId"), any(ContentInfo.class));
		verifyZeroInteractions(senderService);
	}

	private void stubContentService(Boolean exists) throws IOException {
		ContentInfo content = mock(ContentInfo.class);
		when(content.getMimeType()).thenReturn("text/html");
		when(content.asString()).thenReturn("text");
		when(content.exists()).thenReturn(exists);
		when(content.isIndexable()).thenReturn(Boolean.TRUE);
		when(instanceContentService.getContent("emf:contentId", null)).thenReturn(content);
	}

	@Test(expected = ContentNotFoundRuntimeException.class)
	public void onContent_ShouldFailOnNonExistingContent() throws Exception {
		stubContentService(Boolean.FALSE);
		handler.onContent(buildMessage("emf:instanceId", "emf:contentId"));
		verifyZeroInteractions(senderService);
	}

	private static Message buildMessage(String instanceId, String contentId) throws JMSException {
		Message message = mock(Message.class);
		when(message.getStringProperty(ContentCommunicationConstants.CONTENT_ID)).thenReturn(contentId);
		when(message.getStringProperty(InstanceCommunicationConstants.INSTANCE_ID)).thenReturn(instanceId);
		return message;
	}

	@Test
	public void onContent_rescheduleExtractionWhenContentUnavailable() throws Exception {
		stubContentService(Boolean.TRUE);
		when(contentExtractor.extractAndPersist(any(Serializable.class), any(ContentInfo.class)))
				.thenThrow(new HTTPClientRuntimeException());
		handler.onContent(buildMessage("emf:instanceId", "emf:contentId"));
		verify(senderService).sendObject(anyString(), any(), any(), any());
	}

	@Test
	public void delayedQueueProcessor() throws Exception {
		stubContentService(Boolean.TRUE);
		handler.delayedQueueProcessor(buildMessage("emf:instanceId", "emf:contentId"));
		verify(contentExtractor).extractAndPersist(eq("emf:instanceId"), any(ContentInfo.class));
		verifyZeroInteractions(senderService);
	}
}