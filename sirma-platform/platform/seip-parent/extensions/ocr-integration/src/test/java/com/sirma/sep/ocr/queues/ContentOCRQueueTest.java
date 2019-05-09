package com.sirma.sep.ocr.queues;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirma.sep.ocr.jms.ContentOCRQueue;
import com.sirma.sep.ocr.jms.OCRContentMessageAttributes;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Test the {@link ContentOCRQueue}.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentOCRQueueTest {

	@Mock
	private SenderService senderService;
	@Mock
	private InstanceContentService instanceContentService;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private ConfigurationProperty<String> defaultOcrLanguage;
	@InjectMocks
	private ContentOCRQueue ocrQueue;

	private final Instance instance = new EmfInstance("instanceId");

	@Test
	public void should_addContentToOCRQueue() throws JMSException {
		when(defaultOcrLanguage.get()).thenReturn("en");
		Message message = mockMessage();
		ContentInfo contentInfo = mockContentInfo();
		mockInstance(InstanceType.create("document"), false);
		ocrQueue.onContentAdded(message);

		ArgumentCaptor<SendOptions> sendOptionsCaptor = ArgumentCaptor.forClass(SendOptions.class);
		verify(senderService).send(eq(ContentOCRQueue.CONTENT_OCR_QUEUE), any(InputStream.class),
				sendOptionsCaptor.capture());
		assertEquals(contentInfo.getName(),
				sendOptionsCaptor.getValue().getProperties().get(ContentCommunicationConstants.FILE_NAME));
		assertEquals(contentInfo.getMimeType(),
				sendOptionsCaptor.getValue().getProperties().get(InstanceCommunicationConstants.MIMETYPE));
		assertEquals("en",
				sendOptionsCaptor.getValue().getProperties().get(OCRContentMessageAttributes.OCR_LANGUAGE));
	}

	private static Message mockMessage() throws JMSException {
		Message message = mock(Message.class);
		when(message.getStringProperty(ContentCommunicationConstants.CONTENT_ID)).thenReturn("contentId");
		when(message.getStringProperty(ContentCommunicationConstants.PURPOSE)).thenReturn("primaryContent");
		when(message.getObjectProperty(InstanceCommunicationConstants.INSTANCE_ID)).thenReturn("instanceId");
		return message;
	}

	private ContentInfo mockContentInfo() {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getName()).thenReturn("name");
		when(contentInfo.getMimeType()).thenReturn("mimetype");
		when(contentInfo.exists()).thenReturn(Boolean.TRUE);
		when(contentInfo.getInstanceId()).thenReturn("instanceId");
		when(instanceContentService.getContent(anyString(), anyString())).thenReturn(contentInfo);
		return contentInfo;
	}

	private void mockInstance(InstanceType instanceType, boolean isImage) {
		instance.setType(instanceType);
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instanceReference.toInstance()).thenReturn(instance);
		when(instanceReference.getType()).thenReturn(instanceType);
		when(instanceTypeResolver.resolveReference(any(Serializable.class))).thenReturn(Optional.of(instanceReference));

		if (isImage) {
			when(semanticDefinitionService.getHierarchy(eq(instanceType.getId().toString())))
					.thenReturn(Collections.singletonList("image"));
		}
	}

	@Test
	public void should_readLanguageFromAttribute() throws JMSException {
		instance.add("ocrLanguage", "en");
		when(defaultOcrLanguage.get()).thenReturn("en");
		Message message = mockMessage();
		mockContentInfo();
		mockInstance(InstanceType.create("document"), false);
		ocrQueue.onContentAdded(message);

		ArgumentCaptor<SendOptions> sendOptionsCaptor = ArgumentCaptor.forClass(SendOptions.class);
		verify(senderService).send(eq(ContentOCRQueue.CONTENT_OCR_QUEUE), any(InputStream.class),
				sendOptionsCaptor.capture());
		assertEquals("en",
				sendOptionsCaptor.getValue().getProperties().get(OCRContentMessageAttributes.OCR_LANGUAGE));
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void should_failToAddContentToOCRQueueIfContentDoesNotExist() throws JMSException {
		Message message = mockMessage();
		ContentInfo contentInfo = mockContentInfo();
		mockInstance(InstanceType.create("document"), false);
		when(contentInfo.exists()).thenReturn(Boolean.FALSE);
		ocrQueue.onContentAdded(message);
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void should_failIfCannotSendMessage() throws JMSException {
		when(defaultOcrLanguage.get()).thenReturn("en");
		mockContentInfo();
		mockInstance(InstanceType.create("document"), false);
		doThrow(IOException.class).when(senderService)
				.send(eq(ContentOCRQueue.CONTENT_OCR_QUEUE), any(InputStream.class), any(SendOptions.class));
		ocrQueue.onContentAdded(mockMessage());
	}

	@Test
	public void should_notOCRImages() throws JMSException {
		Message message = mockMessage();
		mockContentInfo();
		mockInstance(InstanceType.create("image"), true);
		ocrQueue.onContentAdded(message);

		verifyZeroInteractions(senderService);
	}

	@Test
	public void should_notOCRInstancesWithoutInstanceType() throws JMSException {
		Message message = mockMessage();
		mockContentInfo();
		mockInstance(null, false);
		ocrQueue.onContentAdded(message);

		verifyZeroInteractions(senderService);
	}
}