package com.sirma.itt.seip.adapters.iiif;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.adapters.ftp.BaseFtpContentStore;
import com.sirma.itt.seip.adapters.iip.IIPServerImageProvider;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.rest.client.HTTPClient;
import com.sirma.itt.seip.rest.utils.HttpClientUtil;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.content.ContentMetadata;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreMissMatchException;
import com.sirma.sep.content.ImageContentMetadata;
import com.sirma.sep.content.StoreException;
import com.sirma.sep.content.StoreItemInfo;

/**
 * {@link ContentStore} implementation that works with FTP server for storing image files.
 *
 * @author BBonev
 */
@Singleton
public class IiifImageContentStore extends BaseFtpContentStore {
	private static final String HEIGHT = "height";
	private static final String WIDTH = "width";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	static final String STORE_NAME = ImageServerConfigurations.IIIF;
	private static final String IMAGE_METADATA = "imageMetadata";
	private static final ImageContentMetadata NO_METADATA = ImageContentMetadata.build(null, 0, 0);

	@Inject
	private ImageServerConfigurations imageServerConfigurations;

	@Inject
	private HTTPClient httpClient;

	@Inject
	private IIPServerImageProvider iipServerImageProvider;

	@Override
	public FileDescriptor getReadChannel(StoreItemInfo storeInfo) {
		if (!isFromThisStore(storeInfo)) {
			throw new ContentStoreMissMatchException(getName(), storeInfo == null ? null : storeInfo.getProviderType());
		}
		URI imageAddress;
		try {
			imageAddress = new URI(getContentUrl(storeInfo.getRemoteId()));
		} catch (URISyntaxException e) {
			throw new StoreException("Invalid store address: " + storeInfo.getRemoteId(), e);
		}

		return HttpClientUtil.callRemoteServiceLazily(new HttpGet(imageAddress));
		// the proper way is to check if the file actually exists in the remote store, but the asynchronous upload may
		// break the check
	}

	@Override
	public FileDescriptor getPreviewChannel(StoreItemInfo storeInfo) {
		if (!isFromThisStore(storeInfo)) {
			throw new ContentStoreMissMatchException(getName(), storeInfo == null ? null : storeInfo.getProviderType());
		}
		ContentMetadata metadata = getMetadata(storeInfo);

		URI imageAddress;
		try {
			Dimension<Integer> dimension = getImageRequestDimension(metadata);
			imageAddress = new URI(iipServerImageProvider.getImageUrl(metadata.getAsString("id"), dimension, true));
		} catch (URISyntaxException e) {
			throw new StoreException("Invalid store address: " + metadata.getAsString("id"), e);
		}

		return HttpClientUtil.callRemoteServiceLazily(new HttpGet(imageAddress));
	}

