package com.sirma.sep.content.upload;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.sep.content.upload.RepositoryFileItemFactory;

/**
 * {@link FileItemFactory} that is serializable and exposes the repository folder. The implementation supports a
 * repository reset so it can be reused.
 *
 * @author BBonev
 */
public class SerializableDiskFileItemFactory implements RepositoryFileItemFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(SerializableDiskFileItemFactory.class);
	private static final long serialVersionUID = 2142376919901679882L;
	/** The index used to ensure the uniqueness of the created folders. */
	private static final AtomicLong INDEX = new AtomicLong(0);
	/** The repository location. */
	private File repository;
	/**
	 * The threshold. Files bigger that the threshold value will be stored in the repository and less than that in the
	 * memory.
	 */
	private int threshold;
	/** The factory instance. */
	private transient FileItemFactory factory;
	/** The file provider to create new temporary folders. */
	private TempFileProvider fileProvider;
	private transient boolean reset = false;

	/**
	 * Instantiates a new file item factory proxy.
	 *
	 * @param threshold
	 *            the threshold
	 * @param repository
	 *            the repository. Optional. If not provided the new one will be generated from the provided
	 *            {@link TempFileProvider} if present.
	 * @param fileProvider
	 *            the file provider. For the implementation to be able to reset itself this is required. It's also
	 *            required if the repository parameter is not passed.
	 */
	public SerializableDiskFileItemFactory(int threshold, File repository, TempFileProvider fileProvider) {
		this.threshold = threshold;
		this.fileProvider = fileProvider;
		this.repository = repository;
		if (repository == null && fileProvider == null) {
			throw new EmfRuntimeException("Cannot create factory without repository location and file factory.");
		}
	}

	@Override
	public FileItem createItem(String paramString1, String paramString2, boolean paramBoolean, String paramString3) {
		return getFactory().createItem(paramString1, paramString2, paramBoolean, paramString3);
	}

	@Override
	public synchronized File getRepository() {
		// if the folder is empty create new one
		if (repository == null) {
			repository = createTempFolder();
		}
		return repository;
	}

	/**
	 * Getter method for factory.
	 *
	 * @return the factory
	 */
	protected synchronized FileItemFactory getFactory() {
		if (factory == null) {
			factory = new DiskFileItemFactory(threshold, getRepository());
		} else if (reset) {
			File newRepo = getRepository();
			((DiskFileItemFactory) factory).setRepository(newRepo);
			LOGGER.trace("Created new repository location {}", newRepo);
		}
		return factory;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SerializableDiskFileItemFactory [threshold=");
		builder.append(threshold);
		builder.append(", repository=");
		builder.append(repository);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public synchronized void resetRepository() {
		// if provider is null we will disable the reset functionality not to break all
		if (fileProvider == null) {
			LOGGER.warn("Tried to reset repository but did not provide temporary file provider. Nothing will be reset");
			return;
		}
		if (repository == null || !repository.exists()) {
			LOGGER.debug("Tried to reset repository but nothing to reset.");
			return;
		}
		LOGGER.trace("Clearing old repository location {} ", repository);
		// clean the old repository if left
		fileProvider.deleteFile(repository);
		repository = null;
		if (factory instanceof DiskFileItemFactory) {
			reset = true;
			// does not create new folder right away but lazily
		} else {
			// this will clear the old factory and force is to instantiate new one
			factory = null;
		}
	}

	/**
	 * Creates temporary folder for the next 24 hours that will be used for the current upload session.
	 *
	 * @return the created folder
	 */
	private File createTempFolder() {
		if (fileProvider == null) {
			throw new EmfRuntimeException("Cannot create temporary folder: no file provider present!");
		}
		return fileProvider.createTempDir("FileFactory-" + System.currentTimeMillis() + '-' + INDEX.getAndIncrement());
	}

}
