package com.sirma.itt.cmf.alfresco4.content;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentEntity;
import com.sirma.sep.content.ContentEntityDao;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.LocalStore;
import com.sirma.sep.content.StoreItemInfo;
import com.sirma.sep.content.event.ContentMovedEvent;
import com.sirma.sep.content.preview.jms.ContentPreviewQueue;
import com.sirma.sep.content.preview.remote.ContentPreviewRemoteService;
import com.sirma.sep.content.preview.remote.mimetype.MimeTypeSupport;
import com.sirma.sep.content.rendition.ThumbnailService;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Test for {@link ContentPreviewMigration}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 17/05/2018
 */
public class ContentPreviewMigrationTest {
	@InjectMocks
	private ContentPreviewMigration migration;
	@Mock
	private InstanceContentService contentService;
	@Mock
	private Alfresco4ContentStore alfresco4ContentStore;
	@Mock
	private ContentEntityDao contentEntityDao;
	@Mock
	private ThumbnailService thumbnailService;
	@Mock
	private SenderService senderService;
	@Mock
	private ContentPreviewRemoteService previewRemoteService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(alfresco4ContentStore.createStoreInfo()).thenReturn(
				new StoreItemInfo().setProviderType(Alfresco4ContentStore.STORE_NAME));
	}

	@Test
	public void onContentMove_doNothing_onNonExistingContentEntity() throws Exception {
		when(contentService.getContent(eq("emf:contentId"), anyString())).thenReturn(ContentInfo.DO_NOT_EXIST);

		migration.onContentMove(new ContentMovedEvent("emf:contentId", LocalStore.NAME));

		verify(alfresco4ContentStore, never()).getPreviewChannel(any());
		verify(contentService, never()).saveContent(any(), any(Content.class));
	}

	@Test
	public void onContentMove_doNothing_onNonAlfrescoStore() throws Exception {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.getRemoteSourceName()).thenReturn("tempStore");
		when(contentService.getContent(eq("emf:contentId"), anyString())).thenReturn(info);

		migration.onContentMove(new ContentMovedEvent("emf:contentId", LocalStore.NAME));

		verify(alfresco4ContentStore, never()).getPreviewChannel(any());
		verify(contentService, never()).saveContent(any(), any(Content.class));
	}

	@Test
	public void onContentMove_doNothing_onNonPrimaryContent() throws Exception {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.getContentPurpose()).thenReturn(Content.PRIMARY_VIEW);
		when(info.getRemoteSourceName()).thenReturn(Alfresco4ContentStore.STORE_NAME);
		when(contentService.getContent(eq("emf:contentId"), anyString())).thenReturn(info);

		migration.onContentMove(new ContentMovedEvent("emf:contentId", LocalStore.NAME));

		verify(alfresco4ContentStore, never()).getPreviewChannel(any());
		verify(contentService, never()).saveContent(any(), any(Content.class));
	}

	@Test
	public void onContentMove_doNothing_onAlreadyTransferredContent() throws Exception {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.getContentPurpose()).thenReturn(Content.PRIMARY_CONTENT);
		when(info.getRemoteSourceName()).thenReturn(Alfresco4ContentStore.STORE_NAME);
		when(info.getRemoteId()).thenReturn("alfrescoStoreId");
		when(info.getInstanceId()).thenReturn("emf:instanceId");
		when(contentService.getContent(eq("emf:contentId"), anyString())).thenReturn(info);
		ContentInfo previewInfo = mock(ContentInfo.class);
		when(previewInfo.exists()).thenReturn(Boolean.TRUE);
		when(contentService.getContent(anyString(), eq(Content.PRIMARY_CONTENT_PREVIEW))).thenReturn(previewInfo);
		when(contentEntityDao.getEntityByRemoteId(Alfresco4ContentStore.STORE_NAME, "alfrescoStoreId")).thenReturn(
				createEntities("emf:instanceId", "emf:instanceId-r1.0", "emf:instanceId-v1.1"));

		migration.onContentMove(new ContentMovedEvent("emf:contentId", LocalStore.NAME));

		verify(alfresco4ContentStore, never()).getPreviewChannel(any());
		verify(contentService, never()).saveContent(any(), any(Content.class));
	}

	private List<ContentEntity> createEntities(String... instanceIds) {
		return Stream.of(instanceIds).map(id -> {
			ContentEntity entity = new ContentEntity();
			entity.setInstanceId(id);
			entity.setId(UUID.randomUUID().toString());
			return entity;
		}).collect(Collectors.toList());
	}

	@Test
	public void onContentMove_createContentPreviewEntry() throws Exception {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.getContentPurpose()).thenReturn(Content.PRIMARY_CONTENT);
		when(info.getRemoteSourceName()).thenReturn(Alfresco4ContentStore.STORE_NAME);
		when(info.getRemoteId()).thenReturn("alfrescoStoreId");
		when(info.getInstanceId()).thenReturn("emf:instanceId");
		when(info.getLength()).thenReturn(10L);
		when(contentService.getContent(eq("emf:contentId"), anyString())).thenReturn(info);
		when(contentService.getContent(anyString(), eq(Content.PRIMARY_CONTENT_PREVIEW))).thenReturn(
				mock(ContentInfo.class));
		FileDescriptor preview = FileDescriptor.create(
				() -> new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), 4);
		when(alfresco4ContentStore.getPreviewChannel(any())).thenReturn(preview);
		when(contentEntityDao.getEntityByRemoteId(Alfresco4ContentStore.STORE_NAME, "alfrescoStoreId")).thenReturn(
				createEntities("emf:instanceId", "emf:instanceId-r1.0", "emf:instanceId-v1.1"));
		when(contentService.saveContent(anyString(), any(Content.class))).then(a -> {
			ContentInfo previewInfo = mock(ContentInfo.class);
			when(previewInfo.getInstanceId()).thenReturn(a.getArgumentAt(0, String.class));
			when(previewInfo.exists()).thenReturn(Boolean.TRUE);
			return previewInfo;
		});

		mockMimeTypeSupport(true, false, true);

		migration.onContentMove(new ContentMovedEvent("emf:contentId", LocalStore.NAME));

		verify(contentService).importContent(any(ContentImport.class));
	}

	@Test
	public void onContentMove_assignContentPreviewWhereMissing() throws Exception {
		ContentInfo info = mock(ContentInfo.class);
		when(info.exists()).thenReturn(Boolean.TRUE);
		when(info.getContentPurpose()).thenReturn(Content.PRIMARY_CONTENT);
		when(info.getRemoteSourceName()).thenReturn(Alfresco4ContentStore.STORE_NAME);
		when(info.getRemoteId()).thenReturn("alfrescoStoreId");
		when(info.getInstanceId()).thenReturn("emf:instanceId");
		when(info.getLength()).thenReturn(10L);
		when(contentService.getContent(eq("emf:contentId"), anyString())).thenReturn(info);
		ContentInfo previewExists = mock(ContentInfo.class);
		when(previewExists.exists()).thenReturn(Boolean.TRUE);
		when(contentService.getContent(anyString(), eq(Content.PRIMARY_CONTENT_PREVIEW))).thenReturn(previewExists,
				mock(ContentInfo.class), mock(ContentInfo.class), previewExists);
		FileDescriptor preview = FileDescriptor.create(
				() -> new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)), 4);
		when(alfresco4ContentStore.getPreviewChannel(any())).thenReturn(preview);
		when(contentEntityDao.getEntityByRemoteId(Alfresco4ContentStore.STORE_NAME, "alfrescoStoreId")).thenReturn(
				createEntities("emf:instanceId", "emf:instanceId-r1.0", "emf:instanceId-v1.1"));
		when(contentService.saveContent(anyString(), any(Content.class))).then(a -> {
			ContentInfo previewInfo = mock(ContentInfo.class);
			when(previewInfo.getInstanceId()).thenReturn(a.getArgumentAt(0, String.class));
			when(previewInfo.exists()).thenReturn(Boolean.TRUE);
			return previewInfo;
		});

		mockMimeTypeSupport(false, true, true);

		migration.onContentMove(new ContentMovedEvent("emf:contentId", LocalStore.NAME));

		verify(contentService, never()).saveContent(anyString(), any(Content.class));
		verify(contentService, times(3)).importContent(any(ContentImport.class));
		verify(senderService).send(eq(ContentPreviewQueue.CONTENT_PREVIEW_QUEUE), any(BufferedInputStream.class), any(
				SendOptions.class));
	}

	private void mockMimeTypeSupport(boolean supportsPreview, boolean isSelfPreview, boolean supportsThumbnail) {
		MimeTypeSupport mimeTypeSupport = new MimeTypeSupport();
		mimeTypeSupport.setSelfPreview(isSelfPreview);
		mimeTypeSupport.setSupportsPreview(supportsPreview);
		mimeTypeSupport.setSupportsThumbnail(supportsThumbnail);
		when(previewRemoteService.getMimeTypeSupport(any())).thenReturn(mimeTypeSupport);
	}
}
