package com.sirma.sep.content.rendition;

import java.io.Serializable;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.preview.ContentPreviewConfigurations;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Suppressor that proxies {@link com.sirma.itt.cmf.alfresco4.rendition.AlfrescoThumbnailProvider} by having higher priority as {@link Extension}.
 * <p>
 * If {@link ContentPreviewConfigurations#isIntegrationEnabled()} is true, then {@link com.sirma.itt.cmf.alfresco4.rendition.AlfrescoThumbnailProvider} is
 * suppressed. Otherwise this class will relays requests.
 *
 * @author Mihail Radkov
 */
@Extension(target = ThumbnailProvider.TARGET_NAME, order = 10, priority = 10)
public class ThumbnailProviderSuppressor implements ThumbnailProvider {

	@Inject
	@Named("alfrescoThumbnailProvider")
	@Extension(target = ThumbnailProvider.TARGET_NAME)
	private ThumbnailProvider alfrescoThumbnailProvider;

	@Inject
	private ContentPreviewConfigurations previewConfigurations;

	@Override
	public String createThumbnailEndPoint(Serializable source) {
		if (previewConfigurations.isIntegrationEnabled().get()) {
			return null;
		}
		return alfrescoThumbnailProvider.createThumbnailEndPoint(source);
	}

	@Override
	public String getThumbnail(String endPoint) {
		if (previewConfigurations.isIntegrationEnabled().get()) {
			return null;
		}
		return alfrescoThumbnailProvider.getThumbnail(endPoint);
	}

	@Override
	public String getName() {
		if (previewConfigurations.isIntegrationEnabled().get()) {
			return "suppressor";
		}
		return alfrescoThumbnailProvider.getName();
	}
}
