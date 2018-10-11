package com.sirma.sep.content.preview.messaging;

import com.sirma.sep.content.preview.TestFileUtils;
import com.sirma.sep.content.preview.configuration.ContentPreviewConfiguration;
import com.sirma.sep.content.preview.model.ContentPreviewResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.Message;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Tests the sending of JMS {@link Message} in {@link JMSPreviewSender} from provided {@link ContentPreviewResponse}
 *
 * @author Mihail Radkov
 */
public class JMSPreviewSenderTest {

	@Mock
	private JmsTemplate jmsTemplate;
	@Spy
	private ContentPreviewConfiguration contentPreviewConfiguration;
	@InjectMocks
	private JMSPreviewSender previewSender;

	@Before
	public void beforeEach() throws IOException {
		MockitoAnnotations.initMocks(this);
		contentPreviewConfiguration.setThumbnailFormat("jpeg");
	}

	@Test
	public void showSendJmsMessageWithPreview() throws Exception {
		Message receivedMessage = MessageMock.mockMessage("");
		File preview = TestFileUtils.getTempFile();
		Files.write(preview.toPath(), "testing".getBytes());
		ContentPreviewResponse previewResponse = new ContentPreviewResponse().setPreview(preview);

		previewSender.sendPreviewResponse(receivedMessage, previewResponse);
		Mockito.verify(jmsTemplate)
			   .send(Matchers.eq(JMSPreviewSender.CONTENT_PREVIEW_COMPLETED_QUEUE), Matchers.any(MessageCreator.class));
	}

	@Test
	public void shouldNotSendJmsMessageWithPreviewIfMissing() throws Exception {
		Message receivedMessage = MessageMock.mockMessage("");
		ContentPreviewResponse previewResponse = new ContentPreviewResponse();

		previewSender.sendPreviewResponse(receivedMessage, previewResponse);
		Mockito.verify(jmsTemplate, Mockito.times(0))
			   .send(Matchers.eq(JMSPreviewSender.CONTENT_PREVIEW_COMPLETED_QUEUE), Matchers.any());
	}

	@Test
	public void showSendJmsMessageWithThumbnail() throws Exception {
		Message receivedMessage = MessageMock.mockMessage("");
		ContentPreviewResponse previewResponse = new ContentPreviewResponse().setThumbnail("base64;alabala");
		previewSender.sendPreviewResponse(receivedMessage, previewResponse);
		Mockito.verify(jmsTemplate).send(Matchers.eq(JMSPreviewSender.CONTENT_THUMBNAIL_COMPLETED_QUEUE),
										 Matchers.any(MessageCreator.class));
	}

	@Test
	public void shouldNotSendJmsMessageWithThumbnailIfMissing() throws Exception {
		Message receivedMessage = MessageMock.mockMessage("");
		ContentPreviewResponse previewResponse = new ContentPreviewResponse();

		previewSender.sendPreviewResponse(receivedMessage, previewResponse);
		Mockito.verify(jmsTemplate, Mockito.times(0))
			   .send(Matchers.eq(JMSPreviewSender.CONTENT_THUMBNAIL_COMPLETED_QUEUE), Matchers.any());
	}
}
