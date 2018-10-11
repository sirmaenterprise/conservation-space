package com.sirma.sep.content.descriptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.io.WritableFileDescriptor;

/**
 * Descriptor that works with Apache {@link FileItem}.
 *
 * @author BBonev
 */
public class FileItemDescriptor implements WritableFileDescriptor {
	private static final long serialVersionUID = -4967825785142688735L;
	private static final Logger LOGGER = LoggerFactory.getLogger(FileItemDescriptor.class);

	private final String name;
	private FileItem item;

	private final FileItemFactory factory;

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

	@Override
	public String getId() {
		return name;
	}

	@Override
	public String getContainerId() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		if (item == null) {
			return null;
		}
		try {
			return item.getInputStream();
		} catch (IOException e) {
			LOGGER.warn("Failed to get InputStream from FileItem", e);
		}
		return null;
	}

	@Override
	public void write(InputStream inputStream) {
		if (inputStream == null) {
			return;
		}
		FileItem fileItem = createNewItem();
		try (OutputStream outputStream = fileItem.getOutputStream()) {
			IOUtils.copyLarge(inputStream, outputStream);
			item.delete();
			item = fileItem;
		} catch (IOException e) {
			LOGGER.warn("Failed to copy InputStream to FileItem", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * Creates the new item.
	 *
	 * @return the file item
	 */
	protected FileItem createNewItem() {
		FileItem fileItem = getFactory().createItem(item.getFieldName(), item.getContentType(), item.isFormField(),
				item.getName());
		fileItem.setHeaders(item.getHeaders());
		return fileItem;
	}

	protected FileItemFactory getFactory() {
		return factory;
	}

	@Override
	public void close() {
		if (item != null) {
			item.delete();
			item = null;
		}
	}

	@Override
	public long length() {
		return item.getSize();
	}

}
