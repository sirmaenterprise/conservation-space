/**
 *
 */
package com.sirma.itt.cmf.alfresco4.remote;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.beans.ByteArrayFileDescriptor;
import com.sirma.itt.cmf.beans.InputStreamFileDescriptor;
import com.sirma.itt.cmf.beans.LocalFileDescriptor;
import com.sirma.itt.cmf.beans.LocalProxyFileDescriptor;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.exceptions.DmsRuntimeException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;

/**
 * Uploader for files in alfresco. Various method with multi param configuration allowed finer
 * tuning for the upload. In addition methods to update of existing file is availble.
 *
 * @author borislav banchev
 */
public class AlfrescoUploader implements AlfrescoCommunicationConstants {
	/** The rest client. */
	@Inject
	private Instance<RESTClient> restClient;

	/** The Constant REVISION_DESCRIPTION. */
	protected static final String REVISION_DESCRIPTION = "Uploaded new revision of the document.";

	/** The Constant UTF_8. */
	protected static final String UTF_8 = "UTF-8";

	/**
	 * Uploads the given file to specific site + folder or dms id of parent. If some config is
	 * invalid {@link DmsRuntimeException} is thrown *
	 *
	 * @param baseServiceURI
	 *            is the upload script uri as
	 * @param descriptor
	 *            is the resource descriptor to use
	 * @param siteId
	 *            is the id of the destination site, required
	 * @param folder
	 *            is the name/path to the destination directory. Empty string or <code>null</code>
	 *            to upload into
	 * @param parentNodeId
	 *            is the parent node dms id. Default usage
	 * @param type
	 *            the type of file -> cm:content
	 * @param propertiesProp
	 *            the properties to add to the uploaded file
	 * @param aspectsProp
	 *            the aspects prop - indicates the type of attachment in most cases
	 * @param thumbnailMode
	 *            is the thumbnail generation mode (asynch, synch, none)
	 * @return a pair of the newly create node reference and destination file name or
	 *         <code>null</code> if error occur.
	 * @throws DMSClientException
	 *             on any dms/connection error on any dms/connection error or malformed request
	 */
	public String uploadFile(String baseServiceURI, FileDescriptor descriptor, String siteId,
			String folder, String parentNodeId, String type,
			Map<String, Serializable> propertiesProp, Set<String> aspectsProp, String thumbnailMode)
			throws DMSClientException {
		return uploadFile(baseServiceURI, getPartSource(descriptor), siteId, folder, parentNodeId,
				type, propertiesProp, aspectsProp, Boolean.FALSE, Boolean.TRUE, null, thumbnailMode);
	}

	/**
	 * Uploads the given file to specific site and folder. The files are uploaded into site's
	 * {@value #DIR_DOCUMENT_LIBRARY}. If the file exists the old file will remain intact.
	 *
	 * @param descriptor
	 *            is the resource descriptor to use
	 * @param siteId
	 *            is the id of the destination site, required
	 * @param folder
	 *            is the name/path to the destination directory. Empty string or <code>null</code>
	 *            to upload into
	 * @param parentNodeId
	 *            is the parent node dms id
	 * @param type
	 *            the type
	 * @param propertiesProp
	 *            the properties prop
	 * @param aspectsProp
	 *            the aspects prop
	 * @param thumbnailMode
	 *            is the thumbnail generation mode (asynch, synch, none)
	 * @return a pair of the newly create node reference and destination file name or
	 *         <code>null</code> if error occur.
	 * @throws DMSClientException
	 *             on any dms/connection error on any dms/connection error or malformed request
	 */
	public String uploadFile(FileDescriptor descriptor, String siteId, String folder,
			String parentNodeId, String type, Map<String, Serializable> propertiesProp,
			Set<String> aspectsProp, String thumbnailMode) throws DMSClientException {
		return uploadFile(ServiceURIRegistry.UPLOAD_SERVICE_URI, getPartSource(descriptor), siteId,
				folder, parentNodeId, type, propertiesProp, aspectsProp, Boolean.FALSE,
				Boolean.TRUE, null, thumbnailMode);
	}

	/**
	 * Uploads the given file to specific site and folder. The files are uploaded into site's
	 * {@value #DIR_DOCUMENT_LIBRARY}. If the file exists the old file will remain intact.
	 *
	 * @param baseURIPath
	 *            is the service uri
	 * @param partSource
	 *            is the path to the file to upload, required
	 * @param siteId
	 *            is the id of the destination site, required
	 * @param folder
	 *            is the name/path to the destination directory. Empty string or <code>null</code>
	 *            to upload into
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
	 * @param thumbnailMode
	 *            is the thumbnail generation mode (asynch, synch, none)
	 * @return a pair of the newly create node reference and destination file name or
	 *         <code>null</code> if error occur.
	 * @throws DMSClientException
	 *             on any dms/connection error on any dms/connection error or malformed request
	 */
	public String uploadFile(String baseURIPath, PartSource partSource, String siteId,
			String folder, String parentNodeId, String type,
			Map<String, Serializable> propertiesProp, Set<String> aspectsProp, Boolean overWrite,
			Boolean majorVersion, String versionDescription, String thumbnailMode)
			throws DMSClientException {
		if (partSource == null) {
			throw new DmsRuntimeException(
					"Missing required argument for upload - source content descriptor!");
		}
		if ((parentNodeId == null) && ((folder == null) || (siteId == null))) {
			throw new DmsRuntimeException(
					"Missing required argument for storage location (parentNode:" + parentNodeId
							+ " | site:" + siteId + ", folder:" + folder + ")");
		}
		// max 9 parts
		List<Part> formData = new ArrayList<Part>(9);
		formData.add(new UnicodeFilePart(KEY_FILE_DATA, partSource));
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
		HashSet<String> aspects = null;
		if (aspectsProp != null) {
			aspects = new HashSet<String>(aspectsProp);
		} else {
			aspects = new HashSet<String>();
		}
		aspects.add("cm:versionable");
		String aspectsToString = aspects.toString();
		// remote the [] form list
		formData.add(new StringPart(KEY_ASPECTS, aspectsToString.substring(1,
				aspectsToString.length() - 1), UTF_8));
		formData.add(new StringPart(KEY_MAJOR_VERSION, majorVersion.toString(), UTF_8));
		formData.add(new StringPart(KEY_OVERWRITE, overWrite.toString(), UTF_8));
		if (versionDescription != null) {
			formData.add(new StringPart(KEY_DESCRIPTION, versionDescription, UTF_8));
		}
		if (thumbnailMode != null) {
			formData.add(new StringPart(KEY_THUMBNAIL, thumbnailMode, UTF_8));
		}

		// add the properties
		JSONObject properties = new JSONObject(propertiesProp);
		// add the json object
		formData.add(new StringPart(KEY_PROPERTIES, properties.toString(), UTF_8));

		return upload(baseURIPath, formData.toArray(new Part[formData.size()]));
	}

