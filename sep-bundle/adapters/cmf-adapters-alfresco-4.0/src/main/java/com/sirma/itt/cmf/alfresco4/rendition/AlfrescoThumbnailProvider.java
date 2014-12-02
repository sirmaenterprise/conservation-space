package com.sirma.itt.cmf.alfresco4.rendition;

import java.io.ByteArrayOutputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.adapter.CMFRenditionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.rendition.ThumbnailProvider;

/**
 * Thumbnail provider that works with Alfresco 4 thumbnail service to provide document thumbnails.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class AlfrescoThumbnailProvider implements ThumbnailProvider {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoThumbnailProvider.class);
	/** The Constant THUMBNAIL_PREFIX. */
	private static final String TUMBNAIL_PREFIX = "data:image/png;base64,";
	/** The cmf rendition adapter service for dms. */
	@Inject
	private CMFRenditionAdapterService cmfRenditionAdapterService;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createThumbnailEndPoint(Instance source) {
		String dmsId = null;
		if (source instanceof DocumentInstance) {
			dmsId = ((DMSInstance) source).getDmsId();
		}
		return dmsId;
	}

	/**
	 * {@inheritDoc}
	 */
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
				String thumbnail = new String(Base64.encodeBase64(buffer.toByteArray()));
				return new StringBuilder(TUMBNAIL_PREFIX.length() + thumbnail.length())
						.append(TUMBNAIL_PREFIX).append(thumbnail).toString();
			}
		} catch (DMSException e) {
			LOGGER.debug("No thumbnail for end point {} due to {}", endPoint, e.getMessage());
			LOGGER.trace("Failed to fetch thumbnail due to", e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return "alfresco4Dms";
	}

}
