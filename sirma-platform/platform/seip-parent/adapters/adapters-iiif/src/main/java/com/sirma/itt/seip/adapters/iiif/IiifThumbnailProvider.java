package com.sirma.itt.seip.adapters.iiif;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Base64;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.rendition.ThumbnailProvider;

/**
 * Thumbnail provider for images located at the IIIF server. The requested thumbnails height is restricted to 64 pixels
 * and allow the width to change depending on the aspect ratio.
 *
 * @author BBonev
 */
@Extension(target = ThumbnailProvider.TARGET_NAME, order = 5)
public class IiifThumbnailProvider implements ThumbnailProvider {
	private static final String TUMBNAIL_PREFIX = "data:image/jpg;base64,";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private ImageServerConfigurations imageServerConfigurations;
	@Inject
	private InstanceContentService contentService;
	@Inject
	private RESTClient restClient;

	@Override
	public String createThumbnailEndPoint(Instance source) {
		if (!imageServerConfigurations.isImageServerEnabled().get().booleanValue()) {
			return null;
		}
		if (source != null) {
			ContentInfo info = contentService.getContent(source, Content.PRIMARY_CONTENT);
			if ((info.exists() || info.getContentId() != null) && ImageServerConfigurations.IIIF.equals(info.getRemoteSourceName())) {
				return FileUtil.getName(info.getRemoteId());
			}
		}
		return null;
	}

	@Override
	public String getThumbnail(String endPoint) {
		if (!imageServerConfigurations.isImageServerEnabled().get().booleanValue()) {
			return null;
		}
		URI urlToImageInfo = getRequestUri(endPoint);
		return getImageInfo(urlToImageInfo);
	}

	private URI getRequestUri(String endPoint) {
		// restrict image height to 64 pixels and allow the width to change depending on the aspect ratio
		StringBuilder urlToImageInfo = new StringBuilder(256)
				.append(imageServerConfigurations.getIiifServerAddress().requireConfigured().get())
					.append(endPoint)
					.append(StringUtils.trimToEmpty(imageServerConfigurations.getIiifServerAddressSuffix().get()))
					.append("/full/,64/0/default.jpg");
		try {
			return new URI(urlToImageInfo.toString(), false);
		} catch (Exception e1) {
			throw new EmfRuntimeException(e1);
		}
	}

	private String getImageInfo(URI urlToImage) {
		try {
			HttpMethod request = restClient.rawRequest(new GetMethod(), urlToImage);
			return readContent(request);
		} catch (Exception e) {
			LOGGER.debug("Could not fetch thumbnail for image {}, due to: {}", urlToImage, e.getMessage());
			LOGGER.trace("Failed to fetch thumbnail due to", e);
		}
		return null;
	}

	private static String readContent(HttpMethod request) {
		try (InputStream inputStream = request.getResponseBodyAsStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream(4096)) {

			long size = IOUtils.copyLarge(inputStream, bos);
			if (size <= 0) {
				return null;
			}

			String thumbnail = Base64.getEncoder().encodeToString(bos.toByteArray());
			return new StringBuilder(TUMBNAIL_PREFIX.length() + thumbnail.length())
					.append(TUMBNAIL_PREFIX)
						.append(thumbnail)
						.toString();
		} catch (Exception e) {
			LOGGER.error("An error occured while trying to obtain the information.", e);
		}
		return null;
	}

	@Override
	public String getName() {
		return ImageServerConfigurations.IIIF;
	}

}
