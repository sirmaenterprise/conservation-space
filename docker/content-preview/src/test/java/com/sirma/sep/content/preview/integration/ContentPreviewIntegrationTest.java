package com.sirma.sep.content.preview.integration;

import com.sirma.sep.content.preview.TestFileUtils;
import com.sirma.sep.content.preview.configuration.ContentPreviewConfiguration;
import com.sirma.sep.content.preview.messaging.ContentMessageAttributes;
import com.sirma.sep.content.preview.messaging.JMSContentReceiver;
import com.sirma.sep.content.preview.messaging.JMSPreviewSender;
import com.sirma.sep.content.preview.generator.ContentPreviewGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration test for verifying the {@link com.sirma.sep.content.preview.ContentPreviewApplication} flow of:
 * <ol>
 * <li>Receiving JMS {@link Message}</li>
 * <li>Handling {@link com.sirma.sep.content.preview.model.ContentPreviewRequest}</li>
 * <li>Sending back {@link com.sirma.sep.content.preview.model.ContentPreviewResponse} as JMS {@link Message}</li>
 * </ol>
 *
 * @author Mihail Radkov
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class ContentPreviewIntegrationTest {

	// TODO: This test is disabled until we migrate to ActiveMQ Artemis

	private static final Long JMS_TIMEOUT = 3000L;

	@MockBean
	private ContentPreviewGenerator contentPreviewGenerator;

	@Autowired
	private ContentPreviewConfiguration contentPreviewConfiguration;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Before
	public void beforeEach() throws IOException {
		jmsTemplate.setReceiveTimeout(JMS_TIMEOUT);

		contentPreviewConfiguration.setTempFolder(TestFileUtils.getSystemTempDir());

		File preview = TestFileUtils.getTempFile();
		Files.write(preview.toPath(), "the preview".getBytes());
		Mockito.when(contentPreviewGenerator.generatePreview(Matchers.any(File.class), Matchers.anyInt())).thenReturn(preview);

		File thumbnail = TestFileUtils.getTempFile();
		Files.write(thumbnail.toPath(), "thumbnail".getBytes());
		Mockito.when(contentPreviewGenerator.generateThumbnail(Matchers.any(File.class))).thenReturn(thumbnail);
	}

	@Test
	public void testReceiveAndSend() {
		MessageCreator messageCreator = session -> getTestMessage(session, "text/plain");
		jmsTemplate.send(JMSContentReceiver.CONTENT_PREVIEW_QUEUE, messageCreator);

		Message receivedPreview = jmsTemplate.receive(JMSPreviewSender.CONTENT_PREVIEW_COMPLETED_QUEUE);
		Assert.assertNotNull("Did not receive preview", receivedPreview);
		assertAttributes(receivedPreview, getTestAttributes("application/pdf"));
		assertMessageContent(receivedPreview, "the preview");

		Message receivedThumbnail = jmsTemplate.receive(JMSPreviewSender.CONTENT_THUMBNAIL_COMPLETED_QUEUE);
		Assert.assertNotNull("Did not receive thumbnail", receivedThumbnail);
		assertAttributes(receivedThumbnail, getTestAttributes("image/jpeg"));
		assertMessageContent(receivedThumbnail, Base64Utils.encodeToString("thumbnail".getBytes()));
	}

	@Test
	public void testReceiveAndSendForPdf() {
		MessageCreator messageCreator = session -> getTestMessage(session, "application/pdf");
		jmsTemplate.send(JMSContentReceiver.CONTENT_PREVIEW_QUEUE, messageCreator);

		Message receivedPreview = jmsTemplate.receive(JMSPreviewSender.CONTENT_PREVIEW_COMPLETED_QUEUE);
		Assert.assertNull("Received preview for a PDF", receivedPreview);

		Message receivedThumbnail = jmsTemplate.receive(JMSPreviewSender.CONTENT_THUMBNAIL_COMPLETED_QUEUE);
		Assert.assertNotNull("Did not receive thumbnail", receivedThumbnail);
		assertAttributes(receivedThumbnail, getTestAttributes("image/jpeg"));
	}

	// TODO: Add tests in the case no preview/thumbnail is generated...

	private static Message getTestMessage(Session session, String mimetype) throws JMSException {
		BytesMessage bytesMessage = session.createBytesMessage();

		BufferedInputStream testContent = new BufferedInputStream(new ByteArrayInputStream("test".getBytes()));
		bytesMessage.setObjectProperty("JMS_AMQ_InputStream", testContent);

		setAttributes(bytesMessage, getTestAttributes(mimetype));

		return bytesMessage;
	}

	private static void assertAttributes(Message message, Map<String, String> attributes) {
		attributes.forEach((key, value) -> {
			try {
				Assert.assertEquals(value, message.getStringProperty(key));
			} catch (JMSException e) {
				Assert.fail();
			}
		});
	}

	private static void assertMessageContent(Message message, String content) {
		String messageContent = null;
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			message.setObjectProperty(ContentMessageAttributes.SAVE_STREAM, outputStream);
			messageContent = new String(outputStream.toByteArray());
		} catch (JMSException | IOException e) {
			Assert.fail();
		}
		Assert.assertEquals(content, messageContent);
	}

	private static void setAttributes(Message message, Map<String, String> attributes) {
		attributes.forEach((name, value) -> {
			try {
				message.setStringProperty(name, value);
			} catch (JMSException e) {
				Assert.fail();
			}
		});
	}

	private static Map<String, String> getTestAttributes(String mimetype) {
		Map<String, String> attributes = new HashMap<>(11);
		ContentMessageAttributes.STRING_ATTRIBUTES.forEach(key -> attributes.put(key, key));
		attributes.put(ContentMessageAttributes.MIMETYPE, mimetype);
		return attributes;
	}
}
