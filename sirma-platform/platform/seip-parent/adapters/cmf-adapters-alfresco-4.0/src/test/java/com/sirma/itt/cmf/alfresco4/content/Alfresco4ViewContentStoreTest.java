package com.sirma.itt.cmf.alfresco4.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.seip.content.ContentAdapterService;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentStoreMissMatchException;
import com.sirma.sep.content.DeleteContentData;
import com.sirma.sep.content.StoreException;
import com.sirma.sep.content.StoreItemInfo;

/**
 * Tests for {@link Alfresco4ViewContentStore}
 *
 * @author BBonev
 */
public class Alfresco4ViewContentStoreTest {

	@InjectMocks
	private Alfresco4ViewContentStore alfresco4Store;

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
		StoreItemInfo itemInfo = alfresco4Store.add(instance, descriptor);
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
		alfresco4Store.add(instance, descriptor);
	}

	@Test
	public void addContent_InvalidArgs() throws Exception {
		assertNull(alfresco4Store.add(null, null));
		Content descriptor = Content.createEmpty().setContent("test", "utf-8");
		assertNull(alfresco4Store.add(null, descriptor));
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		assertNull(alfresco4Store.add(instance, null));
	}

	@Test
	public void delete() throws Exception {
		StoreItemInfo itemInfo = createItemInfo();

		when(instanceAdapter.deleteNode(any(DMSInstance.class))).thenReturn(Boolean.TRUE);
		assertTrue(alfresco4Store.delete(itemInfo));
	}

	@Test
	public void delete_error() throws Exception {
		StoreItemInfo itemInfo = createItemInfo();

		when(instanceAdapter.deleteNode(any(DMSInstance.class))).thenThrow(new DMSException());
		assertFalse(alfresco4Store.delete(itemInfo));
	}

	@Test
	public void getReadChannel() throws Exception {
		FileDescriptor result = mock(FileDescriptor.class);
		when(contentAdapterService.getContentDescriptor("dmsId")).thenReturn(result);
		FileDescriptor descriptor = alfresco4Store.getReadChannel(createItemInfo());
		assertEquals(result, descriptor);
	}

	@Test(expected = StoreException.class)
	public void getReadChannel_error() throws Exception {
		when(contentAdapterService.getContentDescriptor("dmsId")).thenThrow(new RuntimeException());
		alfresco4Store.getReadChannel(createItemInfo());
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void getReadChannel_InvalidRequest() throws Exception {
		alfresco4Store.getReadChannel(createItemInfo().setProviderType("someProvider"));
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

		StoreItemInfo itemInfo = alfresco4Store.update(instance, content, createItemInfo());
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
		alfresco4Store.update(instance, content, createItemInfo());
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void update_ShouldFailForNullPreviousInfo() {
		alfresco4Store.update(new EmfInstance(), Content.createEmpty(), null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void update_ShouldFailForMissMatchPreviousInfo() {
		alfresco4Store.update(new EmfInstance(), Content.createEmpty(), new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void delete_ShouldFailForNullPreviousInfo() {
		alfresco4Store.delete((StoreItemInfo) null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void delete_ShouldFailForMissMatchPreviousInfo() {
		alfresco4Store.delete(new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void getReadChannel_ShouldFailForNullPreviousInfo() {
		alfresco4Store.getReadChannel(null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void getReadChannel_ShouldFailForMissMatchPreviousInfo() {
		alfresco4Store.getReadChannel(new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void prepareForDelete_ShouldFailForNullPreviousInfo() {
		alfresco4Store.prepareForDelete(null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void prepareForDelete_ShouldFailForMissMatchPreviousInfo() {
		alfresco4Store.prepareForDelete(new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void deletePreparedData_ShouldFailForNullPreviousInfo() {
		alfresco4Store.delete((DeleteContentData) null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void deletePreparedData_ShouldFailForMissMatchPreviousInfo() {
		alfresco4Store.delete(new DeleteContentData().setStoreName("random"));
	}

	private static StoreItemInfo createItemInfo() {
		return new StoreItemInfo().setProviderType(Alfresco4ViewContentStore.VIEW_STORE_NAME).setRemoteId("dmsId");
	}
}
