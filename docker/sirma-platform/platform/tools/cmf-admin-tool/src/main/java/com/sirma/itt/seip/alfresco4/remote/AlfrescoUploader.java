/**
 *
 */
package com.sirma.itt.seip.alfresco4.remote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.sirma.itt.seip.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.seip.alfresco4.ServiceURIRegistry;

// TODO: Auto-generated Javadoc
/**
 * Uploader for files in alfresco.
 *
 * @author borislav banchev
 */
public class AlfrescoUploader implements AlfrescoCommunicationConstants {
	/** The rest client. */
	private AbstractRESTClient restClient;

	/** The Constant REVISION_DESCRIPTION. */
	protected static final String REVISION_DESCRIPTION = "Uploaded new revision of the document.";

	/** The Constant UTF_8. */
	protected static final String UTF_8 = "UTF-8";

	/**
	 * Uploads the given file to specific site and folder. The files are uploaded into site's
	 * {@value #DIR_DOCUMENT_LIBRARY}. If the file exists the old file will remain intact.
	 *
	 * @param baseServiceURI
	 *            is the upload script uri as
	 * @param filePath
	 *            is the path to the file to upload, required
	 * @param siteId
	 *            is the id of the destination site, required
	 * @param folder
	 *            is the name/path to the destination directory. Empty string or <code>null</code> to upload into
	 * @param parentNodeId
	 *            is the parent node dms id
	 * @param type
	 *            the type
	 * @param propertiesProp
	 *            the properties prop
	 * @param aspectsProp
	 *            the aspects prop
	 * @return a pair of the newly create node reference and destination file name or <code>null</code> if error occur.
	 * @throws FileNotFoundException
	 *             the file not found exception {@value #DIR_DOCUMENT_LIBRARY}.
	 *             {@link ServiceURIRegistry#UPLOAD_SERVICE_URI}
	 */
	public String uploadFile(String baseServiceURI, String filePath, String siteId, String folder, String parentNodeId,
			String type, Map<String, Serializable> propertiesProp, Set<String> aspectsProp)
					throws FileNotFoundException {
		File file = new File(filePath);
		return uploadFile(baseServiceURI, new FilePartSource(file.getName(), file), siteId, folder, parentNodeId, type,
				propertiesProp, aspectsProp, Boolean.FALSE, Boolean.TRUE, null);
	}

	/**
	 * Uploads the given file to specific site and folder. The files are uploaded into site's
	 * {@value #DIR_DOCUMENT_LIBRARY}. If the file exists the old file will remain intact.
	 *
	 * @param filePath
	 *            is the path to the file to upload, required
	 * @param siteId
	 *            is the id of the destination site, required
	 * @param folder
	 *            is the name/path to the destination directory. Empty string or <code>null</code> to upload into
	 * @param parentNodeId
	 *            is the parent node dms id
	 * @param type
	 *            the type
	 * @param propertiesProp
	 *            the properties prop
	 * @param aspectsProp
	 *            the aspects prop
	 * @return a pair of the newly create node reference and destination file name or <code>null</code> if error occur.
	 * @throws FileNotFoundException
	 *             the file not found exception {@value #DIR_DOCUMENT_LIBRARY}.
	 */
	public String uploadFile(String filePath, String siteId, String folder, String parentNodeId, String type,
			Map<String, Serializable> propertiesProp, Set<String> aspectsProp) throws FileNotFoundException {
		File file = new File(filePath);
		return uploadFile(ServiceURIRegistry.UPLOAD_SERVICE_URI, new FilePartSource(file.getName(), file), siteId,
				folder, parentNodeId, type, propertiesProp, aspectsProp, Boolean.FALSE, Boolean.TRUE, null);
	}

	/**
	 * Uploads the given file to specific site and folder. The files are uploaded into site's
	 * {@value #DIR_DOCUMENT_LIBRARY}. If the file exists the old file will remain intact.
	 *
	 * @param baseServiceURI
	 *            is the upload script uri as
	 * @param fileName
	 *            the name for file
	 * @param file
	 *            is the file stream required
	 * @param siteId
	 *            is the id of the destination site, required
	 * @param folder
	 *            is the name/path to the destination directory. Empty string or <code>null</code> to upload into
	 * @param parentNodeId
	 *            is the parent node dms id
	 * @param type
	 *            the type
	 * @param propertiesProp
	 *            the properties prop
	 * @param aspectsProp
	 *            the aspects prop
	 * @return a pair of the newly create node reference and destination file name or <code>null</code> if error occur.
	 * @throws IOException
	 *             the file not found exception {@value #DIR_DOCUMENT_LIBRARY}.
	 *             {@link ServiceURIRegistry#UPLOAD_SERVICE_URI}
	 */
	public String uploadFile(String baseServiceURI, String fileName, InputStream file, String siteId, String folder,
			String parentNodeId, String type, Map<String, Serializable> propertiesProp, Set<String> aspectsProp)
					throws IOException {
		ByteArrayPartSource source = new ByteArrayPartSource(fileName, IOUtils.toByteArray(file));
		return uploadFile(baseServiceURI, source, siteId, folder, parentNodeId, type, propertiesProp, aspectsProp,
				Boolean.FALSE, Boolean.TRUE, null);
	}

