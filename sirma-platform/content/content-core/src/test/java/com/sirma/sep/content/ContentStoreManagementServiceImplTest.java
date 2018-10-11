package com.sirma.sep.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.fakes.InteractiveTransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.sep.content.jms.ContentDestinations;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Test for {@link ContentStoreManagementServiceImpl}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 02/01/2018
 */
public class ContentStoreManagementServiceImplTest {
	private static final String STORE_NAME = "storeName";
	private static final String NEW_STORE_NAME = "newStore";
	@InjectMocks
	private ContentStoreManagementServiceImpl contentManagementService;

	@Spy
	private ConfigurationProperty<Integer> storeProcessingBatchSize = new ConfigurationPropertyMock<>(100);
	@Mock
	private ContentEntityDao contentEntityDao;
	@Spy
	InstanceProxyMock<SenderService> senderServiceProxy = new InstanceProxyMock<>();
	@Mock
	private SenderService senderService;
	@Mock
	private MessageSender messageSender;
	@Mock
	private SecurityContext securityContext;
	@Spy
	private InteractiveTransactionSupportFake transactionSupport = new InteractiveTransactionSupportFake();
	@Mock
	private ContentStoreProvider contentStoreProvider;
	@Mock
	private ContentStore contentStore;
	@Mock
	private ContentStore newContentStore;
	@Mock
	private EventService eventService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(securityContext.getCurrentTenantId()).thenReturn("tenant.id");
		when(contentStoreProvider.findStore(STORE_NAME)).thenReturn(Optional.of(contentStore));
		when(contentStoreProvider.findStore(NEW_STORE_NAME)).thenReturn(Optional.of(newContentStore));
		when(contentStore.prepareForDelete(any())).then(a -> Optional.of(new DeleteContentData().setStoreName(
				STORE_NAME)));
		when(newContentStore.prepareForDelete(any())).then(a -> Optional.of(new DeleteContentData().setStoreName(
				NEW_STORE_NAME)));
		when(contentStore.getName()).thenReturn(STORE_NAME);
		senderServiceProxy.set(senderService);
		when(senderService.createSender(ContentDestinations.DELETE_CONTENT_QUEUE,
				SendOptions.create().asSystem())).thenReturn(messageSender);
		when(senderService.createSender(ContentDestinations.MOVE_CONTENT_QUEUE,
				SendOptions.create().asTenantAdmin())).thenReturn(messageSender);
		when(messageSender.getDefaultSendOptions()).then(a -> SendOptions.create());
	}

	private void mockContentEntities(String storeName, int count) {
		when(contentEntityDao.getStoreContents(eq(storeName), anyInt(), anyInt())).then(a -> {
			Integer offset = a.getArgumentAt(1, Integer.class);
			Integer limit = a.getArgumentAt(2, Integer.class);
			return IntStream.range(offset, Math.min(offset + limit, count)).boxed().map(id -> createEntity(id,
					storeName)).collect(Collectors.toList());
		});
	}

	private ContentEntity createEntity(Integer id, String storeName) {
		ContentEntity entity = new ContentEntity();
		entity.setId("emf:content-" + id);
		entity.setRemoteSourceName(storeName);
		entity.setContentSize((long) (id * 1024));
		entity.setRemoteId("" + id);
		entity.setInstanceId("emf:574984-acb-def4-" + Integer.toString(id, 16));
		return entity;
	}

	@Test
	public void emptyContentStore() throws Exception {
		mockContentEntities(STORE_NAME, 512);

		contentManagementService.emptyContentStore(STORE_NAME);

		verify(messageSender, times(512)).sendText(anyString());
		verify(senderService, times(6)).createSender(ContentDestinations.DELETE_CONTENT_QUEUE,
				SendOptions.create().asSystem());
	}

	@Test
	public void moveAllContent() throws Exception {
		mockContentEntities(STORE_NAME, 512);

		ContentStoreManagementService.StoreInfo storeInfo = contentManagementService.moveAllContent(STORE_NAME,
				NEW_STORE_NAME);

		assertNotNull(storeInfo);
		assertEquals(STORE_NAME, storeInfo.getStoreName());
		assertEquals(512, storeInfo.getNumberOfFiles());
		// calculate the sum of numbers 1..n -> (n * (n - 1)) /2
		assertEquals(((512 * 511) / 2) * 1024, storeInfo.getTotalSize());

		ArgumentCaptor<SendOptions> captor = ArgumentCaptor.forClass(SendOptions.class);
		verify(messageSender, times(512)).sendText(anyString(), captor.capture());

		List<SendOptions> allValues = captor.getAllValues();
		long even = allValues.stream().map(op -> op.getProperties().get("index").toString()).map(Integer::valueOf).filter(idx -> idx == 0).count();
		long odd = allValues.stream().map(op -> op.getProperties().get("index").toString()).map(Integer::valueOf).filter(idx -> idx == 1).count();
		assertEquals(even, odd);

		verify(senderService, times(6)).createSender(ContentDestinations.MOVE_CONTENT_QUEUE,
				SendOptions.create().asTenantAdmin());
	}

	@Test
	public void moveContent_shouldCopyContentAndDeleteOriginal() {
		when(contentEntityDao.getEntity(eq("1"), anyString())).thenReturn(createEntity(1, STORE_NAME));
		when(contentStore.getReadChannel(any())).thenReturn(
				FileDescriptor.create(() -> new ByteArrayInputStream("test".getBytes(
						StandardCharsets.UTF_8)), 4));
		when(newContentStore.add(any(), any())).then(a -> new StoreItemInfo().setProviderType(NEW_STORE_NAME)
				.setRemoteId("new-id-1")
				.setContentLength(a.getArgumentAt(1, Content.class).getContentLength()));
		when(contentEntityDao.getEntityByRemoteId(STORE_NAME, "1")).thenReturn(
				Arrays.asList(createEntity(1, STORE_NAME), createEntity(2, STORE_NAME), createEntity(22, STORE_NAME)));

		transactionSupport.executeInTx(() ->
				contentManagementService.moveContent("1", NEW_STORE_NAME)
		);

		// should add content to the new store
		verify(newContentStore).add(any(), any());
		// should delete the original content
		verify(contentStore).prepareForDelete(any());
		// update 3 entities that have same remote id
		verify(contentEntityDao, times(3)).persistEntity(any(), eq(false));
		// should delete the copied content on transaction fail. Generally this should not be called always but this is
		// test limitation and not a bug
		verify(newContentStore, never()).prepareForDelete(any());
	}

	@Test
	public void moveContent_shouldFailIfNoContentIsFound() {
		transactionSupport.executeInTx(() ->
				contentManagementService.moveContent("1", NEW_STORE_NAME)
		);

		// should add content to the new store
		verify(newContentStore, never()).add(any(), any());
		// should delete the original content
		verify(contentStore, never()).prepareForDelete(any());
		// update 3 entities that have same remote id
		verify(contentEntityDao, never()).persistEntity(any(), eq(false));
		// should delete the copied content on transaction fail. Generally this should not be called always but this is
		// test limitation and not a bug
		verify(newContentStore, never()).prepareForDelete(any());
	}

	@Test
	public void moveContent_shouldDoNothingIfEntityInTheSameStore() {
		when(contentEntityDao.getEntity(eq("1"), anyString())).thenReturn(createEntity(1, NEW_STORE_NAME));
		when(contentStore.getReadChannel(any())).thenReturn(
				FileDescriptor.create(() -> new ByteArrayInputStream("test".getBytes(
						StandardCharsets.UTF_8)), 4));
		when(newContentStore.add(any(), any())).then(a -> new StoreItemInfo().setProviderType(NEW_STORE_NAME)
				.setRemoteId("new-id-1")
				.setContentLength(a.getArgumentAt(1, Content.class).getContentLength()));
		when(contentEntityDao.getEntityByRemoteId(STORE_NAME, "1")).thenReturn(
				Arrays.asList(createEntity(1, STORE_NAME), createEntity(2, STORE_NAME), createEntity(22, STORE_NAME)));

		transactionSupport.executeInTx(() ->
				contentManagementService.moveContent("1", NEW_STORE_NAME)
		);

		// should add content to the new store
		verify(newContentStore, never()).add(any(), any());
		// should delete the original content
		verify(contentStore, never()).prepareForDelete(any());
		// update 3 entities that have same remote id
		verify(contentEntityDao, never()).persistEntity(any(), eq(false));
		// should delete the copied content on transaction fail. Generally this should not be called always but this is
		// test limitation and not a bug
		verify(newContentStore, never()).prepareForDelete(any());
	}

	@Test(expected = ContentNotFoundRuntimeException.class)
	public void moveContent_shouldFailIfContentDoesNotExistInStore() {
		when(contentEntityDao.getEntity(eq("1"), anyString())).thenReturn(createEntity(1, STORE_NAME));
		when(contentStore.getReadChannel(any())).thenReturn(null);

		transactionSupport.executeInTx(() ->
				contentManagementService.moveContent("1", NEW_STORE_NAME)
		);
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void moveContent_shouldFailIfContentNotFullyCopied() {
		when(contentEntityDao.getEntity(eq("1"), anyString())).thenReturn(createEntity(1, STORE_NAME));
		when(contentStore.getReadChannel(any())).thenReturn(
				FileDescriptor.create(() -> new ByteArrayInputStream("test".getBytes(
						StandardCharsets.UTF_8)), 4));
		when(newContentStore.add(any(), any())).then(a -> new StoreItemInfo().setProviderType(NEW_STORE_NAME)
				.setRemoteId("new-id-1").setContentLength(0));
		when(contentEntityDao.getEntityByRemoteId(STORE_NAME, "1")).thenReturn(
				Arrays.asList(createEntity(1, STORE_NAME), createEntity(2, STORE_NAME), createEntity(22, STORE_NAME)));

		transactionSupport.executeInTx(() ->
				contentManagementService.moveContent("1", NEW_STORE_NAME)
		);

		// should add content to the new store
		verify(newContentStore).add(any(), any());
		// should delete the original content
		verify(contentStore, never()).prepareForDelete(any());
		// update 3 entities that have same remote id
		verify(contentEntityDao, never()).persistEntity(any(), eq(false));
		// should delete the copied content on transaction fail. Generally this should not be called always but this is
		// test limitation and not a bug
		verify(newContentStore, never()).prepareForDelete(any());
	}

	@Test
	public void moveContent_shouldDeleteCopiedContentIfTransactionFailsByExternalCause() {
		when(contentEntityDao.getEntity(eq("1"), anyString())).thenReturn(createEntity(1, STORE_NAME));
		when(contentStore.getReadChannel(any())).thenReturn(
				FileDescriptor.create(() -> new ByteArrayInputStream("test".getBytes(
						StandardCharsets.UTF_8)), 4));
		when(newContentStore.add(any(), any())).then(a -> new StoreItemInfo().setProviderType(NEW_STORE_NAME)
				.setRemoteId("new-id-1")
				.setContentLength(a.getArgumentAt(1, Content.class).getContentLength()));
		when(contentEntityDao.getEntityByRemoteId(STORE_NAME, "1")).thenReturn(
				Arrays.asList(createEntity(1, STORE_NAME), createEntity(2, STORE_NAME), createEntity(22, STORE_NAME)));

		transactionSupport.beginTx();
		contentManagementService.moveContent("1", NEW_STORE_NAME);
		transactionSupport.rollbackTx();

		// should add content to the new store
		verify(newContentStore).add(any(), any());
		// should delete the original content
		verify(contentStore).prepareForDelete(any());
		// update 3 entities that have same remote id
		verify(contentEntityDao, times(3)).persistEntity(any(), eq(false));
		// should delete the copied content on transaction fail. Generally this should not be called always but this is
		// test limitation and not a bug
		verify(newContentStore).prepareForDelete(any());
	}
}
