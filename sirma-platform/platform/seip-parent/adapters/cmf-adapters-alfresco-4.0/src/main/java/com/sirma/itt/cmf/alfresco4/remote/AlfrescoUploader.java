package com.sirma.itt.cmf.alfresco4.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.adapters.remote.StreamPartSource;
import com.sirma.itt.seip.adapters.remote.UnicodeFilePart;
import com.sirma.itt.seip.domain.exceptions.DmsRuntimeException;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.sep.content.descriptor.LocalFileDescriptor;
import com.sirma.sep.content.descriptor.LocalProxyFileDescriptor;

/**
 * Uploader for files in alfresco. Various method with multi param configuration allowed finer tuning for the upload. In
 * addition methods to update of existing file is availble.
 *
 * @author borislav banchev
 * @author A. Kunchev
 */
@ApplicationScoped
public class AlfrescoUploader implements AlfrescoCommunicationConstants {

	protected static final String REVISION_DESCRIPTION = "Uploaded new revision of the document.";

	@Inject
	private Instance<RESTClient> restClient;

	/**
	 * Uploads the given file to specific site and folder. The files are uploaded into site's
	 * {@value #DIR_DOCUMENT_LIBRARY}. If the file exists the old file will remain intact.
	 *
	 * @param context
	 *            object containing required data for the correct file upload
	 * @return the response of the upload as json string
	 * @throws DMSClientException
	 *             on any dms/connection error on any dms/connection error or malformed request
	 */
	public String uploadFile(ContentUploadContext context) throws DMSClientException {
		PartSource filePart = context.getFilePart();
		if (filePart == null) {
			throw new DmsRuntimeException("Missing required argument for upload - source content descriptor!");
		}

		if (!context.isLocationAvailable()) {
			throw new DmsRuntimeException(
					"Missing required argument for storage location (parentNode: null, site: null, folder: null)");
		}
		// max 9 parts
		List<Part> formData = new ArrayList<>(10);

		setTargetParams(context.getSiteId(), context.getFolder(), context.getParentNodeId(), formData);
		addAspects(context.getAspectProperties(), formData);
		setOverrideInfo(context, formData);

		formData.add(new UnicodeFilePart(KEY_FILE_DATA, filePart));
		formData.add(new StringPart(KEY_CONTENT_TYPE, context.getContentType(), UTF_8));
		formData.add(new StringPart(KEY_THUMBNAIL, context.getThumbnailMode(), UTF_8));
		formData.add(new StringPart(KEY_UPLOAD_MODE, context.getUploadMode(), UTF_8));

		String propertiesAsString = JSON.convertToJsonObject(context.getProperties()).toString();
		formData.add(new StringPart(KEY_PROPERTIES, propertiesAsString, UTF_8));

		return upload(context.getServiceURL(), formData.toArray(new Part[formData.size()]));
	}

	private static void setOverrideInfo(ContentUploadContext context, List<Part> formData) {
		formData.add(new StringPart(KEY_MAJOR_VERSION, context.isMajorVersion().toString(), UTF_8));
		formData.add(new StringPart(KEY_OVERWRITE, context.shouldOverwrite().toString(), UTF_8));
		String versionDescription = context.getVersionDescription();
		if (versionDescription != null) {
			formData.add(new StringPart(KEY_DESCRIPTION, versionDescription, UTF_8));
		}
	}

	private static void addAspects(Set<String> aspectsProp, List<Part> formData) {
		Set<String> aspects = new HashSet<>(2);
		if (aspectsProp != null) {
			aspects = new HashSet<>(aspectsProp);
		}

		aspects.add("cm:versionable");
		String aspectsToString = aspects.toString();
		// remove the [] form list
		formData.add(new StringPart(KEY_ASPECTS, aspectsToString.substring(1, aspectsToString.length() - 1), UTF_8));
	}

	private static void setTargetParams(String siteId, String folder, String parentNodeId, List<Part> formData) {
		if (parentNodeId != null) {
			formData.add(new StringPart(KEY_DESTINATION, parentNodeId, UTF_8));
		} else if (siteId != null && folder != null) {
			formData.add(new StringPart(KEY_SITEID, siteId, UTF_8));
			formData.add(new StringPart(KEY_CONTAINER_ID, folder, UTF_8));
		} else if (folder != null) {
			formData.add(new StringPart(KEY_UPLOAD_DIRECTORY, folder, UTF_8));
		}
	}

	/**
	 * Update file with the given dms id using the specified rest service
	 *
	 * @param context
	 *            object containing required data for the correct file update
	 * @return the response of the upload as json string
	 * @throws DMSClientException
	 *             on any dms/connection error or malformed request
	 */
	public String updateFile(ContentUpdateContext context) throws DMSClientException {
		PartSource filePart = context.getFilePart();
		if (filePart == null) {
			throw new DmsRuntimeException("Missing required argument - source");
		}

		String dmsId = context.getNodeDmsId();
		if (dmsId == null) {
			throw new DmsRuntimeException("Missing required argument for updated document - dmsId");
		}

		// max 9 parts
		List<Part> formData = new ArrayList<>(9);
		formData.add(new UnicodeFilePart(KEY_FILE_DATA, filePart));
		formData.add(new StringPart(KEY_UPDATE_NODE_REF, dmsId, UTF_8));
		formData.add(new StringPart(KEY_CONTENT_TYPE, context.getContentType(), UTF_8));
		formData.add(new StringPart(KEY_MAJOR_VERSION, context.isMajorVersion().toString(), UTF_8));
		formData.add(new StringPart(KEY_OVERWRITE, context.shouldOverwrite().toString(), UTF_8));
		formData.add(new StringPart(KEY_DESCRIPTION, context.getVersionDescription(), UTF_8));
		String propertiesAsString = JSON.convertToJsonObject(context.getProperties()).toString();
		addAspects(context.getAspectProperties(), formData);
		formData.add(new StringPart(KEY_PROPERTIES, propertiesAsString, UTF_8));
		formData.add(new StringPart(KEY_THUMBNAIL, context.getThumbnailMode(), UTF_8));
		return upload(context.getServiceURL(), formData.toArray(new Part[formData.size()]));
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
		return client.request(uri, method);
	}

	/**
	 * Factory for {@link PartSource} based on the provided descriptor
	 *
	 * @param descriptorInput
	 *            is the descriptor for upload
	 * @return the descriptor or null if missing or throws exception on error
	 */
	@SuppressWarnings({ "static-method", "unchecked" })
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
			FileDescriptor delegate = descriptor;
			if (descriptor instanceof GenericProxy<?>) {
				delegate = ((GenericProxy<FileDescriptor>) descriptor).getTarget();
			}

			PartSource result = null;
			if (delegate instanceof LocalProxyFileDescriptor) {
				File proxiedName = new File(((LocalProxyFileDescriptor) delegate).getProxiedId());
				File realName = new File(delegate.getId());
				result = new FilePartSource(proxiedName.getName(), realName);
			} else if (delegate instanceof LocalFileDescriptor) {
				File file = new File(delegate.getId());
				result = new FilePartSource(file.getName(), file);
			} else {
				// this applies for ByteArrayFileDescriptor, InputStreamFileDescriptor or any other
				result = new StreamPartSource(descriptor.getId(), descriptorInput.length(),
						descriptorInput.getInputStream());
			}

			return result;
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
	}

}