	/**
	 * Uploads the given file to specific site and folder. The files are uploaded into site's
	 * {@value #DIR_DOCUMENT_LIBRARY}. If the file exists the old file will remain intact.
	 *
	 * @param fileName
	 *            is the filename, required
	 * @param file
	 *            is the file stream, required
	 * @param siteId
	 *            is the id of the destination site, required
	 * @param folder
	 *            is the name/path to the destination directory. Empty string or <code>null</code> to upload into
	 * @param parentNodeId
	 *            is the parent node dms id
	 * @param type
	 *            the type
	 * @param propertiesProp
	 *            the properties prop
	 * @param aspectsProp
	 *            the aspects prop
	 * @return a pair of the newly create node reference and destination file name or <code>null</code> if error occur.
	 * @throws IOException
	 *             the file not found exception {@value #DIR_DOCUMENT_LIBRARY}.
	 */
	public String uploadFile(String fileName, InputStream file, String siteId, String folder, String parentNodeId,
			String type, Map<String, Serializable> propertiesProp, Set<String> aspectsProp) throws IOException {
		ByteArrayPartSource source = new ByteArrayPartSource(fileName, IOUtils.toByteArray(file));
		return uploadFile(ServiceURIRegistry.UPLOAD_SERVICE_URI, source, siteId, folder, parentNodeId, type,
				propertiesProp, aspectsProp, Boolean.FALSE, Boolean.TRUE, null);
	}

	/**
	 * Uploads the given file to specific site and folder. The files are uploaded into site's
	 * {@value #DIR_DOCUMENT_LIBRARY}. If the file exists the old file will remain intact.
	 *
	 * @param baseURIPath
	 *            is the service uri
	 * @param filePath
	 *            is the path to the file to upload, required
	 * @param siteId
	 *            is the id of the destination site, required
	 * @param folder
	 *            is the name/path to the destination directory. Empty string or <code>null</code> to upload into
	 * @param parentNodeId
	 *            is the parent node dms id
	 * @param type
	 *            the type
	 * @param propertiesProp
	 *            the properties prop
	 * @param aspectsProp
	 *            the aspects prop
	 * @param overWrite
	 *            whether to overwrite
	 * @param majorVersion
	 *            creates major instead of minor version
	 * @param versionDescription
	 *            is the description for the version
	 * @return a pair of the newly create node reference and destination file name or <code>null</code> if error occur.
	 * @throws FileNotFoundException
	 *             the file not found exception {@value #DIR_DOCUMENT_LIBRARY}.
	 */
	public String uploadFile(String baseURIPath, PartSource filePath, String siteId, String folder, String parentNodeId,
			String type, Map<String, Serializable> propertiesProp, Set<String> aspectsProp, Boolean overWrite,
			Boolean majorVersion, String versionDescription) throws FileNotFoundException {
		if (filePath == null || parentNodeId == null && folder == null && (siteId == null || folder == null)) {
			throw new IllegalArgumentException("Missing required argument.");
		}
		// max 9 parts
		List<Part> formData = new ArrayList<Part>(9);
		formData.add(new UnicodeFilePart(KEY_FILE_DATA, filePath));
		if (parentNodeId != null) {
			formData.add(new StringPart(KEY_DESTINATION, parentNodeId, UTF_8));
		} else if (siteId != null) {
			formData.add(new StringPart(KEY_SITEID, siteId, UTF_8));
			formData.add(new StringPart(KEY_CONTAINER_ID, folder, UTF_8));
		} else if (folder != null) {
			formData.add(new StringPart(KEY_UPLOAD_DIRECTORY, folder, UTF_8));
		}
		formData.add(new StringPart(KEY_CONTENT_TYPE, type, UTF_8));
		// add the revision props
		aspectsProp.add("cm:versionable");
		formData.add(new StringPart(KEY_MAJOR_VERSION, majorVersion.toString(), UTF_8));
		formData.add(new StringPart(KEY_OVERWRITE, overWrite.toString(), UTF_8));
		if (versionDescription != null) {
			formData.add(new StringPart(KEY_DESCRIPTION, versionDescription, UTF_8));
		}
		String aspectsToString = aspectsProp.toString();
		// remote the [] form list
		formData.add(new StringPart(KEY_ASPECTS, aspectsToString.substring(1, aspectsToString.length() - 1), UTF_8));

		// add the properties
		JSONObject properties = new JSONObject(propertiesProp);
		// add the json object
		formData.add(new StringPart(KEY_PROPERTIES, properties.toString(), UTF_8));

		return upload(baseURIPath, formData.toArray(new Part[formData.size()]));
	}

