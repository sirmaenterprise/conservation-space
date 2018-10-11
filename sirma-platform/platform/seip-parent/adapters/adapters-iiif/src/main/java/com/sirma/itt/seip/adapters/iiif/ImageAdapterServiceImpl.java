package com.sirma.itt.seip.adapters.iiif;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT_LENGTH;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.FtpClient;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.util.file.FileUtil;

/**
 * Implementation of the adapter service for sending images to a specific image server.
 *
 * @author Nikolay Ch
 */
@ApplicationScoped
public class ImageAdapterServiceImpl implements ImageAdapterService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final long serialVersionUID = -6161958303312597536L;

	@Inject
	private ImageServerConfigurations ftpConfig;
	@Inject
	private FtpClient ftpClient;

	@Override
	public FileAndPropertiesDescriptor upload(Instance instance, FileDescriptor fileDescriptor) throws DMSException {
		if (!ftpConfig.isImageServerEnabled().get().booleanValue()) {
			return null;
		}

		if (fileDescriptor == null || !isInstanceValid(instance)) {
			return null;
		}

		String newName = getImageName(instance, fileDescriptor);

		doUpload(instance, fileDescriptor, newName);

		Map<String, Serializable> properties = new HashMap<>(4);
		properties.put(DefaultProperties.ATTACHMENT_LOCATION, newName);
		properties.put(DefaultProperties.MIMETYPE, instance.getString(DefaultProperties.MIMETYPE, ""));
		properties.put(DefaultProperties.NAME, fileDescriptor.getId());
		return new ImagePropertiesDescriptor(newName, properties);
	}

	private static boolean isInstanceValid(Instance instance) {
		return instance != null && instance.getId() != null
				&& instance.getString(DefaultProperties.MIMETYPE, "").contains("image");
	}

	private static String getImageName(Instance instance, FileDescriptor fileDescriptor) {
		String newName = convertInstanceIdToRemoteId(instance) + "."
				+ FileUtil.splitNameAndExtension(fileDescriptor.getId()).getSecond();
		return newName;
	}

	private void doUpload(Instance instance, FileDescriptor fileDescriptor, String newName) throws DMSException {
		FTPConfiguration config = ftpConfig.getImageFTPServerConfig().get();

		try (InputStream stream = fileDescriptor.getInputStream()) {
			Long contentLength = instance.get(CONTENT_LENGTH, Long.class);
			long threshold = ftpConfig.getAsyncUploadThreshold().getOrFail().longValue();
			if (contentLength == null || threshold <= 0 || contentLength.longValue() < threshold) {
				LOGGER.debug("Sending image {} with size {} to IIIF server synchronously", newName, contentLength);
				ftpClient.transfer(stream, newName, config);
			} else {
				LOGGER.debug("Sending image {} with size {} to IIIF server asynchronously", newName, contentLength);
				ftpClient.transferAsync(stream, newName, config);
			}
		} catch (IOException e) {
			throw new DMSException(e);
		}
	}

	@Override
	public String getContentUrl(Instance instance) {
		if (instance == null || instance.getId() == null) {
			return null;
		}

		String remoteId = null;
		if (instance instanceof DMSInstance) {
			remoteId = ((DMSInstance) instance).getDmsId();
		}
		if (remoteId == null) {
			remoteId = instance.getString(DefaultProperties.ATTACHMENT_LOCATION, "");
		}
		// we cannot access the file if not uploaded from this service
		if (!remoteId.isEmpty() && remoteId.startsWith(convertInstanceIdToRemoteId(instance))) {
			String remoteDir = ftpConfig.getImageFTPServerConfig().get().getRemoteDir();
			return ftpConfig.getImageAccessServerAddress().requireConfigured().get() + remoteDir + remoteId;
		}
		return null;

	}

	private static String convertInstanceIdToRemoteId(Instance instance) {
		return instance.getId().toString().replace(':', '-');
	}

	/**
	 * Contains the necessary information for the uploaded image.
	 */
	private static class ImagePropertiesDescriptor implements FileAndPropertiesDescriptor {

		private static final long serialVersionUID = 1L;
		private final Map<String, Serializable> properties;
		private final String id;

		/**
		 * Constructor for the descriptor.
		 *
		 * @param id
		 *            the id
		 * @param properties
		 *            map with the properties of an uploaded image
		 */
		public ImagePropertiesDescriptor(String id, final Map<String, Serializable> properties) {
			this.id = id;
			this.properties = properties;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getContainerId() {
			return null;
		}

		@Override
		public InputStream getInputStream() {
			return null;
		}

		@Override
		public Map<String, Serializable> getProperties() {
			return properties;
		}

		@Override
		public void close() {
			// nothing to do
		}
	}
}
