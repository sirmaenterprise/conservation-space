package com.sirma.sep.content.preview.jms;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentEntity;
import com.sirma.sep.content.ContentEntityDao;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.jms.ContentCommunicationConstants;
import com.sirma.sep.content.preview.PreviewIntegrationTestUtils;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests the logic in {@link ContentPreviewCompletedQueue} after a preview is generated for {@link Content} and received
 * as {@link Message}.
 *
 * @author Mihail Radkov
 */
public class ContentPreviewCompletedQueueTest {

	public static final String CONTENT_ID = "content:1";
	public static final String ORIGINAL_CONTENT_ID = "content:2";
	public static final String INSTANCE_ID = "emf:123";
	public static final String INSTANCE_VERSION_ID = "emf:123-v1.0";
	@Mock
	private TempFileProvider tempFileProvider;

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private ContentEntityDao contentEntityDao;

	@InjectMocks
	private ContentPreviewCompletedQueue contentPreviewCompletedQueue;

	@Before
	public void initialize() throws IOException {
		MockitoAnnotations.initMocks(this);
		mockTempFileProvider();
	}

	@Test
	public void shouldUpdateContentWithGeneratedPreview_backwardCompatibility() throws JMSException, IOException {
		mockInstanceContentPreview(CONTENT_ID, mockContentInfo(true));
		mockInstanceContentPreview(INSTANCE_VERSION_ID, mockContentInfo(false));
		when(instanceContentService.updateContent(eq(CONTENT_ID), any(Instance.class), any())).then(a -> mockContentInfo(true));
		mockInstanceContent(INSTANCE_ID, mockContentInfo(true));
		when(contentEntityDao.getEntityByRemoteId("remote-content-id", "remote-store")).thenReturn(createEntities(
				INSTANCE_ID));
		Message message = stubMessage();
		when(message.getStringProperty(ContentCommunicationConstants.CONTENT_ID)).thenReturn(null);
		contentPreviewCompletedQueue.onContentPreviewCompleted(message);

		ArgumentCaptor<Content> previewCaptor = ArgumentCaptor.forClass(Content.class);
		verify(instanceContentService)
				.updateContent(eq(CONTENT_ID), any(Instance.class), previewCaptor.capture());

		Content preview = previewCaptor.getValue();
		assertContent(preview);

		verify(instanceContentService).importContent(any(ContentImport.class));
	}

	@Test
	public void shouldUpdateContentWithGeneratedPreview() throws JMSException, IOException {
		mockInstanceContentPreview(CONTENT_ID, mockContentInfo(true));
		mockInstanceContentPreview(INSTANCE_ID, mockContentInfo(true));
		mockInstanceContentPreview(INSTANCE_VERSION_ID, mockContentInfo(true));
		when(instanceContentService.updateContent(eq(CONTENT_ID), any(Instance.class), any())).then(a -> mockContentInfo(true));
		mockInstanceContent(ORIGINAL_CONTENT_ID, mockContentInfo(true));
		when(contentEntityDao.getEntityByRemoteId("remote-store", "remote-content-id" )).thenReturn(createEntities(
				INSTANCE_ID, INSTANCE_VERSION_ID));

		contentPreviewCompletedQueue.onContentPreviewCompleted(stubMessage());

		ArgumentCaptor<Content> previewCaptor = ArgumentCaptor.forClass(Content.class);
		verify(instanceContentService)
				.updateContent(eq(CONTENT_ID), any(Instance.class), previewCaptor.capture());

		Content preview = previewCaptor.getValue();
		assertContent(preview);

		verify(instanceContentService, never()).importContent(any(ContentImport.class));
	}

	private List<ContentEntity> createEntities(String... instanceIds) {
		return Stream.of(instanceIds).map(id -> {
			ContentEntity entity = new ContentEntity();
			entity.setInstanceId(id);
			entity.setId(id);
			return entity;
		}).collect(Collectors.toList());
	}

	@Test
	public void shouldUpdateContentForRelatedInstances() throws JMSException, IOException {
		mockInstanceContentPreview(CONTENT_ID, mockContentInfo(true));
		when(instanceContentService.updateContent(eq(CONTENT_ID), any(Instance.class), any())).then(a -> mockContentInfo(true));

		ContentInfo viewContent = mockContentInfo(true);
		when(viewContent.getInstanceId()).thenReturn(INSTANCE_ID);
		mockInstanceContent(ORIGINAL_CONTENT_ID, viewContent);
		when(contentEntityDao.getEntityByRemoteId("remote-store", "remote-content-id")).thenReturn(
				createEntities(INSTANCE_ID, "emf:123-v1.0", "emf:123-r1.0", "emf:123-r1.0-v1.0"));
		ContentInfo viewNotExists = mockContentInfo(false);
		mockInstanceContentPreview(INSTANCE_ID, viewNotExists);
		mockInstanceContentPreview("emf:123-v1.0", viewNotExists);
		mockInstanceContentPreview("emf:123-r1.0", viewNotExists);
		mockInstanceContentPreview("emf:123-r1.0-v1.0", mockContentInfo(true));
		contentPreviewCompletedQueue.onContentPreviewCompleted(stubMessage());

		verify(instanceContentService, times(2)).importContent(any(ContentImport.class));
	}

	private void assertContent(Content content) throws IOException {
		Assert.assertNotNull(content);
		Assert.assertEquals(ContentType.APPLICATION_JSON.getMimeType(), content.getMimeType());
		Assert.assertEquals("test", content.getContent().asString());
	}

	@Test(expected = EmfRuntimeException.class)
	public void shouldNotUpdateMissingContent() throws JMSException {
		mockInstanceContentPreview(CONTENT_ID, mockContentInfo(false));
		contentPreviewCompletedQueue.onContentPreviewCompleted(stubMessage());
	}

	@Test(expected = EmfRuntimeException.class)
	public void shouldBlowDuringPreviewDownload() throws JMSException {
		mockInstanceContentPreview(CONTENT_ID, mockContentInfo(true));
		Message message = PreviewIntegrationTestUtils.stubMessage("test", true);
		contentPreviewCompletedQueue.onContentPreviewCompleted(message);
	}

	private Message stubMessage() throws JMSException {
		return PreviewIntegrationTestUtils.stubMessage("test", false);
	}

	private ContentInfo mockContentInfo(boolean exists) {
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.getInstanceId()).thenReturn(INSTANCE_ID);
		when(contentInfo.getContentId()).thenReturn(exists ? "content-id" : null);
		when(contentInfo.getRemoteId()).thenReturn(exists ? "remote-content-id" : null);
		when(contentInfo.getRemoteSourceName()).thenReturn(exists ? "remote-store" : null);
		when(contentInfo.exists()).thenReturn(exists);
		return contentInfo;
	}

	private void mockInstanceContentPreview(String contentId, ContentInfo contentInfo) {
		when(instanceContentService.getContent(eq(contentId), eq(Content.PRIMARY_CONTENT_PREVIEW)))
				.thenReturn(contentInfo);
	}

	private void mockInstanceContent(String instanceId, ContentInfo contentInfo) {
		when(instanceContentService.getContent(eq(instanceId), eq(Content.PRIMARY_CONTENT)))
				.thenReturn(contentInfo);
	}

	private void mockTempFileProvider() throws IOException {
		File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
		tempFile.deleteOnExit();
		when(tempFileProvider.createTempFile(anyString(), anyString())).thenReturn(tempFile);
	}
}
