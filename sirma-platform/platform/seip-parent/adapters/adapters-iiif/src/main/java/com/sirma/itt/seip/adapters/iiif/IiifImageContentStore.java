package com.sirma.itt.seip.adapters.iiif;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.JsonObject;

import com.sirma.itt.seip.adapters.iip.IIPServerImageProvider;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.adapters.ftp.BaseFtpContentStore;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
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
	private Instance<RESTClient> restClient;

	@Inject
	private IIPServerImageProvider iipServerImageProvider;

	@Override
	public FileDescriptor getReadChannel(StoreItemInfo storeInfo) {
		if (!isFromThisStore(storeInfo)) {
			throw new ContentStoreMissMatchException(getName(), storeInfo == null ? null : storeInfo.getProviderType());
		}
		URI imageAddress;
		try {
			imageAddress = new URI(getContentUrl(storeInfo.getRemoteId()), false);
		} catch (URIException e) {
			throw new StoreException("Invalid store address: " + storeInfo.getRemoteId(), e);
		}

		return new ImageFileDescriptor(imageAddress, null, restClient.get());
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
			int width = Math.min(imageServerConfigurations.getMaximumWidthForPreview().get(), Integer.parseInt(metadata.getAsString(WIDTH)));
			Dimension<Integer> dimension = new Dimension<>(width, null);
			imageAddress = new URI(iipServerImageProvider.getImageUrl(metadata.getAsString("id"), dimension), false);
		} catch (URIException e) {
			throw new StoreException("Invalid store address: " + metadata.getAsString("id"), e);
		}

		return new ImageFileDescriptor(imageAddress, null, restClient.get());
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
		JsonObject imageInfo = null;
		URI imageAddress;
		try {
			imageAddress = new URI(urlToImage, false);
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
		RESTClient client = restClient.get();
		try (InputStream inputStream = client.rawRequest(new GetMethod(), imageAddress).getResponseBodyAsStream()) {
			if (inputStream != null) {
				imageInfo = JSON.readObject(inputStream, Function.identity());
			}
		} catch (Exception e) {
			LOGGER.error("An error occurred while trying to obtain image metadata for image '{}': {}", urlToImage,
						 e.getMessage());
			LOGGER.trace("An error occurred while trying to obtain image metadata.", e);
		}
		return imageInfo;
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
}