	// ----------------------------update-----------------------------------//
	/**
	 * Update file.
	 *
	 * @param updateService
	 *            the update service
	 * @param absolutePath
	 *            the absolute path
	 * @param dmsId
	 *            the dms id
	 * @param type
	 *            the type
	 * @param propertiesProp
	 *            the properties prop
	 * @param aspectsProp
	 *            the aspects prop
	 * @param majorVersion
	 *            the major version
	 * @param versionDescription
	 *            the version description
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String updateFile(String updateService, String absolutePath, String dmsId, String type,
			Map<String, Serializable> propertiesProp, Set<String> aspectsProp, Boolean majorVersion,
			String versionDescription) throws IOException {
		File file = new File(absolutePath);
		return updateFile(updateService, new FilePartSource(file.getName(), file), dmsId, type, propertiesProp,
				aspectsProp, Boolean.TRUE, majorVersion, versionDescription);
	}

	/**
	 * Update file.
	 *
	 * @param updateService
	 *            the update service
	 * @param fileName
	 *            the file name
	 * @param file
	 *            the file
	 * @param dmsId
	 *            the dms id
	 * @param type
	 *            the type
	 * @param propertiesProp
	 *            the properties prop
	 * @param aspectsProp
	 *            the aspects prop
	 * @param majorVersion
	 *            the major version
	 * @param versionDescription
	 *            the version description
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public String updateFile(String updateService, String fileName, InputStream file, String dmsId, String type,
			Map<String, Serializable> propertiesProp, Set<String> aspectsProp, Boolean majorVersion,
			String versionDescription) throws IOException {
		ByteArrayPartSource source = new ByteArrayPartSource(fileName, IOUtils.toByteArray(file));
		return updateFile(updateService, source, dmsId, type, propertiesProp, aspectsProp, Boolean.TRUE, majorVersion,
				versionDescription);
	}

	/**
	 * Update file.
	 *
	 * @param baseURIPath
	 *            the base uri path
	 * @param filePath
	 *            the file path
	 * @param dmsId
	 *            the dms id
	 * @param type
	 *            the type
	 * @param propertiesProp
	 *            the properties prop
	 * @param aspectsProp
	 *            the aspects prop
	 * @param overWrite
	 *            the over write
	 * @param majorVersion
	 *            the major version
	 * @param versionDescription
	 *            the version description
	 * @return the string
	 */
	private String updateFile(String baseURIPath, PartSource filePath, String dmsId, String type,
			Map<String, Serializable> propertiesProp, Set<String> aspectsProp, Boolean overWrite, Boolean majorVersion,
			String versionDescription) {
		if (dmsId == null || filePath == null) {
			throw new IllegalArgumentException("Missing required argument.");
		}
		// max 9 parts
		List<Part> formData = new ArrayList<Part>(9);
		formData.add(new UnicodeFilePart(KEY_FILE_DATA, filePath));
		formData.add(new StringPart(KEY_UPDATE_NODE_REF, dmsId, UTF_8));
		formData.add(new StringPart(KEY_CONTENT_TYPE, type, UTF_8));
		// add the revision props
		aspectsProp.add("cm:versionable");
		formData.add(new StringPart(KEY_MAJOR_VERSION, majorVersion.toString(), UTF_8));
		formData.add(new StringPart(KEY_OVERWRITE, overWrite.toString(), UTF_8));
		if (versionDescription != null) {
			formData.add(new StringPart(KEY_DESCRIPTION, versionDescription, UTF_8));
		}
		String aspectsToString = aspectsProp.toString();
		// remote the [] form list
		formData.add(new StringPart(KEY_ASPECTS, aspectsToString.substring(1, aspectsToString.length() - 1), UTF_8));

		// add the properties
		JSONObject properties = new JSONObject(propertiesProp);
		// add the json object
		formData.add(new StringPart(KEY_PROPERTIES, properties.toString(), UTF_8));

		return upload(baseURIPath, formData.toArray(new Part[formData.size()]));
	}

	/**
	 * Upload.
	 *
	 * @param uri
	 *            the uri
	 * @param parts
	 *            the parts
	 * @return the string
	 */
	protected String upload(String uri, Part[] parts) {
		HttpMethod method = getRestClient().createMethod(new PostMethod(), parts, true);
		// TODO auth
		String result = getRestClient().invokeWithResponse(uri, method);
		return result;
	}

	/**
	 * Gets the rest client.
	 *
	 * @return the restClient
	 */
	public AbstractRESTClient getRestClient() {
		return restClient;
	}

	/**
	 * Sets the rest client.
	 *
	 * @param restClient
	 *            the restClient to set
	 */
	public void setRestClient(AbstractRESTClient restClient) {
		this.restClient = restClient;
	}

}
