package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.model.SerializableValue;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.event.ContentAddEvent;
import com.sirma.sep.content.event.ContentAssignedEvent;
import com.sirma.sep.content.event.ContentUpdatedEvent;
import com.sirma.sep.content.event.InstanceViewAddedEvent;
import com.sirma.sep.content.event.InstanceViewUpdatedEvent;
import com.sirma.sep.content.jms.ContentDestinations;
import com.sirma.sep.content.type.MimeTypeResolver;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Tests for {@link InstanceContentServiceImpl}.
 *
 * @author BBonev
 */
public class InstanceContentServiceImplTest {
	private static final String INSTANCE_ID = "emf:instance";

	private static final String REMOTE_SYSTEM = "remoteSystem";

	@InjectMocks
	private InstanceContentServiceImpl service;

	@Mock
	private ContentStoreProvider contentStoreProvider;
	@Mock
	private ContentStore contentStore;
	@Mock
	private ContentStore tempStore;
	@Mock
	private InstanceViewPreProcessor viewPreProcessor;
	@Mock
	private EventService eventService;
	@Mock
	private DbDao dbDao;
	@Mock
	private DatabaseIdManager idManager;
	@Mock
	private MimeTypeResolver mimeTypeResolver;
	@Mock
	private SchedulerService schedulerService;

	@Spy
	private ContentDigestProvider digestProvider = new DefaultContentDigestProvider();
	@Spy
	private IdResolver idResolver;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private SenderService senderService;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Before
	public void beforeMethod() {
		TypeConverter typeConverter = mock(TypeConverter.class);
		idResolver = new IdResolver(typeConverter);
		MockitoAnnotations.initMocks(this);
		when(securityContext.getCurrentTenantId()).thenReturn("tenant.com");
		when(contentStoreProvider.getStore(any(Instance.class), any(Content.class))).thenReturn(contentStore);
		when(contentStoreProvider.getViewStore(any(Instance.class), any(Content.class))).thenReturn(contentStore);
		when(contentStoreProvider.findStore(REMOTE_SYSTEM)).thenReturn(Optional.of(contentStore));
		when(contentStoreProvider.findStore("someOtherRemoteSystem")).thenReturn(Optional.empty());
		when(tempStore.add(any(), any())).thenReturn(mock(StoreItemInfo.class));
		when(contentStoreProvider.getTempStore()).thenReturn(tempStore);
		when(idManager.generateStringId(any(Entity.class), anyBoolean())).then(a -> {
			Entity entity = a.getArgumentAt(0, Entity.class);
			entity.setId(UUID.randomUUID().toString());
			return entity;
		});
		when(typeConverter.tryConvert(eq(String.class), any()))
				.then(a -> Objects.toString(a.getArgumentAt(1, Object.class), null));
		service.init();
	}

	@Test
	public void saveNewView() {
		String fileName = "test.html";
		Content viewDescriptor = Content
				.createEmpty()
					.setName(fileName)
					.setContent("<test>", StandardCharsets.UTF_8.name())
					.setMimeType("application/unknown")
					.setView(true)
					.setPurpose(Content.PRIMARY_VIEW);
		Instance instance = new EmfInstance();
		instance.setId(INSTANCE_ID);

		when(mimeTypeResolver.resolveFromName(anyString())).thenReturn(MediaType.APPLICATION_OCTET_STREAM);
		when(mimeTypeResolver.getMimeType(any(BufferedInputStream.class), Matchers.eq(fileName)))
				.thenReturn("text/html");

		mockNoEntity();
		when(contentStore.add(instance, viewDescriptor))
				.thenReturn(new StoreItemInfo().setProviderType(REMOTE_SYSTEM).setRemoteId("remoteId"));

		ContentInfo contentId = service.saveContent(instance, viewDescriptor);
		assertNotNull(contentId);

		verify(contentStoreProvider).getViewStore(instance, viewDescriptor);
		verify(viewPreProcessor).process(any(ViewPreProcessorContext.class));
		verify(contentStore).add(instance, viewDescriptor);
		verify(eventService).fire(any(InstanceViewAddedEvent.class));
		verify(dbDao).saveOrUpdate(any(ContentEntity.class));
		verify(tempStore, never()).add(eq(instance), any(Content.class));
		verify(mimeTypeResolver).getMimeType(any(BufferedInputStream.class), Matchers.eq(fileName));
	}

