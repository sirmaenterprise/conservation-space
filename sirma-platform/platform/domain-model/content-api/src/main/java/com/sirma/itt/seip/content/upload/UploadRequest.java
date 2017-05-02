package com.sirma.itt.seip.content.upload;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.fileupload.FileItem;

/**
 * UploadRequest carries parsed {@link FileItem}s and the instance of {@link RepositoryFileItemFactory} responsible for
 * producing them. This can be used for implementing upload REST services as the content will be read before calling the
 * target REST service. This is important because when we have bigger files for upload the transaction for the rest
 * service starts after all files are transferred to the server and are ready for reading. This minimize the time for
 * handling the request in the REST method and potential transaction timeout.
 * <p>
 * Note that the REST method that expects this object to be build as argument must be a form multi part request.
 *
 * @author BBonev
 */
public class UploadRequest implements Serializable {

	private static final long serialVersionUID = 8994938754293146571L;

	private final List<FileItem> items;
	private final RepositoryFileItemFactory fileItemFactory;

	/**
	 * Instantiates a new upload request.
	 *
	 * @param items
	 *            the items
	 * @param fileItemFactory
	 *            the file item factory
	 */
	public UploadRequest(List<FileItem> items, RepositoryFileItemFactory fileItemFactory) {
		this.items = items;
		this.fileItemFactory = fileItemFactory;
	}

	/**
	 * Gets the parsed request items if any. If the request is not multi part then this list will be empty
	 *
	 * @return the request items
	 */
	public List<FileItem> getRequestItems() {
		return items;
	}

	/**
	 * Gets the file item factory instance used for producing the {@link FileItem}s.
	 *
	 * @return the file item factory
	 */
	public RepositoryFileItemFactory getFileItemFactory() {
		return fileItemFactory;
	}
}
