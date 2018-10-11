package com.sirma.itt.cmf.alfresco4.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.seip.content.ContentAdapterService;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentStoreMissMatchException;
import com.sirma.sep.content.DeleteContentData;
import com.sirma.sep.content.StoreException;
import com.sirma.sep.content.StoreItemInfo;

/**
 * Tests for {@link Alfresco4ContentStore}
 *
 * @author BBonev
 */
public class Alfresco4ContentStoreTest {

	@InjectMocks
	private Alfresco4ContentStore alfresco4ContentStore;

	@Mock
	private ContentAdapterService contentAdapterService;
	@Mock
	private CMFDocumentAdapterService documentAdapter;
	@Mock
	private DMSInstanceAdapterService instanceAdapter;

	@Mock
	private SecurityContextManager securityContextManager;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(documentAdapter.getLibraryDMSId()).thenReturn("dmsLibrariry");
	}

	@Test
	public void should_PrepareCorrectDeleteContentData() {
		StoreItemInfo itemInfo = createItemInfo();
		DeleteContentData deleteContentData = alfresco4ContentStore.prepareForDelete(itemInfo).get();
		assertEquals(Alfresco4ContentStore.STORE_NAME, deleteContentData.getStoreName());
		assertEquals("dmsId", deleteContentData.get("dmsId"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void should_ThrowException_When_StoreItemInfoIsNull() {
		alfresco4ContentStore.prepareForDelete(null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void should_ThrowException_When_StoreItemInfoIsFromDifferentStore() {
		StoreItemInfo storeItemInfo = new StoreItemInfo().setProviderType("differentStoreName");
		alfresco4ContentStore.prepareForDelete(storeItemInfo);
	}

	@Test
	public void should_supportTwoPhaseDelete() {
		assertTrue(alfresco4ContentStore.isTwoPhaseDeleteSupported());
	}

	@Test
	public void addContent() throws Exception {
		FileAndPropertiesDescriptor result = mock(FileAndPropertiesDescriptor.class);
		when(result.getId()).thenReturn("dmsId");
		when(documentAdapter.uploadContent(any(DMSInstance.class), any(UploadWrapperDescriptor.class), anySet()))
				.thenReturn(result);
		Content descriptor = Content.createEmpty().setContent("test", "utf-8");
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		StoreItemInfo itemInfo = alfresco4ContentStore.add(instance, descriptor);
		assertNotNull(itemInfo);
		assertEquals("dmsId", itemInfo.getRemoteId());
	}

	@Test
	public void addContent_AndSyncDMSId() throws Exception {
		FileAndPropertiesDescriptor result = mock(FileAndPropertiesDescriptor.class);
		when(result.getId()).thenReturn("dmsId");
		when(result.getProperties())
				.thenReturn(Collections.singletonMap(DefaultProperties.ATTACHMENT_LOCATION, "newDMSId"));
		when(documentAdapter.uploadContent(any(DMSInstance.class), any(UploadWrapperDescriptor.class), anySet()))
				.thenReturn(result);
		Content descriptor = Content.createEmpty().setContent("test", "utf-8");
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		StoreItemInfo itemInfo = alfresco4ContentStore.add(instance, descriptor);
		assertNotNull(itemInfo);
		assertEquals("dmsId", itemInfo.getRemoteId());
	}

	@Test(expected = StoreException.class)
	public void addContent_error() throws Exception {
		when(documentAdapter.uploadContent(any(DMSInstance.class), any(UploadWrapperDescriptor.class), anySet()))
				.thenThrow(new DMSException());
		Content descriptor = Content.createEmpty().setContent("test", "utf-8");
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		alfresco4ContentStore.add(instance, descriptor);
	}

	@Test
	public void addContent_InvalidArgs() throws Exception {
		assertNull(alfresco4ContentStore.add(null, null));
		Content descriptor = Content.createEmpty().setContent("test", "utf-8");
		assertNull(alfresco4ContentStore.add(null, descriptor));
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		assertNull(alfresco4ContentStore.add(instance, null));
	}

	@Test
	public void delete() throws Exception {
		StoreItemInfo itemInfo = createItemInfo();

		when(instanceAdapter.deleteNode(any(DMSInstance.class))).thenReturn(Boolean.TRUE);
		assertTrue(alfresco4ContentStore.delete(itemInfo));
	}

	@Test
	public void delete_error() throws Exception {
		StoreItemInfo itemInfo = createItemInfo();

		when(instanceAdapter.deleteNode(any(DMSInstance.class))).thenThrow(new DMSException());
		assertFalse(alfresco4ContentStore.delete(itemInfo));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void delete_invalidData() throws Exception {
		StoreItemInfo itemInfo = createItemInfo().setProviderType("someOtherProvider");

		alfresco4ContentStore.delete(itemInfo);
	}

	@Test
	public void getReadChannel() throws Exception {
		FileDescriptor result = mock(FileDescriptor.class);
		when(contentAdapterService.getContentDescriptor("dmsId")).thenReturn(result);
		FileDescriptor descriptor = alfresco4ContentStore.getReadChannel(createItemInfo());
		assertEquals(result, descriptor);
	}

	@Test
	public void getReadChannel_noRemoteId() throws Exception {
		StoreItemInfo itemInfo = createItemInfo();
		itemInfo.setRemoteId(null);
		FileDescriptor descriptor = alfresco4ContentStore.getReadChannel(itemInfo);
		assertNull(descriptor);
		verify(contentAdapterService, times(0)).getContentDescriptor(anyString());
	}

	@Test
	public void getPreviewChannel() throws Exception {
		FileDescriptor result = mock(FileDescriptor.class);
		FileDescriptor previewDescriptor = mock(FileDescriptor.class);
		when(previewDescriptor.getId())
				.thenReturn(ServiceURIRegistry.CMF_TO_DMS_PROXY_SERVICE + ServiceURIRegistry.CONTENT_ACCESS_URI);
		when(documentAdapter.getDocumentPreview(any(), eq("application/pdf"))).thenReturn(previewDescriptor);
		when(contentAdapterService.getContentDescriptor(ServiceURIRegistry.CONTENT_ACCESS_URI.substring(1)))
				.thenReturn(result);
		FileDescriptor descriptor = alfresco4ContentStore.getPreviewChannel(createItemInfo());
		assertEquals(result, descriptor);
	}

	@Test(expected = StoreException.class)
	public void getPreviewChannel_noPreview() throws Exception {
		when(documentAdapter.getDocumentPreview(any(), eq("application/pdf"))).thenThrow(new DMSException());
		alfresco4ContentStore.getPreviewChannel(createItemInfo());
	}

	@Test(expected = StoreException.class)
	public void getReadChannel_error() throws Exception {
		when(contentAdapterService.getContentDescriptor("dmsId")).thenThrow(new RuntimeException());
		alfresco4ContentStore.getReadChannel(createItemInfo());
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void getReadChannel_InvalidRequest() throws Exception {
		alfresco4ContentStore.getReadChannel(createItemInfo().setProviderType("someProvider"));
	}

	@Test
	public void updateContent() throws Exception {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");

		Content content = Content.createEmpty().setContent("test", "utf-8");
		FileAndPropertiesDescriptor propertiesDescriptor = mock(FileAndPropertiesDescriptor.class);
		when(propertiesDescriptor.getId()).thenReturn("updatedDmsId");

		when(documentAdapter.uploadNewVersion(any(DMSInstance.class), any(UploadWrapperDescriptor.class)))
				.thenReturn(propertiesDescriptor);

		StoreItemInfo itemInfo = alfresco4ContentStore.update(instance, content, createItemInfo());
		assertNotNull(itemInfo);
		assertEquals("updatedDmsId", itemInfo.getRemoteId());
	}

	@Test(expected = StoreException.class)
	public void updateContent_error() throws Exception {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");

		Content content = Content.createEmpty().setContent("test", "utf-8");

		when(documentAdapter.uploadNewVersion(any(DMSInstance.class), any(UploadWrapperDescriptor.class)))
				.thenThrow(new DMSException());
		when(contentAdapterService.getContentDescriptor(anyString())).thenReturn(mock(FileDescriptor.class));
		alfresco4ContentStore.update(instance, content, createItemInfo());
	}

	@Test
	public void updateContent_error_successfulRecover() throws Exception {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");

		Content content = Content.createEmpty().setContent("test", "utf-8");

		when(documentAdapter.uploadNewVersion(any(DMSInstance.class), any(UploadWrapperDescriptor.class)))
				.thenThrow(new DMSException());
		when(contentAdapterService.getContentDescriptor(anyString())).thenReturn(null);
		FileAndPropertiesDescriptor result = mock(FileAndPropertiesDescriptor.class);
		when(result.getId()).thenReturn("dmsId");
		when(documentAdapter.uploadContent(any(DMSInstance.class), any(UploadWrapperDescriptor.class), anySet()))
				.thenReturn(result);
		StoreItemInfo info = alfresco4ContentStore.update(instance, content, createItemInfo());
		assertNotNull(info);
		assertEquals("dmsId", info.getRemoteId());
	}

	@Test(expected = IllegalArgumentException.class)
	public void update_ShouldFailForInstance() {
		alfresco4ContentStore.update(null, Content.createEmpty(), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void update_ShouldFailForNullContent() {
		alfresco4ContentStore.update(new EmfInstance(), null, null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void update_ShouldFailForNullPreviousInfo() {
		alfresco4ContentStore.update(new EmfInstance(), Content.createEmpty(), null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void update_ShouldFailForMissMatchPreviousInfo() {
		alfresco4ContentStore.update(new EmfInstance(), Content.createEmpty(), new StoreItemInfo().setProviderType("random"));
	}

	private static StoreItemInfo createItemInfo() {
		return new StoreItemInfo().setProviderType(Alfresco4ContentStore.STORE_NAME).setRemoteId("dmsId");
	}

	private ContextualExecutor buildContextualExecutor(boolean result) {
		ContextualExecutor executor = Mockito.mock(ContextualExecutor.class);
		Mockito.when(executor.function(Matchers.any(), Matchers.any())).thenReturn(result);
		return executor;
	}

	private DeleteContentData buildDeleteContentData() {
		return new DeleteContentData().setTenantId("someTenantId").addProperty("dmsId", "someDmsId");
	}
}
