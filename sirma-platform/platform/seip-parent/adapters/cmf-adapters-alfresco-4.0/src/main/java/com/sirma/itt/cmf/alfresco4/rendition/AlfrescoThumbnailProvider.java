package com.sirma.itt.cmf.alfresco4.rendition;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Base64;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.content.Alfresco4ContentStore;
import com.sirma.itt.emf.adapter.CMFRenditionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.rendition.ThumbnailProvider;

/**
 * Thumbnail provider that works with Alfresco 4 thumbnail service to provide document thumbnails.
 *
 * @author BBonev
 * @deprecated This provider is deprecated in favor of the remote thumbnail service
 */
@Named
@Deprecated
@Extension(target = ThumbnailProvider.TARGET_NAME, order = 10)
public class AlfrescoThumbnailProvider implements ThumbnailProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoThumbnailProvider.class);
	private static final String TUMBNAIL_PREFIX = "data:image/png;base64,";

	@Inject
	private CMFRenditionAdapterService cmfRenditionAdapterService;

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public String createThumbnailEndPoint(Serializable source) {
		String dmsId = null;
		ContentInfo contentInfo = instanceContentService.getContent(source, Content.PRIMARY_CONTENT);
		if (contentInfo != null && contentInfo.exists() && isStoredInAlfresco(contentInfo)) {
			dmsId = contentInfo.getRemoteId();
			LOGGER.trace("Creating thumbnail endpoint from primary content for [{}].", dmsId);
		}
		return dmsId;
	}

	@Override
	public String getThumbnail(String endPoint) {
		if (StringUtils.isBlank(endPoint)) {
			return null;
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
		int downloadThumbnail;
		try {
			downloadThumbnail = cmfRenditionAdapterService.downloadThumbnail(endPoint, buffer);
			if (downloadThumbnail > 0) {
				// prefix to the base64 string
				String thumbnail = Base64.getEncoder().encodeToString(buffer.toByteArray());
				return new StringBuilder(TUMBNAIL_PREFIX.length() + thumbnail.length()).append(TUMBNAIL_PREFIX)
						.append(thumbnail).toString();
			}
		} catch (DMSException e) {
			LOGGER.debug("No thumbnail for end point {} due to {}", endPoint, e.getMessage(), e);
			LOGGER.trace("Failed to fetch thumbnail due to", e);
		}
		return null;
	}

	@Override
	public String getName() {
		return "alfresco4Dms";
	}

	private static boolean isStoredInAlfresco(ContentInfo contentInfo) {
		return Alfresco4ContentStore.STORE_NAME.equals(contentInfo.getRemoteSourceName());
	}
}
