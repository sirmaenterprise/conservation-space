package com.sirma.itt.emf.io;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentAccessProvider;
import com.sirma.itt.seip.content.ContentImport;
import com.sirma.itt.seip.content.ContentLoader;
import com.sirma.itt.seip.content.ContentService;
import com.sirma.itt.seip.content.ContentSetter;
import com.sirma.itt.seip.content.ContentUploader;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.content.TextExtractor;
import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.FileDescriptor.CountingFileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * The class is responsible for obtaining content from descriptor and storing it on temp location or in memory.
 *
 * @author BBonev
 * @author bbanchev
 */
@ApplicationScoped
public class ContentServiceImpl implements ContentService, Serializable {
	private static final long serialVersionUID = -1034216178534973036L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ContentServiceImpl.class);

	private static final String FAILED_TO_READ_FILE_FROM_FILE_SYSTEM = "Failed to read file from File System";

	@Inject
	private TempFileProvider tempFileProviderImpl;

	@Inject
	private TextExtractor extractor;

	@Inject
	private ContentUploader contentUploaders;

	@Inject
	@ExtensionPoint(ContentLoader.TARGET_NAME)
	private Iterable<ContentLoader> contentLoaders;

	@Inject
	@ExtensionPoint(ContentSetter.TARGET_NAME)
	private Iterable<ContentSetter> contentSetters;

	@Inject
	@ExtensionPoint(ContentAccessProvider.TARGET_NAME)
	private Iterable<ContentAccessProvider> contentAccessProviders;

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public File getContent(FileDescriptor location) {
		if (location == null) {
			return null;
		}
		File tempFile = tempFileProviderImpl.createTempFile("tempContent", ".tmp");
		return copyContentFrom(location, tempFile);
	}

	@Override
	public long getContent(FileDescriptor location, OutputStream output) {
		if (location == null || output == null) {
			return -1L;
		}
		try (InputStream inputStream = location.getInputStream()) {
			if (inputStream == null) {
				// failed to download the resource
				return -1L;
			}
			return IOUtils.copyLarge(inputStream, output);
		} catch (IOException e) {
			LOGGER.warn("", e);
		}
		return -1L;
	}

	@Override
	public File getContent(FileDescriptor location, String fileName) {
		if (location == null) {
			return null;
		}
		File tempFile = tempFileProviderImpl.createTempFile(fileName, "");

		return copyContentFrom(location, tempFile);
	}

	/**
	 * Copy content from the given descriptor to the given temporary file. The file is deleted if the copy is
	 * unsuccessful.
	 *
	 * @param location
	 *            the location
	 * @param tempFile
	 *            the temporary file
	 * @return the file or <code>null</code> if not successful
	 */
	private File copyContentFrom(FileDescriptor location, File tempFile) {
		if (copyDescriptorToLocalFile(location, tempFile)) {
			return tempFile;
		}
		tempFileProviderImpl.deleteFile(tempFile);
		return null;
	}

	/**
	 * Copy descriptor to local file.
	 *
	 * @param location
	 *            the location
	 * @param tempFile
	 *            the target temporary file
	 * @return true, if successful
	 */
	private static boolean copyDescriptorToLocalFile(FileDescriptor location, File tempFile) {
		try (InputStream inputStream = location.getInputStream();
				OutputStream output = new FileOutputStream(tempFile)) {
			if (inputStream == null) {
				return false;
			}
			IOUtils.copy(inputStream, output);
			return true;
		} catch (FileNotFoundException e) {
			LOGGER.warn(FAILED_TO_READ_FILE_FROM_FILE_SYSTEM, e);
		} catch (IOException e) {
			LOGGER.warn("", e);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String extractContent(String mimetype, FileDescriptor descriptor) {
		if (descriptor == null) {
			LOGGER.warn("Invalid extract content request: invalid request parameters");
			return null;
		}
		try {
			return extractor.extract(mimetype, descriptor).orElse(null);
		} catch (Exception e) {
			// this is changed to throwable due to the fact the tika extractor
			// throws an Error
			// for missing module and we could ignore it for now
			LOGGER.debug("Failed to extract content for descritor {} using {}", descriptor.getId(), extractor, e);
		}
		return null;
	}

	@Override
	public String loadTextContent(Instance instance) {
		return loadContent(instance);
	}

	@Override
	public String loadTextContent(InstanceReference reference) {
		return loadContent(reference);
	}

	/**
	 * Load content using the content loaders. Iterates over the loaders until content is found or there are no more
	 * loaders
	 *
	 * @param instance
	 *            the instance
	 * @return the string
	 */
	private String loadContent(Object instance) {
		if (instance == null) {
			return null;
		}
		for (ContentLoader contentLoader : contentLoaders) {
			if (contentLoader.isApplicable(instance)) {
				String content = contentLoader.loadContent(instance);
				if (content != null) {
					return content;
				}
			}
		}
		return null;
	}

	@Override
	public void setContent(Instance instance, String content) {
		if (instance == null) {
			return;
		}

		for (ContentSetter setter : contentSetters) {
			if (setter.isApplicable(instance)) {
				setter.setContent(instance, content);
				break;
			}
		}
	}

	@Override
	public FileAndPropertiesDescriptor uploadContent(Instance instance, FileDescriptor descriptor) {
		if (instance == null || descriptor == null) {
			LOGGER.warn("Invalid request parameters.");
			return null;
		}
		try {
			CountingFileDescriptor countingDescriptor = FileDescriptor.enableCounting(descriptor);
			FileAndPropertiesDescriptor uploadedProperties = contentUploaders.uploadContent(instance,
					countingDescriptor);
			if (uploadedProperties != null) {
				Map<String, Serializable> properties = uploadedProperties.getProperties();
				String name = (String) properties.get(DefaultProperties.NAME);
				String mimeType = (String) properties.get(DefaultProperties.MIMETYPE);
				importContent(instance, name, mimeType, countingDescriptor, uploadedProperties.getId());
				return uploadedProperties;
			}
		} catch (Exception e) {
			LOGGER.debug("Error occured while trying to upload the content.", e);
		}
		return null;
	}

	private void importContent(Instance instance, String name, String mimeType, CountingFileDescriptor descriptor,
			String remoteId) {
		boolean isView = false;
		if (instance instanceof Purposable) {
			isView = "iDoc".equals(((Purposable) instance).getPurpose());
		}

		ContentImport content = ContentImport
				.createEmpty()
					.setContent(descriptor)
					.setCharset(StandardCharsets.UTF_8.name())
					.setMimeType(mimeType)
					.setPurpose(Content.PRIMARY_CONTENT)
					.setName(name)
					.setInstanceId(instance.getId())
					.setRemoteId(remoteId)
					// this is temporary check until all content is passed via InstanceContentService
					.setRemoteSourceName(remoteId.startsWith("workspace://") ? "alfresco4" : "iiif")
					.setView(isView)
					.setContentLength(Long.valueOf(descriptor.getTransferredBytes()))
					.setIndexable(true);

		String contentId = instanceContentService.importContent(content);
		// This is a fix requred for "old" ui idocs. We need to set PRIMARY_CONTENT_ID only if the
		// purpose is not idoc. See DocumentInstance.isUploaded().
		if (!isView) {
			instance.addIfNotNull(PRIMARY_CONTENT_ID, contentId);
		}
	}

	@Override
	public String getContentURI(Instance instance) {
		if (contentAccessProviders == null) {
			LOGGER.warn("No URI providers.");
			return null;
		}
		if (instance == null) {
			LOGGER.warn("Invalid request parameter for URI creation");
			return null;
		}
		String contentURI = null;
		TimeTracker tracker = TimeTracker.createAndStart();
		for (ContentAccessProvider provider : contentAccessProviders) {
			try {

				contentURI = provider.getContentURI(instance);
				if (contentURI != null) {
					LOGGER.debug("URI creation took {} ms", tracker.stop());
					return contentURI;
				}
			} catch (Exception e) {
				LOGGER.debug("Error occured while trying to create the document's URI.", e);
			}
		}

		LOGGER.debug("URI creation took {} ms", tracker.stop());
		return null;
	}

	@Override
	public FileDescriptor getDescriptor(Instance instance) {
		if (contentAccessProviders == null) {
			LOGGER.warn("No URI providers.");
			return null;
		}
		if (instance == null) {
			LOGGER.warn("Invalid request parameter for URI creation");
			return null;
		}
		FileDescriptor descriptor = null;
		TimeTracker tracker = TimeTracker.createAndStart();
		for (ContentAccessProvider provider : contentAccessProviders) {
			try {
				descriptor = provider.getDescriptor(instance);
				if (descriptor != null) {
					LOGGER.debug("URI creation took {} ms", tracker.stop());
					return descriptor;
				}
			} catch (Exception e) {
				LOGGER.debug("Error occured while trying to create the document's URI.", e);
			}
		}

		LOGGER.debug("URI creation took {} ms", tracker.stop());
		return null;
	}

}
