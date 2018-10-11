package com.sirma.sep.content.preview.messaging;

import com.sirma.sep.content.preview.TestFileUtils;
import com.sirma.sep.content.preview.configuration.ContentPreviewConfiguration;
import com.sirma.sep.content.preview.model.ContentPreviewRequest;
import com.sirma.sep.content.preview.model.ContentPreviewResponse;
import com.sirma.sep.content.preview.service.ContentPreviewService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Tests the JMS {@link Message} receiving and handling in {@link JMSContentReceiver}.
 *
 * @author Mihail Radkov
 */
public class JMSContentReceiverTest {

	@Mock
	private ContentPreviewService previewService;
	@Mock
	private JMSPreviewSender previewSender;
	@Spy
	private ContentPreviewConfiguration contentPreviewConfiguration;
	@InjectMocks
	private JMSContentReceiver receiver;

	@Before
	public void beforeEach() throws IOException {
		MockitoAnnotations.initMocks(this);
		mockEmptyResponse();
		Mockito.when(previewService.isContentSupported(Matchers.anyString())).thenReturn(true);
		contentPreviewConfiguration.setTempFolder(TestFileUtils.getSystemTempDir());
	}

	@Test
	public void receivingContent_shouldSetDiagnosticContext() throws Exception {
		Message message = mockMessage(MediaType.TEXT_PLAIN_VALUE);
		receiver.receiveContentMessage(message);
		Assert.assertEquals("request-id", MDC.get(ContentMessageAttributes.REQUEST_ID));
		Assert.assertEquals("instance-id", MDC.get(ContentMessageAttributes.INSTANCE_ID));
		Assert.assertEquals("instance-version-id", MDC.get(ContentMessageAttributes.INSTANCE_VERSION_ID));
	}

	@Test
	public void receivingContent_shouldNotProceedIfTheMimetypeIsUnsupported() throws Exception {
		Message message = mockMessage(MediaType.TEXT_PLAIN_VALUE);
		Mockito.when(previewService.isContentSupported(Matchers.eq(MediaType.TEXT_PLAIN_VALUE))).thenReturn(false);
		receiver.receiveContentMessage(message);
		Mockito.verify(previewService, Mockito.times(0)).processRequest(Matchers.any(ContentPreviewRequest.class));
	}

	@Test
	public void receivingContent_shouldConstructPreviewRequest() throws Exception {
		Message message = mockMessage(MediaType.TEXT_PLAIN_VALUE);
		ArgumentCaptor<ContentPreviewRequest> requestCaptor = ArgumentCaptor.forClass(ContentPreviewRequest.class);
		Mockito.when(previewService.processRequest(requestCaptor.capture())).thenReturn(new ContentPreviewResponse());

		receiver.receiveContentMessage(message);
		Mockito.verify(previewService).processRequest(Matchers.any(ContentPreviewRequest.class));

		String mimetype = requestCaptor.getValue().getMimetype();
		Assert.assertEquals(MediaType.TEXT_PLAIN_VALUE, mimetype);

		File capturedFile = requestCaptor.getValue().getContent();
		Assert.assertTrue(capturedFile.getName().contains("instance-id"));
		Assert.assertTrue(capturedFile.getName().contains(".txt"));
	}

	@Test
	public void receivingContent_shouldUseRetryCountAsTimeoutMultiplier() throws Exception {
		Message message = mockMessage(MediaType.TEXT_PLAIN_VALUE);
		Mockito.when(message.getIntProperty(Matchers.eq(ContentMessageAttributes.JMS_DELIVERY_COUNT))).thenReturn(3);

		ArgumentCaptor<ContentPreviewRequest> requestCaptor = ArgumentCaptor.forClass(ContentPreviewRequest.class);
		Mockito.when(previewService.processRequest(requestCaptor.capture())).thenReturn(new ContentPreviewResponse());

		receiver.receiveContentMessage(message);

		Assert.assertEquals(3, requestCaptor.getValue().getTimeoutMultiplier());
	}

	@Test
	public void receivingContent_shouldFinallyCleanTheDownloadedContent() throws Exception {
		Message message = mockMessage(MediaType.TEXT_PLAIN_VALUE);
		ArgumentCaptor<ContentPreviewRequest> requestCaptor = ArgumentCaptor.forClass(ContentPreviewRequest.class);
		Mockito.when(previewService.processRequest(requestCaptor.capture())).thenReturn(new ContentPreviewResponse());

		receiver.receiveContentMessage(message);

		File capturedFile = requestCaptor.getValue().getContent();
		Assert.assertFalse(capturedFile.exists());
	}

	@Test
	public void generatedPreview_shouldBeGivenToSender() throws Exception {
		Message message = mockMessage(MediaType.TEXT_PLAIN_VALUE);
		File generatedPreview = TestFileUtils.getTempFile();
		mockResponse(generatedPreview, "");

		ContentPreviewResponse contentPreviewResponse = new ContentPreviewResponse();
		Mockito.when(previewService.processRequest(Matchers.any(ContentPreviewRequest.class)))
			   .thenReturn(contentPreviewResponse);

		receiver.receiveContentMessage(message);
		Mockito.verify(previewSender).sendPreviewResponse(Matchers.eq(message), Matchers.eq(contentPreviewResponse));
	}

	@Test
	public void shouldCleanGeneratedPreviews() throws Exception {
		File generatedPreview = TestFileUtils.getTempFile();
		Message message = mockMessage(MediaType.TEXT_PLAIN_VALUE);
		mockResponse(generatedPreview, "");
		try {
			receiver.receiveContentMessage(message);
		} finally {
			Assert.assertFalse(generatedPreview.exists());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailInCaseOfException() throws Exception {
		Message message = mockMessage(MediaType.TEXT_PLAIN_VALUE);
		Mockito.when(previewService.processRequest(Matchers.any(ContentPreviewRequest.class)))
			   .thenThrow(new IllegalArgumentException("testing exceptions"));
		receiver.receiveContentMessage(message);
	}

	@Test(expected = JMSException.class)
	public void shouldCleanGeneratedPreviewsInCaseOfException() throws Exception {
		File generatedPreview = TestFileUtils.getTempFile();
		Message message = mockMessage(MediaType.TEXT_PLAIN_VALUE);
		mockResponse(generatedPreview, "");
		Mockito.doThrow(new JMSException("Cannot send")).when(previewSender)
			   .sendPreviewResponse(Matchers.any(), Matchers.any());
		try {
			receiver.receiveContentMessage(message);
		} finally {
			Assert.assertFalse(generatedPreview.exists());
		}
	}

	private Message mockMessage(String mimetype) throws Exception {
		Message message = MessageMock.mockMessage(mimetype);

		BufferedInputStream testContent = new BufferedInputStream(new ByteArrayInputStream("test".getBytes()));
		Mockito.doAnswer(invocation -> {
			BufferedOutputStream buffer = (BufferedOutputStream) invocation.getArguments()[1];
			FileCopyUtils.copy(testContent, buffer);
			return null;
		}).when(message).setObjectProperty(Matchers.eq(ContentMessageAttributes.SAVE_STREAM),
										   Matchers.any(BufferedOutputStream.class));

		return message;
	}

	private void mockEmptyResponse() throws IOException {
		mockResponse(null, null);
	}

	private void mockResponse(File preview, String thumbnail) throws IOException {
		ContentPreviewResponse previewResponse = new ContentPreviewResponse().setPreview(preview)
																			 .setThumbnail(thumbnail);
		Mockito.when(previewService.processRequest(Matchers.any(ContentPreviewRequest.class)))
			   .thenReturn(previewResponse);
	}
}