	// ----------------------------update-----------------------------------//
	/**
	 * Update file as update procedure of existing dms node.
	 *
	 * @param updateService
	 *            the update service
	 * @param descriptor
	 *            the file descriptor
	 * @param dmsId
	 *            the dms id of the existing node
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
	 * @param thumbnailMode
	 *            is the thumbnail generation mode (asynch, synch, none)
	 * @return the response of the upload as json string
	 * @throws DMSClientException
	 *             on any dms/connection error on any dms/connection error or malformed request
	 */
	public String updateFile(String updateService, FileDescriptor descriptor, String dmsId,
			String type, Map<String, Serializable> propertiesProp, Set<String> aspectsProp,
			Boolean majorVersion, String versionDescription, String thumbnailMode)
			throws DMSClientException {

		return updateFile(updateService, getPartSource(descriptor), dmsId, type, propertiesProp,
				aspectsProp, Boolean.TRUE, majorVersion, versionDescription);
	}

	/**
	 * Update file with the given dms id using the specified rest service
	 *
	 * @param baseURIPath
	 *            the base uri path of rest service
	 * @param partSource
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
	 * @return the response of the upload as json string
	 * @throws DMSClientException
	 *             on any dms/connection error or malformed request
	 */
	private String updateFile(String baseURIPath, PartSource partSource, String dmsId, String type,
			Map<String, Serializable> propertiesProp, Set<String> aspectsProp, Boolean overWrite,
			Boolean majorVersion, String versionDescription) throws DMSClientException {
		if (partSource == null) {
			throw new DmsRuntimeException("Missing required argument - source");
		}
		if (dmsId == null) {
			throw new DmsRuntimeException("Missing required argument for updated document - dmsId");
		}
		// max 9 parts
		List<Part> formData = new ArrayList<Part>(9);
		formData.add(new UnicodeFilePart(KEY_FILE_DATA, partSource));
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
		formData.add(new StringPart(KEY_ASPECTS, aspectsToString.substring(1,
				aspectsToString.length() - 1), UTF_8));

		// add the properties
		JSONObject properties = new JSONObject(propertiesProp);
		// add the json object
		formData.add(new StringPart(KEY_PROPERTIES, properties.toString(), UTF_8));

		return upload(baseURIPath, formData.toArray(new Part[formData.size()]));
	}

	/**
	 * Upload to the given uri the provided parts.
	 *
	 * @param uri
	 *            the uri
	 * @param parts
	 *            the parts
	 * @return the response of the upload as json string
	 * @throws DMSClientException
	 *             on any dms/connection error on any dms/connection error or malformed request
	 */
	protected String upload(String uri, Part[] parts) throws DMSClientException {
		RESTClient client = restClient.get();
		HttpMethod method = client.createMethod(new PostMethod(), parts, true);
		String result = client.request(uri, method);
		return result;
	}

	/**
	 * Factory for {@link PartSource} based on the provided descriptor
	 *
	 * @param descriptorInput
	 *            is the descriptor for upload
	 * @return the descriptor or null if missing or throws exception on error
	 */
	public PartSource getPartSource(FileDescriptor descriptorInput) {

		FileDescriptor descriptor = descriptorInput;
		if (descriptor instanceof UploadWrapperDescriptor) {
			// downgrade it
			descriptor = ((UploadWrapperDescriptor) descriptor).getDelegate();
		}
		if (descriptor == null) {
			return null;
		}
		try {
			if (descriptor instanceof LocalProxyFileDescriptor) {
				File proxiedName = new File(((LocalProxyFileDescriptor) descriptor).getProxiedId());
				File realName = new File(descriptor.getId());
				return new FilePartSource(proxiedName.getName(), realName);
			} else if (descriptor instanceof LocalFileDescriptor) {
				File file = new File(descriptor.getId());
				return new FilePartSource(file.getName(), file);
			} else if (descriptor instanceof ByteArrayFileDescriptor) {
				return new StreamPartSource(descriptor.getId(), descriptor.getInputStream());
			} else if (descriptor instanceof InputStreamFileDescriptor) {
				return new StreamPartSource(descriptor.getId(), descriptor.getInputStream());
			}
			return new StreamPartSource(descriptor.getId(), descriptor.getInputStream());
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
	}

}
