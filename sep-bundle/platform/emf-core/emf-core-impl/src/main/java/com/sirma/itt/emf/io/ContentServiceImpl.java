package com.sirma.itt.emf.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.content.extract.TextExtractor;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.time.TimeTracker;

/**
 * The class is responsible for obtaining content from descriptor and storing it on temp location or
 * in memory.
 *
 * @author BBonev
 * @author bbanchev
 */
@ApplicationScoped
public class ContentServiceImpl implements ContentService, Serializable {
	private static final String FAILED_TO_READ_FILE_FROM_FILE_SYSTEM = "Failed to read file from File System";
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1034216178534973036L;
	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ContentServiceImpl.class);
	@Inject
	private TempFileProvider tempFileProviderImpl;

	@Inject
	@ExtensionPoint(TextExtractor.TARGET_NAME)
	private Iterable<TextExtractor> extractors;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getContent(FileDescriptor location) {
		if (location == null) {
			return null;
		}
		File tempFile = tempFileProviderImpl.createTempFile("tempContent", ".tmp");
		try (InputStream inputStream = location.getInputStream();
				OutputStream output = new FileOutputStream(tempFile)) {
			if (inputStream == null) {
				// failed to download the resource
				return null;
			}
			IOUtils.copy(inputStream, output);
			return tempFile;
		} catch (FileNotFoundException e) {
			LOGGER.warn(FAILED_TO_READ_FILE_FROM_FILE_SYSTEM, e);
		} catch (IOException e) {
			if (tempFile != null) {
				tempFile.delete();
			}
			LOGGER.warn("", e);
		}
		return null;
	}

	@Override
	@Secure
	public long getContent(FileDescriptor location, OutputStream output) {
		if ((location == null) || (output == null)) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	public File getContent(FileDescriptor location, String fileName) {
		if (location == null) {
			return null;
		}
		File tempFile = tempFileProviderImpl.createTempFile(fileName, "");
		try (InputStream inputStream = location.getInputStream();
				OutputStream output = new FileOutputStream(tempFile)) {
			if (inputStream == null) {
				return null;
			}
			IOUtils.copy(inputStream, output);
			return tempFile;
		} catch (FileNotFoundException e) {
			LOGGER.warn(FAILED_TO_READ_FILE_FROM_FILE_SYSTEM, e);
		} catch (IOException e) {
			if (tempFile != null) {
				tempFile.delete();
			}
			LOGGER.warn("", e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String extractContent(Instance instance, FileDescriptor descriptor) {
		if (extractors == null) {
			LOGGER.warn("Invalid extract content request: no extractors installed");
			return null;
		}
		if ((instance == null) || (descriptor == null)) {
			LOGGER.warn("Invalid extract content request: invalid request parameters");
			return null;
		}
		TimeTracker tracker = TimeTracker.createAndStart();
		for (TextExtractor extractor : extractors) {
			try {
				if (extractor.isApplicable(instance)) {
					return extractor.extract(descriptor);
				}
			} catch (Exception e) {
				// this is changed to throwable due to the fact the tika extractor throws an Error
				// for missing module and we could ignore it for now
				LOGGER.debug("Failed to extract content for descritor {} using {}",
						descriptor.getId(), extractor, e);
			}
		}
		LOGGER.debug("Content extraction took {} s", tracker.stopInSeconds());
		return null;
	}

}
