package com.sirma.sep.content.jms;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.IdResolver;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.event.ContentAssignedEvent;
import com.sirma.sep.content.event.ContentUpdatedEvent;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.convert.ObjectMessageWriter;
import com.sirmaenterprise.sep.jms.exception.JmsRuntimeException;

/**
 * Test the content topic.
 * 
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentTopicTest {

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private SenderService senderService;

	@Mock
	private IdResolver idResolver;

	@InjectMocks
	private ContentTopic contentTopic;

	@Test
	public void should_sendMessage_onContentAssigned() throws JMSException {
		String contentId = "content";
		String instanceId = "instance";
		ContentInfo content = mockInstanceContentService(contentId, "purpose");
		ContentAssignedEvent event = new ContentAssignedEvent(instanceId, contentId);

		contentTopic.onContentAssigned(event);
		verifyMessageAddedToTopic(instanceId, content);
	}

	@Test
	public void should_sendMessage_onContentUpdate() throws JMSException {
		String contentId = "content";
		String instanceId = "instance";
		when(idResolver.resolve(any(Serializable.class))).thenReturn(Optional.of(instanceId));
		ContentInfo content = mockInstanceContentService(contentId, "purpose");
		ContentUpdatedEvent event = new ContentUpdatedEvent(instanceId,
				Content.createFrom(content).setContentId(contentId), mock(ContentInfo.class), content);

		contentTopic.onContentUpdated(event);
		verifyMessageAddedToTopic(instanceId, content);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = JmsRuntimeException.class)
	public void should_throwException_onFailedMessageConstruction() throws JMSException {
		String contentId = "content";
		String instanceId = "instance";
		mockInstanceContentService(contentId, "purpose");
		ContentAssignedEvent event = new ContentAssignedEvent(instanceId, contentId);

		contentTopic.onContentAssigned(event);

		ArgumentCaptor<ObjectMessageWriter<Map<String, Message>>> messageInitializerCaptor = ArgumentCaptor
				.forClass(ObjectMessageWriter.class);
		ArgumentCaptor<Map<String, Message>> argumentsCaptor = ArgumentCaptor.forClass(Map.class);
		verify(senderService).sendObject(eq(ContentDestinations.CONTENT_TOPIC), argumentsCaptor.capture(),
				messageInitializerCaptor.capture(), any(SendOptions.class));

		ObjectMessage message = Mockito.mock(ObjectMessage.class);
		doThrow(new JMSException("reason")).when(message).setObjectProperty(anyString(), anyObject());
		messageInitializerCaptor.getValue().write(argumentsCaptor.getValue(), message);
	}
	
	@Test
	public void should_notAddUnresolvableInstance_toTopic() {
		String contentId = "content";
		String instanceId = "instance";
		when(idResolver.resolve(any(Serializable.class))).thenReturn(Optional.empty());
		ContentInfo content = mockInstanceContentService(contentId, "purpose");
		ContentUpdatedEvent event = new ContentUpdatedEvent(instanceId,
				Content.createFrom(content).setContentId(contentId), mock(ContentInfo.class), content);

		contentTopic.onContentUpdated(event);
		verifyZeroInteractions(senderService);
	}

	@SuppressWarnings("unchecked")
	private void verifyMessageAddedToTopic(String instanceId, ContentInfo content) throws JMSException {
		ArgumentCaptor<ObjectMessageWriter<Map<String, Message>>> messageInitializerCaptor = ArgumentCaptor
				.forClass(ObjectMessageWriter.class);
		ArgumentCaptor<Map<String, Message>> argumentsCaptor = ArgumentCaptor.forClass(Map.class);
		verify(senderService).sendObject(eq(ContentDestinations.CONTENT_TOPIC), argumentsCaptor.capture(),
				messageInitializerCaptor.capture(), any(SendOptions.class));

		ObjectMessage message = Mockito.mock(ObjectMessage.class);

		// Run the captured messageInitializer. It should put the contet's info in the mocked
		// message.
		messageInitializerCaptor.getValue().write(argumentsCaptor.getValue(), message);

		verify(message).setObjectProperty(InstanceCommunicationConstants.INSTANCE_ID, instanceId);
		verify(message).setObjectProperty(ContentCommunicationConstants.PURPOSE, content.getContentPurpose());
		verify(message).setObjectProperty(ContentCommunicationConstants.CONTENT_ID, content.getContentId());
	}

	private ContentInfo mockInstanceContentService(String contentId, String purpose) {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getContentId()).thenReturn(contentId);
		when(contentInfo.getContentPurpose()).thenReturn(purpose);
		when(instanceContentService.getContent(eq(contentId), eq(null))).thenReturn(contentInfo);
		return contentInfo;
	}

}
