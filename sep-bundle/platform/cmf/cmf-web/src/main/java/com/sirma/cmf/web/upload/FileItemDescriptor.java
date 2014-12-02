package com.sirma.cmf.web.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemHeadersSupport;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.WritableFileDescriptor;

/**
 * Descriptor that works with Apache {@link FileItem}.
 * 
 * @author BBonev
 */
public class FileItemDescriptor implements WritableFileDescriptor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4967825785142688735L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FileItemDescriptor.class);

	/** The name. */
	private final String name;
	/** The item. */
	private FileItem item;

	private FileItemFactory factory;

	/**
	 * Instantiates a new file item descriptor.
	 * 
	 * @param name
	 *            the name
	 * @param item
	 *            the item
	 * @param factory
	 *            the factory
	 */
	public FileItemDescriptor(String name, FileItem item, FileItemFactory factory) {
		this.name = name;
		this.item = item;
		this.factory = factory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContainerId() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getInputStream() {
		try {
			return item.getInputStream();
		} catch (IOException e) {
			LOGGER.warn("Failed to get InputStream from FileItem", e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(InputStream inputStream) {
		if (inputStream == null) {
			return;
		}
		FileItem fileItem = factory.createItem(item.getFieldName(), item.getContentType(),
				item.isFormField(), item.getName());
		if (item instanceof FileItemHeadersSupport) {
			fileItem.setHeaders(item.getHeaders());
		}
		item.delete();
		try (OutputStream outputStream = fileItem.getOutputStream()) {
			IOUtils.copyLarge(inputStream, outputStream);
			item = fileItem;
		} catch (IOException e) {
			LOGGER.warn("Failed to copy InputStream to FileItem", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

}
