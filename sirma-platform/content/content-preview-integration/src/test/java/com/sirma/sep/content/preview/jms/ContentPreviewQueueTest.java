package com.sirma.sep.content.preview.jms;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.messaging.InstanceCommunicationConstants;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirma.sep.content.preview.ContentPreviewConfigurations;
import com.sirma.sep.content.preview.PreviewIntegrationTestUtils;
import com.sirma.sep.content.preview.remote.ContentPreviewRemoteService;
import com.sirma.sep.content.preview.remote.mimetype.MimeTypeSupport;
import com.sirma.sep.content.rendition.RenditionService;
import com.sirma.sep.content.rendition.ThumbnailService;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Tests the control flow in {@link ContentPreviewRemoteService } when receiving {@link Message}s.
 *
 * @author Mihail Radkov
 */
public class ContentPreviewQueueTest {

	private static final String APP_JSON = ContentType.APPLICATION_JSON.getMimeType();

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private ThumbnailService thumbnailService;

	@Mock
	private SenderService senderService;

	@Mock
	private ContentPreviewRemoteService previewRemoteService;

	@Mock
	private ContentPreviewConfigurations previewConfigurations;

	@InjectMocks
	private ContentPreviewQueue contentPreviewQueue;

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
		stubContentService(stubContentInfo(true, 5));
		PreviewIntegrationTestUtils.stubInstanceTypeResolver(instanceTypeResolver, getTestInstance());
		stubRemotePreviewService(APP_JSON, true, false, true);
		stubContentPreviewConfigurations(true);
	}

	@Test
	public void shouldIgnoreEmptyContent() throws JMSException {
		stubContentService(stubContentInfo(true, 0));
		contentPreviewQueue.onContentAssigned(stubMessage());
		verifyNoMessages();
	}

	@Test(expected = EmfRuntimeException.class)
	public void shouldBlowOnMissingContent() throws JMSException {
		stubContentService(stubContentInfo(false, 0));
		contentPreviewQueue.onContentAssigned(stubMessage());
	}

	@Test(expected = EmfRuntimeException.class)
	public void shouldBlowOnMissingInstance() throws JMSException {
		stubContentService(stubContentInfo(true, 5));
		PreviewIntegrationTestUtils.stubInstanceTypeResolver(instanceTypeResolver, null);
		contentPreviewQueue.onContentAssigned(stubMessage());
	}

	@Test
	public void shouldNotScheduleIfDisabled() throws JMSException {
		stubContentPreviewConfigurations(false);
		contentPreviewQueue.onContentAssigned(stubMessage());
		verifyNoContentImport();
		verifyNoMessages();
	}

	@Test
	public void shouldStoreContentAsSelfPreview() throws JMSException {
		stubRemotePreviewService(APP_JSON, false, true, false);
		ArgumentCaptor<ContentImport> importCaptor = ArgumentCaptor.forClass(ContentImport.class);

		contentPreviewQueue.onContentAssigned(stubMessage());
		Mockito.verify(instanceContentService, Mockito.times(2)).importContent(importCaptor.capture());

		List<ContentImport> capturedImports = importCaptor.getAllValues();

		ContentImport imported = capturedImports.get(0);
		assertContentImport(imported, 5L, "emf:123", "test.txt");

		ContentImport importedVersion = capturedImports.get(1);
		assertContentImport(importedVersion, 5L, "emf:123-v1.0", "test.txt");

		verifyNoMessages();
	}

	@Test
	public void shouldDeleteThumbnailsIfMimetypeDoesntSupportThem() throws JMSException {
		stubRemotePreviewService(APP_JSON, true, false, false);
		contentPreviewQueue.onContentAssigned(stubMessage());

		Mockito.verify(thumbnailService)
				.removeThumbnail(Matchers.eq("emf:123"), Matchers.eq(RenditionService.DEFAULT_PURPOSE));
		Mockito.verify(thumbnailService)
				.removeThumbnail(Matchers.eq("emf:123-v1.0"), Matchers.eq(RenditionService.DEFAULT_PURPOSE));
	}

	@Test
	public void shouldScheduleGenerationForThumbnailOnly() throws JMSException, IOException {
		stubRemotePreviewService(APP_JSON, false, false, true);
		ArgumentCaptor<SendOptions> sendOptionsCaptor = ArgumentCaptor.forClass(SendOptions.class);

		contentPreviewQueue.onContentAssigned(stubMessage());
		Mockito.verify(instanceContentService, Mockito.times(0)).importContent(Matchers.any(ContentImport.class));
		Mockito.verify(senderService)
				.send(Matchers.eq(ContentPreviewQueue.CONTENT_PREVIEW_QUEUE), Matchers.any(InputStream.class),
					  sendOptionsCaptor.capture());

		SendOptions sendOptions = sendOptionsCaptor.getValue();
		Map<String, Serializable> properties = sendOptions.getProperties();
		assertCommonSendProperties(properties);

		// Shouldn't have prepared a preview content
		Assert.assertFalse(properties.containsKey(ContentPreviewMessageAttributes.CONTENT_PREVIEW_CONTENT_ID));
		verifyNoContentImport();
	}

	@Test
	public void shouldScheduleGenerationForPreviewAndThumbnail() throws JMSException {
		stubRemotePreviewService(APP_JSON, true, false, true);
		ArgumentCaptor<ContentImport> importCaptor = ArgumentCaptor.forClass(ContentImport.class);
		ArgumentCaptor<SendOptions> sendOptionsCaptor = ArgumentCaptor.forClass(SendOptions.class);

		contentPreviewQueue.onContentAssigned(stubMessage());
		Mockito.verify(instanceContentService).importContent(importCaptor.capture());
		Mockito.verify(senderService)
				.send(Matchers.eq(ContentPreviewQueue.CONTENT_PREVIEW_QUEUE), Matchers.any(InputStream.class),
					  sendOptionsCaptor.capture());

		SendOptions sendOptions = sendOptionsCaptor.getValue();
		Map<String, Serializable> properties = sendOptions.getProperties();
		assertCommonSendProperties(properties);
		assertProperty(properties, ContentPreviewMessageAttributes.CONTENT_PREVIEW_CONTENT_ID, "content:1");

		List<ContentImport> importedContent = importCaptor.getAllValues();
		ContentImport preview = importedContent.get(0);
		assertContentImport(preview, null, "emf:123", "test.txt");
	}

	@Test(expected = EmfRuntimeException.class)
	public void shouldBlowWhileSendingMessage() throws JMSException {
		stubRemotePreviewService(APP_JSON, true, false, true);
		Mockito.doThrow(IOException.class)
				.when(senderService)
				.send(Matchers.eq(ContentPreviewQueue.CONTENT_PREVIEW_QUEUE), Matchers.any(InputStream.class),
					  Matchers.any(SendOptions.class));
		contentPreviewQueue.onContentAssigned(stubMessage());
	}

	private Message stubMessage() throws JMSException {
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getStringProperty(ContentCommunicationConstants.CONTENT_ID)).thenReturn("content:123");
		Mockito.when(message.getStringProperty(ContentCommunicationConstants.PURPOSE))
				.thenReturn(Content.PRIMARY_CONTENT);
		return message;
	}

	private ContentInfo stubContentInfo(boolean exists, long length) {
		ContentInfo contentInfo = Mockito.mock(ContentInfo.class);
		Mockito.when(contentInfo.exists()).thenReturn(exists);
		Mockito.when(contentInfo.getLength()).thenReturn(length);
		Mockito.when(contentInfo.getInstanceId()).thenReturn("emf:123");
		Mockito.when(contentInfo.getMimeType()).thenReturn(APP_JSON);
		Mockito.when(contentInfo.getName()).thenReturn("test.txt");
		Mockito.when(contentInfo.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
		return contentInfo;
	}

	private Instance getTestInstance() {
		Instance instance = new EmfInstance("emf:123");
		instance.add(DefaultProperties.VERSION, "1.0");
		return instance;
	}

	private void verifyNoMessages() {
		Mockito.verify(senderService, Mockito.times(0))
				.send(Matchers.eq(ContentPreviewQueue.CONTENT_PREVIEW_QUEUE), Matchers.any(),
					  Matchers.any(SendOptions.class));
	}

	private void verifyNoContentImport() {
		Mockito.verify(instanceContentService, Mockito.times(0)).importContent(Matchers.any(ContentImport.class));
	}

	private void assertCommonSendProperties(Map<String, Serializable> properties) {
		assertProperty(properties, InstanceCommunicationConstants.MIMETYPE, APP_JSON);
		assertProperty(properties, InstanceCommunicationConstants.INSTANCE_ID, "emf:123");
		assertProperty(properties, ContentCommunicationConstants.FILE_NAME, "test");
		assertProperty(properties, ContentCommunicationConstants.FILE_EXTENSION, ".txt");
	}

	private void assertProperty(Map<String, Serializable> properties, String key, String value) {
		Assert.assertTrue(properties.containsKey(key));
		Assert.assertEquals(value, properties.get(key));
	}

	private void assertContentImport(ContentImport contentImport, Long length, String instanceId, String name) {
		Assert.assertNotNull(contentImport);
		Assert.assertEquals(Content.PRIMARY_CONTENT_PREVIEW, contentImport.getPurpose());
		Assert.assertEquals(length, contentImport.getContentLength());
		Assert.assertEquals(instanceId, contentImport.getInstanceId());
		Assert.assertEquals(name, contentImport.getName());
	}

	private void stubContentService(ContentInfo contentInfo) {
		Mockito.when(instanceContentService.getContent(Matchers.any(Serializable.class), Matchers.anyString()))
				.thenReturn(contentInfo);
		Mockito.when(instanceContentService.importContent(Matchers.any(ContentImport.class)))
				.thenReturn("content:1")
				.thenReturn("content:2");
	}

	private void stubRemotePreviewService(String mimetype, boolean preview, boolean selfPreview, boolean thumbnail) {
		MimeTypeSupport support = new MimeTypeSupport();
		support.setSupportsPreview(preview);
		support.setSelfPreview(selfPreview);
		support.setSupportsThumbnail(thumbnail);
		Mockito.when(previewRemoteService.getMimeTypeSupport(Matchers.eq(mimetype))).thenReturn(support);
	}

	private void stubContentPreviewConfigurations(boolean enabled) {
		Mockito.when(previewConfigurations.isIntegrationEnabled()).thenReturn(new ConfigurationPropertyMock<>
																					  (enabled));
	}
}