	@Test
	public void updateView() {
		Content viewDescriptor = Content
				.createEmpty()
					.setName("test.html")
					.setContent("<test>", StandardCharsets.UTF_8.name())
					.setView(true)
					.setVersionable(true)
					.setPurpose(Content.PRIMARY_VIEW);
		Instance instance = new EmfInstance();
		instance.setId(INSTANCE_ID);

		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));

		mockValidEntity();
		when(contentStore.update(eq(instance), eq(viewDescriptor), any(StoreItemInfo.class)))
				.thenReturn(new StoreItemInfo().setProviderType(REMOTE_SYSTEM).setRemoteId("remoteId"));

		ContentInfo contentId = service.saveContent(instance, viewDescriptor);
		assertNotNull(contentId);

		verify(contentStore).getReadChannel(any());
		verify(viewPreProcessor).process(any(ViewPreProcessorContext.class));
		verify(contentStore).add(eq(instance), any(Content.class));
		verify(eventService).fire(any(InstanceViewUpdatedEvent.class));
		verify(dbDao).saveOrUpdate(any(ContentEntity.class));
	}

	@Test
	public void saveNewContent() {
		Content viewDescriptor = Content
				.createEmpty()
					.setName("test.html")
					.setContent("<test>", StandardCharsets.UTF_8.name())
					.setPurpose(Content.PRIMARY_CONTENT);
		Instance instance = new EmfInstance();
		instance.setId(INSTANCE_ID);

		mockNoEntity();
		when(contentStore.add(instance, viewDescriptor))
				.thenReturn(new StoreItemInfo().setProviderType(REMOTE_SYSTEM).setRemoteId("remoteId"));

		ContentInfo info = service.saveContent(instance, viewDescriptor);
		assertNotNull(info);

		verify(contentStoreProvider).getStore(instance, viewDescriptor);
		verify(viewPreProcessor, never()).process(any(ViewPreProcessorContext.class));
		verify(contentStore).add(instance, viewDescriptor);
		verify(eventService).fire(any(ContentAddEvent.class));
		verify(dbDao).saveOrUpdate(any(ContentEntity.class));
		verify(tempStore, never()).add(eq(instance), any(Content.class));
	}

	@Test
	public void saveContent_collection() {
		Content content1 = Content
				.createEmpty()
					.setName("test.html")
					.setContent("<test>", StandardCharsets.UTF_8.name())
					.setPurpose(Content.PRIMARY_CONTENT);
		Content content2 = Content
				.createEmpty()
					.setName("test.html")
					.setContent("<test>", StandardCharsets.UTF_8.name())
					.setMimeType("text/html")
					.setView(true)
					.setPurpose(Content.PRIMARY_VIEW);

		Instance instance = new EmfInstance();
		instance.setId(INSTANCE_ID);

		mockNoEntity();
		when(contentStore.add(instance, content1))
				.thenReturn(new StoreItemInfo().setProviderType(REMOTE_SYSTEM).setRemoteId("remoteId1"));
		when(contentStore.add(instance, content2))
				.thenReturn(new StoreItemInfo().setProviderType(REMOTE_SYSTEM).setRemoteId("remoteId2"));

		List<ContentInfo> contentIds = service.saveContent(instance, Arrays.asList(content1, content2));
		assertNotNull(contentIds);
		assertEquals(2, contentIds.size());
		assertNotNull(contentIds.get(0));
		assertNotNull(contentIds.get(1));

		verify(contentStoreProvider).getStore(instance, content1);
		verify(contentStoreProvider).getViewStore(instance, content2);
		verify(viewPreProcessor).process(any(ViewPreProcessorContext.class));
		verify(contentStore).add(instance, content1);
		verify(contentStore).add(instance, content2);
		verify(eventService, times(2)).fire(any(EmfEvent.class));
		verify(dbDao, times(2)).saveOrUpdate(any(ContentEntity.class));
		verify(tempStore, never()).add(eq(instance), any(Content.class));
	}

	@Test
	public void saveContentWithInvalidDataShouldReturnNonExistingContentInfo() {
		ContentInfo info = service.saveContent(null, (Content) null);
		assertNotNull(info);
		assertFalse(info.exists());

		info = service.saveContent(null, mock(Content.class));
		assertNotNull(info);
		assertFalse(info.exists());

		info = service.saveContent(new EmfInstance(), (Content) null);
		assertNotNull(info);
		assertFalse(info.exists());

		List<ContentInfo> contentList = service.saveContent(null, (List<Content>) null);
		assertNotNull(info);
		assertTrue(contentList.isEmpty());

		contentList = service.saveContent(null, Collections.emptyList());
		assertNotNull(info);
		assertTrue(contentList.isEmpty());

		contentList = service.saveContent(new EmfInstance(), (List<Content>) null);
		assertNotNull(info);
		assertTrue(contentList.isEmpty());
	}

	@Test
	public void updateContent() {
		Content viewDescriptor = Content
				.createEmpty()
					.setName("test.html")
					.setContent("<test>", StandardCharsets.UTF_8.name())
					.setPurpose(Content.PRIMARY_CONTENT);
		Instance instance = new EmfInstance();
		instance.setId(INSTANCE_ID);

		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));

		mockValidEntity();
		when(contentStore.update(eq(instance), eq(viewDescriptor), any(StoreItemInfo.class)))
				.thenReturn(new StoreItemInfo()
						.setProviderType(REMOTE_SYSTEM)
							.setRemoteId("remoteId")
							.setAdditionalData(new HashMap<>()));

		ContentInfo contentId = service.saveContent(instance, viewDescriptor);
		assertNotNull(contentId);

		verify(viewPreProcessor, never()).process(any(ViewPreProcessorContext.class));
		verify(tempStore, never()).add(eq(instance), any(Content.class));
		verify(tempStore, never()).delete(any(StoreItemInfo.class));
		verify(contentStore).update(eq(instance), eq(viewDescriptor), any(StoreItemInfo.class));
		verify(eventService).fire(any(ContentUpdatedEvent.class));
		verify(dbDao).saveOrUpdate(any(ContentEntity.class));
	}

	@Test(expected = NullPointerException.class)
	public void updateContentShouldFailWithNPEOnNullContentId() {
		service.updateContent(null, new EmfInstance(), Content.createEmpty());
	}

	@Test
	public void updateContentShouldReturnNonExistingContentOnNullContent() {
		ContentInfo info = service.updateContent("contentId", new EmfInstance(), null);
		assertNotNull(info);
		assertFalse(info.exists());
	}

	@Test
	public void updateContentByIdShouldUpdateOnlyExistingContent() {
		Content viewDescriptor = Content
				.createEmpty()
					.setName("updated-test.html")
					.setContent("<test></test>", StandardCharsets.UTF_8.name())
					.setPurpose(Content.PRIMARY_CONTENT);
		Instance instance = new EmfInstance();
		instance.setId(INSTANCE_ID);

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));

		mockValidEntity();
		when(contentStore.update(eq(instance), eq(viewDescriptor), any(StoreItemInfo.class)))
				.thenReturn(new StoreItemInfo()
						.setProviderType(REMOTE_SYSTEM)
							.setRemoteId("remoteId")
							.setAdditionalData(new HashMap<>()));

		ContentInfo content = service.updateContent("1", instance, viewDescriptor);
		assertNotNull(content);
		assertTrue(content.exists());

		verify(viewPreProcessor, never()).process(any(ViewPreProcessorContext.class));
		verify(tempStore, never()).add(eq(instance), any(Content.class));
		verify(tempStore, never()).delete(any(StoreItemInfo.class));
		verify(contentStore).update(eq(instance), eq(viewDescriptor), any(StoreItemInfo.class));
		verify(eventService).fire(any(ContentUpdatedEvent.class));
		verify(dbDao).saveOrUpdate(any(ContentEntity.class));
	}

	@Test
	public void updateContentByIdShouldDoNothingForNonExistingContent() {
		Content viewDescriptor = Content
				.createEmpty()
					.setName("updated-test.html")
					.setContent("<test></test>", StandardCharsets.UTF_8.name())
					.setPurpose(Content.PRIMARY_CONTENT);
		Instance instance = new EmfInstance();
		instance.setId(INSTANCE_ID);

		mockNoEntity();

		ContentInfo content = service.updateContent("1", instance, viewDescriptor);
		assertNotNull(content);
		assertFalse(content.exists());

		verify(viewPreProcessor, never()).process(any(ViewPreProcessorContext.class));
		verify(tempStore, never()).add(eq(instance), any(Content.class));
		verify(tempStore, never()).delete(any(StoreItemInfo.class));
		verify(contentStore, never()).update(eq(instance), eq(viewDescriptor), any(StoreItemInfo.class));
		verify(eventService, never()).fire(any(ContentUpdatedEvent.class));
		verify(dbDao, never()).saveOrUpdate(any(ContentEntity.class));
	}

	@Test
	public void getViewContent_notFound() {
		mockNoEntity();
		ContentInfo contentInfo = service.getContent(INSTANCE_ID, Content.PRIMARY_VIEW);
		assertEquals(ContentInfo.DO_NOT_EXIST, contentInfo);
	}

	@Test
	public void getViewContent_noContentStore() {
		mockValidEntity();

		ContentInfo contentInfo = service.getContent(INSTANCE_ID, Content.PRIMARY_VIEW);
		assertNotNull(contentInfo);
		assertFalse(contentInfo.exists());
		verify(contentStoreProvider).getStore(any(StoreItemInfo.class));
	}

	@Test
	public void getViewContent_noContentFromStore() {
		mockValidEntity();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		ContentInfo contentInfo = service.getContent(INSTANCE_ID, Content.PRIMARY_VIEW);
		assertFalse(contentInfo.exists());
		verify(contentStoreProvider).getStore(any(StoreItemInfo.class));
		verify(contentStore).getReadChannel(any(StoreItemInfo.class));
	}

	@Test
	public void getViewContent_found() {
		mockValidEntity();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		FileDescriptor descriptor = mock(FileDescriptor.class);
		when(descriptor.getId()).thenReturn("remoteId");
		when(descriptor.getContainerId()).thenReturn("containerId");
		when(descriptor.getInputStream()).thenReturn(mock(InputStream.class));

		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(descriptor);
		ContentInfo contentInfo = service.getContent(INSTANCE_ID, Content.PRIMARY_VIEW);
		assertNotEquals(ContentInfo.DO_NOT_EXIST, contentInfo);
		// get the stream to trigger calling contentStore.getReadChannel as it's lazy evaluated
		assertNotNull(contentInfo.getInputStream());
		verify(contentStoreProvider).getStore(any(StoreItemInfo.class));
		verify(contentStore).getReadChannel(any(StoreItemInfo.class));

		assertEquals(1L, contentInfo.getLength());
		assertEquals("test.html", contentInfo.getName());
		assertEquals("test.html", contentInfo.getId());
		assertEquals("containerId", contentInfo.getContainerId());
		assertEquals("text/html", contentInfo.getMimeType());

		assertTrue(contentInfo.exists());

		contentInfo.close();
		verify(descriptor).close();
	}

	@Test
	public void getContent_Metadata() {
		mockValidEntity();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		FileDescriptor descriptor = mock(FileDescriptor.class);
		when(descriptor.getId()).thenReturn("remoteId");
		when(descriptor.getContainerId()).thenReturn("containerId");
		when(descriptor.getInputStream()).thenReturn(mock(InputStream.class));

		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(descriptor);
		ContentInfo contentInfo = service.getContent(INSTANCE_ID, Content.PRIMARY_CONTENT);
		assertNotEquals(ContentInfo.DO_NOT_EXIST, contentInfo);
		// get the stream to trigger calling contentStore.getReadChannel as it's lazy evaluated
		assertNotNull(contentInfo.getInputStream());
		verify(contentStoreProvider).getStore(any(StoreItemInfo.class));
		verify(contentStore).getReadChannel(any(StoreItemInfo.class));
		when(contentStore.getMetadata(any())).then(a -> {
			StoreItemInfo info = a.getArgumentAt(0, StoreItemInfo.class);
			Map<String, Serializable> map = new HashMap<>();
			map.put("key", "value");
			info.setAdditionalData((Serializable) map);
			return ContentMetadata.from(map);
		});

		assertEquals(1L, contentInfo.getLength());
		assertEquals("test.html", contentInfo.getName());
		assertEquals("test.html", contentInfo.getId());
		assertEquals("containerId", contentInfo.getContainerId());
		assertEquals("text/html", contentInfo.getMimeType());
		assertNotNull(contentInfo.getMetadata());

		assertTrue(contentInfo.exists());

		contentInfo.close();
		verify(descriptor).close();
		verify(contentStore).getMetadata(any());
		verify(dbDao).saveOrUpdate(any());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getContent_batched() {
		Collection<ContentInfo> collection = service.getContent(Collections.emptyList(), "someType");
		assertNotNull(collection);
		assertTrue(collection.isEmpty());

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		InstanceReference reference = new InstanceReferenceMock("emf:reference", mock(DataTypeDefinition.class));
		List<Serializable> ids = Arrays.asList(instance, reference, "contentId", null);

		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_CONTENTS_BY_INSTANCE_AND_PURPOSE_KEY), anyListOf(Pair.class)))
				.thenReturn(Collections.emptyList(), Collections.singletonList(createEntity()));
		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));

		collection = service.getContent(ids, null);
		assertNotNull(collection);
		assertTrue(collection.isEmpty());

		// instance with not id
		collection = service.getContent(Collections.singleton(new EmfInstance()), null);
		assertNotNull(collection);
		assertTrue(collection.isEmpty());

		collection = service.getContent(ids, "someType");
		assertNotNull(collection);
		assertFalse(collection.isEmpty());

		assertEquals(1, collection.size());
	}

	@Test
	public void getAllContentForInstanceId() {
		mockExistingContentForInstance();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));

		Collection<ContentInfo> allContent = service.getAllContent("emf:instance");
		assertNotNull(allContent);
		assertFalse(allContent.isEmpty());
		assertTrue(allContent.iterator().next().exists());
	}

	@Test
	public void getAllContentForInstance() {
		mockExistingContentForInstance();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));

		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		Collection<ContentInfo> allContent = service.getAllContent(instance);
		assertNotNull(allContent);
		assertFalse(allContent.isEmpty());
		assertTrue(allContent.iterator().next().exists());
	}

	@Test
	public void getAllContentForInstanceReference() {
		mockExistingContentForInstance();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));

		InstanceReference reference = new InstanceReferenceMock("emf:instance", EmfInstance.class);
		Collection<ContentInfo> allContent = service.getAllContent(reference);
		assertNotNull(allContent);
		assertFalse(allContent.isEmpty());
		assertTrue(allContent.iterator().next().exists());
	}

	@Test
	public void getAllContentForInvalid() {
		Collection<ContentInfo> allContent = service.getAllContent(null);
		assertNotNull(allContent);
		assertTrue(allContent.isEmpty());
	}

	@Test
	public void getContentPreview_shouldLoadPrimaryContentPreviewIfPresent() {
		mockValidEntity();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		// Mocking the read channel will mark the PRIMARY_CONTENT_PREVIEW as existing.
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));

		ContentInfo preview = service.getContentPreview("emf:instance", Content.PRIMARY_CONTENT);
		assertNotNull(preview);
		assertTrue(preview.exists());
	}

	@Test
	public void getContentPreview_ocrPuropse() {
		mockValidEntity();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getPreviewChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));

		ContentInfo preview = service.getContentPreview("emf:instance", "ocr");
		assertNotNull(preview);
		assertTrue(preview.exists());

		verify(dbDao, never()).fetchWithNamed(eq(ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY),
				argThat(CustomMatcher.ofPredicate((List<Pair<String, Object>> list) -> Content.PRIMARY_CONTENT_PREVIEW
						.equals(list.get(1).getSecond()))));

		verify(dbDao).fetchWithNamed(eq(ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY), argThat(
				CustomMatcher.ofPredicate((List<Pair<String, Object>> list) -> "ocr".equals(list.get(1).getSecond()))));
	}

	@Test
	public void getContentPreview_notFound() {
		mockNoEntity();

		ContentInfo preview = service.getContentPreview("emf:instance", Content.PRIMARY_CONTENT);
		assertNotNull(preview);
		assertFalse(preview.exists());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deleteAllContentForInstance() {
		assertFalse(service.deleteAllContentForInstance(null));

		mockSomeEntities();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));
		when(contentStore.prepareForDelete(any(StoreItemInfo.class)))
				.thenReturn(Optional.of(new DeleteContentData().setStoreName("name")));

		when(dbDao.delete(eq(ContentEntity.class), anyString())).thenReturn(1);

		assertTrue(service.deleteAllContentForInstance("emf:instance"));

		verify(contentStore, times(2)).prepareForDelete(any(StoreItemInfo.class));
		verify(dbDao, times(3)).fetchWithNamed(anyString(), anyListOf(Pair.class));
	}

	@SuppressWarnings("unchecked")
	protected void mockSomeEntities() {
		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_LATEST_CONTENT_BY_INSTANCE_KEY), anyListOf(Pair.class)))
				.thenReturn(Arrays.asList(createEntity(), createEntity()));

		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY), anyListOf(Pair.class)))
				.thenReturn(Arrays.asList(createEntity(), createEntity()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deleteAllContentForInstance_deleteOneFromStore_addOneToDeletionQueue() {
		assertFalse(service.deleteAllContentForInstance(null));

		mockSomeEntities();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));
		when(contentStore.prepareForDelete(any(StoreItemInfo.class)))
				.thenReturn(Optional.of(new DeleteContentData().setStoreName("name")), Optional.empty());

		when(dbDao.delete(eq(ContentEntity.class), anyString())).thenReturn(1);

		assertFalse(service.deleteAllContentForInstance("emf:instance"));

		verify(contentStore, times(2)).prepareForDelete(any(StoreItemInfo.class));
		verify(contentStore).delete(any(StoreItemInfo.class));
		verify(senderService).sendText(any(String.class), any(String.class), any(SendOptions.class));
		verify(dbDao, times(3)).fetchWithNamed(anyString(), anyListOf(Pair.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deleteAllContentForInstance_addAllToDeletionQueue() {
		assertFalse(service.deleteAllContentForInstance(null));

		mockSomeEntities();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));
		when(contentStore.prepareForDelete(any(StoreItemInfo.class)))
				.thenReturn(Optional.of(new DeleteContentData().setStoreName("name")));

		when(dbDao.delete(eq(ContentEntity.class), anyString())).thenReturn(1, 0);

		assertTrue(service.deleteAllContentForInstance("emf:instance"));

		verify(contentStore, times(2)).prepareForDelete(any(StoreItemInfo.class));
		verify(dbDao, times(3)).fetchWithNamed(anyString(), anyListOf(Pair.class));
		verify(senderService, times(2)).sendText(anyString(), anyString(), any(SendOptions.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deleteAllContentForInstance_referencedContents_deletedOnlyEntities() {
		assertFalse(service.deleteAllContentForInstance(null));

		mockSomeEntities();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));
		when(contentStore.prepareForDelete(any(StoreItemInfo.class)))
				.thenReturn(Optional.of(new DeleteContentData().setStoreName("name")));
		// stub references, we only need count bigger then 1
		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_CONTENTS_BY_STORE_NAME_AND_REMOTE_ID_KEY),
				anyListOf(Pair.class))).thenReturn(Arrays.asList(new ContentEntity(), new ContentEntity()));

		when(dbDao.delete(eq(ContentEntity.class), anyString())).thenReturn(1, 1);

		assertTrue(service.deleteAllContentForInstance("emf:instance"));

		verify(contentStore, times(2)).prepareForDelete(any(StoreItemInfo.class));
		verify(dbDao, times(3)).fetchWithNamed(anyString(), anyListOf(Pair.class));
		ArgumentCaptor<String> messagesCaptor = ArgumentCaptor.forClass(String.class);
		verify(senderService, times(2)).sendText(anyString(), messagesCaptor.capture(), any(SendOptions.class));
		assertTrue("All messages should have been configured not to delete the content", messagesCaptor.getAllValues()
				.stream()
				.map(DeleteContentData::fromJsonString)
				.noneMatch(DeleteContentData::isDeleteContent));
		verify(contentStore, never()).delete(any(StoreItemInfo.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deleteContent() {
		assertFalse(service.deleteContent(null, null));

		mockValidEntity();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));
		when(dbDao.delete(eq(ContentEntity.class), anyString())).thenReturn(1);
		when(contentStore.prepareForDelete(any(StoreItemInfo.class)))
				.thenReturn(Optional.of(new DeleteContentData().setStoreName("name")));

		assertTrue(service.deleteContent("emf:instance", "purpose"));

		verify(contentStore).prepareForDelete(any(StoreItemInfo.class));
		verify(dbDao, times(2)).fetchWithNamed(anyString(), anyListOf(Pair.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deleteContent_noStore() {
		mockValidEntity();
		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(null);

		assertFalse(service.deleteContent("emf:instance", "purpose"));

		// only once to extract the entity
		verify(dbDao).fetchWithNamed(anyString(), anyListOf(Pair.class));
		verifyZeroInteractions(contentStore, senderService, securityContext);
	}

	@Test
	public void deleteContent_nonExisting() {
		assertFalse(service.deleteContent("emf:instance", "purpose"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void scheduleDeleteContent_nullIdentifier() {
		service.deleteContent(null, "purpose", 1, TimeUnit.HOURS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void scheduleDeleteContent_nullPurpose() {
		service.deleteContent("id", null, 1, TimeUnit.HOURS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void scheduleDeleteContent_emptyPurpose() {
		service.deleteContent("id", "", 1, TimeUnit.HOURS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void scheduleDeleteContent_nullTimeUnit() {
		service.deleteContent("id", "purpose", 1, null);
	}

	@Test
	public void scheduleDeleteContent_successful() {
		mockSomeEntities();

		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(mock(FileDescriptor.class));
		when(dbDao.delete(eq(ContentEntity.class), anyString())).thenReturn(1);
		when(contentStore.prepareForDelete(any(StoreItemInfo.class)))
				.thenReturn(Optional.of(new DeleteContentData().setStoreName("name")));

		service.deleteContent("id", "purpose", 1, TimeUnit.HOURS);

		verify(senderService).sendText(eq(ContentDestinations.DELETE_CONTENT_QUEUE), any(String.class),
				eq(SendOptions.create().asSystem().delayWith(1, TimeUnit.HOURS)));
	}

	@Test
	public void importContent_single() {
		ContentImport contentImport = ContentImport
				.createEmpty()
					.setCharset("UTF-8")
					.setContentLength(5L)
					.setInstanceId("emf:instance")
					.setMimeType("text/plain")
					.setName("filename")
					.setPurpose(Content.PRIMARY_CONTENT)
					.setRemoteId("remoteId")
					.setRemoteSourceName(REMOTE_SYSTEM)
					.setView(true);

		assertNotNull(service.importContent(contentImport));
	}

	@Test
	public void importContent_multiple() {
		// valid
		ContentImport contentImport1 = ContentImport
				.createEmpty()
					.setCharset("UTF-8")
					.setContentLength(5L)
					.setInstanceId("emf:instance")
					.setMimeType("text/plain")
					.setName("filename")
					.setPurpose(Content.PRIMARY_CONTENT)
					.setRemoteId("remoteId")
					.setRemoteSourceName(REMOTE_SYSTEM)
					.setView(true);
		// content store not found
		ContentImport contentImport2 = ContentImport
				.createEmpty()
					.setCharset("UTF-8")
					.setContentLength(5L)
					.setInstanceId("emf:instance")
					.setMimeType("text/plain")
					.setName("filename")
					.setPurpose(Content.PRIMARY_CONTENT)
					.setRemoteId("remoteId")
					.setRemoteSourceName("someOtherRemoteSystem")
					.setView(true);
		// no instance id
		ContentImport contentImport3 = ContentImport
				.createEmpty()
					.setCharset("UTF-8")
					.setContentLength(5L)
					.setMimeType("text/plain")
					.setName("filename")
					.setPurpose(Content.PRIMARY_CONTENT)
					.setRemoteId("remoteId")
					.setRemoteSourceName(REMOTE_SYSTEM)
					.setView(true);
		// valid but no mimetype
		ContentImport contentImport4 = ContentImport
				.createEmpty()
					.setCharset("UTF-8")
					.setContentLength(5L)
					.setInstanceId("emf:instance")
					.setName("filename.txt")
					.setPurpose(Content.PRIMARY_CONTENT)
					.setRemoteId("remoteId")
					.setRemoteSourceName(REMOTE_SYSTEM)
					.setView(true);
		// for the content above that is without mimetype
		when(mimeTypeResolver.resolveFromName("filename.txt")).thenReturn("text/html");

		List<String> ids = service
				.importContent(Arrays.asList(contentImport1, contentImport2, contentImport3, contentImport4, null));
		assertNotNull(ids);
		assertNotNull(ids.get(0));
		assertNull(ids.get(1));
		assertNull(ids.get(2));
		assertNotNull(ids.get(3));
		assertNull(ids.get(4));
	}

	@Test
	public void importContent_emptyList() {
		assertTrue(service.importContent(Collections.emptyList()).isEmpty());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void assignContent() {
		when(dbDao.executeUpdate(eq(ContentEntity.ASSIGN_CONTENT_TO_INSTANCE_KEY), anyListOf(Pair.class)))
				.thenReturn(1);

		assertTrue(service.assignContentToInstance("emf:contentId", "emf:instanceId", Content.PRIMARY_CONTENT));

		verify(eventService).fire(any(ContentAssignedEvent.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void assignContent_withIncrementedVersion() {
		ContentEntity currentEntity = new ContentEntity();
		currentEntity.setVersion(4);
		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY), anyList()))
				.thenReturn(Collections.singletonList(currentEntity));
		when(dbDao.executeUpdate(eq(ContentEntity.ASSIGN_CONTENT_TO_INSTANCE_KEY),
				argThat(CustomMatcher.of((List<Pair<String, Object>> list) -> {
					assertEquals("emf:instanceId", list.get(0).getSecond());
					assertEquals("emf:contentId", list.get(1).getSecond());
					assertEquals(5, list.get(2).getSecond());
				})))).thenReturn(1);

		assertTrue(service.assignContentToInstance("emf:contentId", "emf:instanceId", Content.PRIMARY_CONTENT));

		verify(eventService).fire(any(ContentAssignedEvent.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void assignContent_invalidData() {
		when(dbDao.executeUpdate(eq(ContentEntity.ASSIGN_CONTENT_TO_INSTANCE_KEY), anyListOf(Pair.class)))
				.thenReturn(0);

		assertFalse(service.assignContentToInstance(null, null, null));
		assertFalse(service.assignContentToInstance(null, "emf:instanceId", ""));
		assertFalse(service.assignContentToInstance("emf:contentId", null, "primary"));
		assertFalse(service.assignContentToInstance("emf:contentId", "emf:instanceId", "view"));

		verify(eventService, never()).fire(any(ContentAssignedEvent.class));
	}

	@Test
	public void copyContent() {
		mockValidEntity();
		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any())).thenReturn(mock(FileDescriptor.class));

		ContentInfo info = service.copyContent(new EmfInstance(), "1");
		assertNotNull(info);
		assertTrue(info.exists());
	}

	@Test
	public void copyContent_contentDoesNotExist() {
		ContentInfo info = service.copyContent(new EmfInstance(), "1");
		assertNotNull(info);
		assertFalse(info.exists());
	}

	@Test
	public void copyContentAsync() {
		mockValidEntity();
		when(contentStoreProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
		when(contentStore.getReadChannel(any())).thenReturn(mock(FileDescriptor.class));

		when(schedulerService.buildEmptyConfiguration(any(SchedulerEntryType.class)))
				.then(a -> new DefaultSchedulerConfiguration().setType(a.getArgumentAt(0, SchedulerEntryType.class)));

		ContentInfo info = service.copyContentAsync(new EmfInstance(), "1");
		assertNotNull(info);

		verify(schedulerService).schedule(eq(AsyncContentCopyAction.NAME), any(), any());
		verify(dbDao).saveOrUpdate(any(ContentEntity.class));
	}

	@Test
	public void copyContentAsync_contentNotExists() {
		ContentInfo info = service.copyContentAsync(new EmfInstance(), "1");
		assertNotNull(info);
		assertFalse(info.exists());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void saveWithContentReuse() {
		String fileName = "test.html";
		Content viewDescriptor = Content
				.createEmpty()
					.setName(fileName)
					.setContent("<test>", StandardCharsets.UTF_8.name())
					.setMimeType("application/unknown")
					.setPurpose(Content.PRIMARY_VIEW)
					.allowReuse();
		Instance instance = new EmfInstance();
		instance.setId(INSTANCE_ID);

		when(mimeTypeResolver.resolveFromName(anyString())).thenReturn(MediaType.APPLICATION_OCTET_STREAM);
		when(mimeTypeResolver.getMimeType(any(BufferedInputStream.class), Matchers.eq(fileName)))
				.thenReturn("text/html");

		mockNoEntity();
		when(contentStore.add(instance, viewDescriptor))
				.thenReturn(new StoreItemInfo().setProviderType(REMOTE_SYSTEM).setRemoteId("remoteId"));

		ContentEntity entity = createEntity();
		entity.setChecksum("someChecksum");
		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_CONTENT_BY_CHECKSUM_KEY), anyListOf(Pair.class)))
				.thenReturn(Collections.emptyList(), Collections.singletonList(entity));

		when(idManager.isPersisted(any(Entity.class))).thenReturn(Boolean.FALSE, Boolean.TRUE);

		ContentInfo content = service.saveContent(instance, viewDescriptor);
		assertNotNull(content);
		assertTrue(content.isReuseable());
		content = service.saveContent(instance, viewDescriptor);
		assertNotNull(content);
		assertTrue(content.isReuseable());

		verify(contentStoreProvider).getStore(instance, viewDescriptor);
		verify(contentStore).add(instance, viewDescriptor);
		verify(eventService).fire(any(ContentAddEvent.class));
		verify(dbDao).saveOrUpdate(any(ContentEntity.class));
		verify(tempStore, never()).add(eq(instance), any(Content.class));
		verify(mimeTypeResolver).getMimeType(any(BufferedInputStream.class), Matchers.eq(fileName));
	}

	@SuppressWarnings("unchecked")
	private void mockExistingContentForInstance() {
		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_LATEST_CONTENT_BY_INSTANCE_KEY), anyListOf(Pair.class)))
				.thenReturn(Collections.singletonList(createEntity()));
	}

	@SuppressWarnings("unchecked")
	private void mockValidEntity() {
		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY), anyListOf(Pair.class)))
				.thenReturn(Collections.singletonList(createEntity()));
	}

	private static ContentEntity createEntity() {
		ContentEntity entity = new ContentEntity();
		entity.setId("1");
		entity.setContentSize(1L);
		entity.setPurpose(Content.PRIMARY_VIEW);
		entity.setName("test.html");
		entity.setRemoteId("remoteId");
		entity.setRemoteSourceName(REMOTE_SYSTEM);
		entity.setInstanceId(INSTANCE_ID);
		entity.setMimeType("text/html");
		entity.setCharset(StandardCharsets.UTF_8.name());
		entity.setView(Boolean.FALSE);
		entity.setAdditionalInfo(new SerializableValue(new HashMap<>()));
		entity.setVersion(0);
		return entity;
	}

	@SuppressWarnings("unchecked")
	private void mockNoEntity() {
		when(dbDao.fetchWithNamed(eq(ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY), anyListOf(Pair.class)))
				.thenReturn(Collections.emptyList());
	}
}
