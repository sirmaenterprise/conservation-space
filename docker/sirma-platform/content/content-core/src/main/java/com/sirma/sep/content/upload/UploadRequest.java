package com.sirma.sep.content.upload;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.fileupload.FileItem;

import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.sep.content.upload.RepositoryFileItemFactory;

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
public class UploadRequest extends ActionRequest {

	private static final long serialVersionUID = 8994938754293146571L;

	private final List<FileItem> items;
	private final RepositoryFileItemFactory fileItemFactory;
	private String instanceVersion;

	public static final String UPLOAD = "upload";

	/**
	 * Instantiates a new upload request.
	 *
	 * @param items the items
	 * @param fileItemFactory the file item factory
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

	/**
	 * Gets the version of the instance/object that will be affected by this operation request.
	 *
	 * @return the version
	 */
	public String getInstanceVersion() {
		return instanceVersion;
	}

	/**
	 * Sets the version of the instance/object that will be affected by this operation request.
	 *
	 * @param instanceVersion the instanceVersion
	 */
	public void setInstanceVersion(String instanceVersion) {
		this.instanceVersion = instanceVersion;
	}

	@Override
	public String getOperation() {
		return UPLOAD;
	}

	/**
	 * Returns a form field value lookup by name. If value is not found or has no value then the given default value will be returned
	 *
	 * @param name the form field name
	 * @param defaultValue the default value to return
	 * @return the found value or the default value
	 */
	public String resolveFormField(String name, String defaultValue) {
		return getRequestItems()
				.stream()
				.filter(FileItem::isFormField)
				.filter(item -> nullSafeEquals(item.getFieldName(), name, true))
				.map(item -> {
					try {
						return item.getString("utf-8");
					} catch (UnsupportedEncodingException e) {
						return item.getString();
					}
				})
				.findFirst()
				.orElse(defaultValue);
	}
}
