package com.sirma.cmf.web.upload;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

/**
 * {@link FileItemFactory} that is serializable and exposes the repository folder.
 * 
 * @author BBonev
 */
public class SerializableDiskFileItemFactory implements EmfFileItemFactory {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7643953622739595937L;
	/** The repository. */
	private File repository;
	/** The threshold. */
	private int threshold;
	/** The factory instance. */
	private transient FileItemFactory factory;

	/**
	 * Instantiates a new file item factory proxy.
	 * 
	 * @param threshold
	 *            the threshold
	 * @param repository
	 *            the repository
	 */
	public SerializableDiskFileItemFactory(int threshold, File repository) {
		this.threshold = threshold;
		this.repository = repository;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileItem createItem(String paramString1, String paramString2, boolean paramBoolean,
			String paramString3) {
		return getFactory().createItem(paramString1, paramString2, paramBoolean, paramString3);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getRepository() {
		return repository;
	}

	/**
	 * Getter method for factory.
	 *
	 * @return the factory
	 */
	protected synchronized FileItemFactory getFactory() {
		if (factory == null) {
			factory = new DiskFileItemFactory(threshold, repository);
		}
		return factory;
	}

	/**
	 * {@inheritDoc}
	 */
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

}
