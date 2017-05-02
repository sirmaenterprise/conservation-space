package com.sirma.cmf.web.upload;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.content.descriptor.FileItemDescriptor;

/**
 * Descriptor that works with Richfaces {@link UploadedFile}.
 *
 * @author BBonev
 */
public class RichfacesFileItemDescriptor extends FileItemDescriptor {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -5753118465831934135L;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(RichfacesFileItemDescriptor.class);
	/** The item. */
	private final UploadedFile item;

	/**
	 * Instantiates a new file item descriptor.
	 *
	 * @param name
	 *            the name
	 * @param item
	 *            the item
	 * @param itemFactory
	 *            the factory
	 */
	public RichfacesFileItemDescriptor(String name, UploadedFile item, FileItemFactory itemFactory) {
		super(name, null, itemFactory);
		this.item = item;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getInputStream() {
		// first check if we have a update in the content
		InputStream stream = super.getInputStream();
		if (stream == null) {
			try {
				return item.getInputStream();
			} catch (IOException e) {
				LOGGER.warn("Failed to get InputStream from FileItem", e);
			}
		}
		return null;
	}

	@Override
	protected FileItem createNewItem() {
		return getFactory().createItem(item.getParameterName(), item.getContentType(), false, item.getName());
	}

	@Override
	public long length() {
		return item.getSize();
	}

}
