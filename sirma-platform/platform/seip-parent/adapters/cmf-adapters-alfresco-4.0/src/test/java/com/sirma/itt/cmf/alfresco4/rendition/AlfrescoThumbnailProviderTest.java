package com.sirma.itt.cmf.alfresco4.rendition;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.cmf.alfresco4.content.Alfresco4ContentStore;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Tests the thumbnail provisioning in {@link AlfrescoThumbnailProvider}.
 *
 * @author Mihail Radkov
 */
public class AlfrescoThumbnailProviderTest {

	@Mock
	private InstanceContentService instanceContentService;

	@InjectMocks
	private AlfrescoThumbnailProvider alfrescoThumbnailProvider;

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests when there is a content info for the document's primary content.
	 */
	@Test
	public void testCreatingThumbnailEndpointWithPrimaryContent() {
		Instance instance = getTestInstance();
		ContentInfo info = getContentInfo(true, "remoteId", Alfresco4ContentStore.STORE_NAME);
		mockInstanceContentService("emf:123", info);

		String endPoint = alfrescoThumbnailProvider.createThumbnailEndPoint(instance);
		Assert.assertEquals("remoteId", endPoint);
	}

	/**
	 * Tests when there is non existent content info for the document's primary content.
	 */
	@Test
	public void testCreatingThumbnailEndpointWithoutExsistingContentInfo() {
		Instance instance = getTestInstance();
		ContentInfo info = getContentInfo(false, null, null);
		mockInstanceContentService("emf:123", info);

		String endPoint = alfrescoThumbnailProvider.createThumbnailEndPoint(instance);
		Assert.assertNull(endPoint);
	}

	/**
	 * Tests when there is no content info for the document's primary content.
	 */
	@Test
	public void testCreatingThumbnailEndpointWithoutContentInfo() {
		Instance instance = getTestInstance();
		mockInstanceContentService("emf:123", null);

		String endPoint = alfrescoThumbnailProvider.createThumbnailEndPoint(instance);
		Assert.assertNull(endPoint);
	}

	/**
	 * Tests when an instance different from a document is given for end point creation.
	 */
	@Test
	public void testCreatingThumbnailEndpointForNonDocumentInstance() {
		Instance instance = new EmfInstance();

		String endPoint = alfrescoThumbnailProvider.createThumbnailEndPoint(instance);
		Assert.assertNull(endPoint);
	}

	private void mockInstanceContentService(String instanceId, ContentInfo info) {
		Mockito.when(instanceContentService.getContent(Matchers.eq(instanceId), Matchers.eq(Content.PRIMARY_CONTENT)))
				.thenReturn(info);
	}

	private static Instance getTestInstance() {
		Instance instance = new EmfInstance();
		instance.setId("emf:123");
		return instance;
	}

	private static ContentInfo getContentInfo(boolean exists, String remoteId, String remoteSystem) {
		ContentInfo info = Mockito.mock(ContentInfo.class);
		Mockito.when(info.exists()).thenReturn(exists);
		Mockito.when(info.getRemoteId()).thenReturn(remoteId);
		Mockito.when(info.getRemoteSourceName()).thenReturn(remoteSystem);
		return info;
	}
}
