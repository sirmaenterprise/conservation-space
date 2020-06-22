package com.sirma.sep.content.rendition;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.content.preview.ContentPreviewConfigurations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests the suppression logic in {@link ThumbnailProviderSuppressor}
 *
 * @author Mihail Radkov
 */
public class ThumbnailProviderSuppressorTest {

	@Mock
	private ThumbnailProvider alfrescoThumbnailProvider;

	@Mock
	private ContentPreviewConfigurations previewConfigurations;

	@InjectMocks
	private ThumbnailProviderSuppressor thumbnailProviderSuppressor;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		stubAlfrescoProvider();
	}

	@Test
	public void shouldSuppressAlfresco() {
		stubPreviewConfigurations(true);
		Assert.assertNull(thumbnailProviderSuppressor.createThumbnailEndPoint(new EmfInstance()));
		Assert.assertNull(thumbnailProviderSuppressor.getThumbnail(""));
		Assert.assertEquals("suppressor", thumbnailProviderSuppressor.getName());
	}

	@Test
	public void shouldProxyAlfresco() {
		stubPreviewConfigurations(false);
		Assert.assertEquals("endpoint", thumbnailProviderSuppressor.createThumbnailEndPoint(new EmfInstance()));
		Assert.assertEquals("thumbnail", thumbnailProviderSuppressor.getThumbnail(""));
		Assert.assertEquals("alfresco", thumbnailProviderSuppressor.getName());
	}

	private void stubPreviewConfigurations(boolean enabled) {
		Mockito.when(previewConfigurations.isIntegrationEnabled()).thenReturn(new ConfigurationPropertyMock<>
																					  (enabled));
	}

	private void stubAlfrescoProvider() {
		Mockito.when(alfrescoThumbnailProvider.createThumbnailEndPoint(Matchers.any(Instance.class)))
				.thenReturn("endpoint");
		Mockito.when(alfrescoThumbnailProvider.getThumbnail(Matchers.anyString())).thenReturn("thumbnail");
		Mockito.when(alfrescoThumbnailProvider.getName()).thenReturn("alfresco");
	}
}
