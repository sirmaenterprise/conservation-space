package com.sirma.sep.ocr.queues;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentPersister;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.TextExtractor;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirma.sep.ocr.jms.ContentOCRCompleteQueue;
import com.sirma.sep.ocr.jms.OCRContentMessageAttributes;
import com.sirmaenterprise.sep.jms.api.CommunicationConstants;

/**
 * Tests for the content ocr complete queue.
 *
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentOCRCompleteQueueTest {

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private TempFileProvider tempFileProvider;

	@Mock
	private ContentPersister contentPersister;

	@Mock
	private TextExtractor textExtractor;

	@InjectMocks
	private ContentOCRCompleteQueue ocrCompleteQueue;

	@Test
	public void should_saveOCRedContent_onOCRCompleted() throws JMSException, IOException, URISyntaxException {
		String mimetype = "text/plain";
		String oceredText = "text extracted from ocr";
		String instanceId = "instanceId";

		BytesMessage message = mock(BytesMessage.class);
		when(message.getStringProperty(ContentCommunicationConstants.FILE_NAME)).thenReturn("fileName");
		when(message.getStringProperty(ContentCommunicationConstants.FILE_EXTENSION)).thenReturn("fileExtension");
		when(message.getStringProperty(InstanceCommunicationConstants.MIMETYPE)).thenReturn(mimetype);
		when(message.getStringProperty(InstanceCommunicationConstants.INSTANCE_ID)).thenReturn(instanceId);
		when(message.getStringProperty(OCRContentMessageAttributes.OCRED_CONTENT_ID)).thenReturn("ocredContentId");

		File ocredFile = new File(this.getClass().getResource("test.pdf").toURI());
		when(tempFileProvider.createTempFile(anyString(), anyString()))
				.thenReturn(ocredFile);
		when(instanceContentService.updateContent(anyString(), any(Serializable.class), any(Content.class)))
				.thenReturn(mock(ContentInfo.class));

		when(textExtractor.extract(Matchers.eq(mimetype), Matchers.any(FileDescriptor.class))).thenReturn(Optional.of(oceredText));

		ocrCompleteQueue.onContentOCRDoneMessage(message);

		verify(message).setObjectProperty(eq(CommunicationConstants.JMS_SAVE_STREAM), any(OutputStream.class));
		verify(contentPersister).saveOcrContent(instanceId, oceredText);
	}
}