	/**
	 * Calculate request size parameters for an image.
	 * There are two possible scenarios for size calculation:
	 * 1.  When real width of image is less than configured maximum width of image for preview then returned dimension
	 * will be created with real width and height.
	 *
	 * 2.  When real width of image is bigger than configured maximum width of image for preview then returned dimension
	 * will be created with width equal to configured maximum width of image for preview and height will be calculated
	 * according IIIF Image API specification. <a href="https://iiif.io/api/image/2.1/#appendices.">@see</a>
	 *
	 * @param metadata - content metadata of requested image.
	 * @return the calculated image dimension.
	 */
	private Dimension<Integer> getImageRequestDimension(ContentMetadata metadata) {
		int imageWidth = Integer.parseInt(metadata.getAsString(WIDTH));
		int imageHeight = Integer.parseInt(metadata.getAsString(HEIGHT));
		Integer maxRequestedWidthForPreview = imageServerConfigurations.getMaximumWidthForPreview().get();
		if (imageWidth <= maxRequestedWidthForPreview) {
			return new Dimension<>(imageWidth, imageHeight);
		}
		return new Dimension<>(maxRequestedWidthForPreview, maxRequestedWidthForPreview * imageHeight / imageWidth);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ContentMetadata getMetadata(StoreItemInfo itemInfo) {
		if (!isFromThisStore(itemInfo)) {
			return NO_METADATA;
		}
		Serializable metadata = getAdditionalInfo(itemInfo, IMAGE_METADATA);
		if (metadata instanceof Map) {
			return new ImageContentMetadata((Map<String, Serializable>) metadata);
		}

		ImageContentMetadata info = getImageInfo(itemInfo.getRemoteId());
		if (isEmpty(info.getProperties())) {
			// if image is not present, try to load the default (fall-back) image
			ImageContentMetadata defaultInfo = getImageInfo(imageServerConfigurations.getDefaultImageName().get());
			if (!isEmpty(defaultInfo.getProperties())) {
				return new ImageContentMetadata(defaultInfo.getProperties());
			}
			// empty metadata with zero dimensions. this will force another check later again until valid result is
			// returned from the iiif server
			return NO_METADATA;
		}
		addAdditionalInfo(itemInfo, IMAGE_METADATA, (Serializable) info.getProperties());
		return info;
	}

	/**
	 * Gets the image width and height from the information provided by the image server.
	 *
	 * @param remoteName
	 * 		the id of the image
	 * @return the dimension of the image if already uploaded or null if not present
	 */
	@SuppressWarnings("boxing")
	private ImageContentMetadata getImageInfo(String remoteName) {
		ImageContentMetadata imageMetadata = new ImageContentMetadata(new HashMap<>());
		if (StringUtils.isEmpty(remoteName)) {
			return imageMetadata;
		}

		String imageId = FileUtil.getName(remoteName);

		StringBuilder urlToImageInfo = new StringBuilder(512).append(
				imageServerConfigurations.getIiifServerAddress().requireConfigured().get())
				.append(imageId)
				.append(StringUtils.trimToEmpty(imageServerConfigurations.getIiifServerAddressSuffix().get()))
				.append("/info.json");
		JsonObject imageInfo = getImageInfoForUri(urlToImageInfo.toString());
		if (imageInfo != null) {
			return ImageContentMetadata.build(imageId, imageInfo.getInt(WIDTH), imageInfo.getInt(HEIGHT));
		}
		return imageMetadata;
	}

	/**
	 * Connects to the image server and obtains the image information provided by the server.
	 *
	 * @param urlToImage
	 * 		the url to the image info
	 * @return the image info
	 */
	private JsonObject getImageInfoForUri(String urlToImage) {
		URI imageAddress;
		try {
			imageAddress = new URI(urlToImage);
		} catch (URISyntaxException e) {
			throw new EmfRuntimeException(e);
		}

		return httpClient.execute(imageAddress, readObject(urlToImage));
	}

	private ResponseHandler<JsonObject> readObject(String urlToImage) {
		return response -> {
			try (InputStream inputStream = response.getEntity().getContent()) {
				return JSON.readObject(inputStream, Function.identity());
			} catch (Exception e) {
				LOGGER.error("An error occurred while trying to obtain image metadata for image '{}'", urlToImage, e);
			}
			return null;
		};
	}

	@Override
	protected String getBaseReadDir(FTPConfiguration ftpConfiguration) {
		// we only need the path from the address
		// for configuration value like http://localhost:8080/processed/ the output of the method is /processed/
		String path = getReadAccessAddress().getPath();
		if (!path.endsWith("/")) {
			// make sure we have at least one path separator
			path = path + '/';
		}
		return path.substring(path.indexOf('/'));
	}

	@Override
	protected FTPConfiguration getFtpConfig() {
		return imageServerConfigurations.getImageFTPServerConfig().get();
	}

	/**
	 * Builds remote address based on the given relative file name
	 *
	 * @param remoteId
	 * 		the remote id
	 * @return the content url
	 */
	protected String getContentUrl(String remoteId) {
		if (StringUtils.isNotEmpty(remoteId)) {
			String remoteAccessAddress = getReadAccessAddress().toString();
			if (!remoteAccessAddress.endsWith("/")) {
				remoteAccessAddress += "/";
			}

			return remoteAccessAddress + remoteId;
		}
		return null;
	}

	/**
	 * Gets the full access address.
	 *
	 * @return the read access address
	 */
	protected java.net.URI getReadAccessAddress() {
		return imageServerConfigurations.getImageAccessServerAddress().getOrFail();
	}

	@Override
	public String getName() {
		return STORE_NAME;
	}

	@Override
	protected long getAsyncThreshold() {
		return imageServerConfigurations.getAsyncUploadThreshold().getOrFail();
	}

	@Override
	public boolean isCleanSupportedOnTenantDelete() {
		return true;
	}
}