package com.sirma.itt.cmf.alfresco4.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentAdapterService;
import com.sirma.itt.seip.content.StoreException;
import com.sirma.itt.seip.content.StoreItemInfo;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;

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

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(documentAdapter.getLibraryDMSId()).thenReturn("dmsLibrariry");
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
				.thenReturn(Collections.singletonMap(DocumentProperties.ATTACHMENT_LOCATION, "newDMSId"));
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
				.thenThrow(DMSException.class);
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

		when(instanceAdapter.deleteNode(any(DMSInstance.class))).thenThrow(DMSException.class);
		assertFalse(alfresco4ContentStore.delete(itemInfo));
	}

	@Test
	public void delete_invalidData() throws Exception {
		StoreItemInfo itemInfo = createItemInfo().setProviderType("someOtherProvider");

		assertFalse(alfresco4ContentStore.delete(itemInfo));
	}

	@Test
	public void getReadChannel() throws Exception {
		FileDescriptor result = mock(FileDescriptor.class);
		when(contentAdapterService.getContentDescriptor("dmsId")).thenReturn(result);
		FileDescriptor descriptor = alfresco4ContentStore.getReadChannel(createItemInfo());
		assertEquals(result, descriptor);
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
		when(documentAdapter.getDocumentPreview(any(), eq("application/pdf"))).thenThrow(DMSException.class);
		alfresco4ContentStore.getPreviewChannel(createItemInfo());
	}

	@Test(expected = StoreException.class)
	public void getReadChannel_error() throws Exception {
		when(contentAdapterService.getContentDescriptor("dmsId")).thenThrow(RuntimeException.class);
		alfresco4ContentStore.getReadChannel(createItemInfo());
	}

	@Test
	public void getReadChannel_InvalidRequest() throws Exception {
		assertNull(alfresco4ContentStore.getReadChannel(createItemInfo().setProviderType("someProvider")));
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
				.thenThrow(DMSException.class);

		alfresco4ContentStore.update(instance, content, createItemInfo());
	}

	@Test
	public void updateContent_invalidData() throws Exception {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");

		Content content = Content.createEmpty().setContent("test", "utf-8");

		assertNull(alfresco4ContentStore.update(null, null, null));
		assertNull(alfresco4ContentStore.update(null, null, createItemInfo().setProviderType("someOtherProvider")));
		assertNull(alfresco4ContentStore.update(instance, null, createItemInfo()));
		assertNull(alfresco4ContentStore.update(null, content, createItemInfo()));
		assertNull(alfresco4ContentStore.update(null, null, createItemInfo()));
	}

	private static StoreItemInfo createItemInfo() {
		return new StoreItemInfo().setProviderType(Alfresco4ContentStore.STORE_NAME).setRemoteId("dmsId");
	}
}
