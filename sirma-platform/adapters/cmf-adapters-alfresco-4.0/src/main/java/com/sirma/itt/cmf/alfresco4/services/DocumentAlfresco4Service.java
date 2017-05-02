/**
 *
 */
package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoUtils;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileAndPropertiesDescriptor;
import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileDescriptor;
import com.sirma.itt.cmf.alfresco4.remote.AlfrescoUploader;
import com.sirma.itt.cmf.alfresco4.remote.ContentUpdateContext;
import com.sirma.itt.cmf.alfresco4.remote.ContentUploadContext;
import com.sirma.itt.cmf.alfresco4.services.convert.Converter;
import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.adapters.remote.AlfrescoErrorReader;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualConcurrentMap;
import com.sirma.itt.seip.content.descriptor.ContentPreviewDescriptor;
import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.util.StringEncoder;

/**
 * The class adapter for documents in dms<->cmf communication. Has a methods to upload/update documents in dms.
 *
 * @author Borislav Banchev
 */
@ApplicationScoped
public class DocumentAlfresco4Service implements CMFDocumentAdapterService, AlfrescoCommunicationConstants {

	private static final String PREVIEW_URL = ServiceURIRegistry.CMF_TO_DMS_PROXY_SERVICE
			+ ServiceURIRegistry.CONTENT_ACCESS_URI;

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentAlfresco4Service.class);

	@Inject
	private RESTClient restClient;

	@Inject
	private Instance<RESTClient> restClientInstance;

	@Inject
	private AlfrescoUploader alfrescoUploader;

	@Inject
	@Converter(name = ConverterConstants.GENERAL)
	private DMSTypeConverter docConvertor;

	@Inject
	private ContextualConcurrentMap<String, String> libraryCache;

	@Inject
	private CMFSearchAdapterService searchAdapter;
	@Inject
	private AdaptersConfiguration adaptersConfiguration;

	@Override
	public FileAndPropertiesDescriptor checkOut(DMSInstance document, String userId) throws DMSException {
		JSONObject object = performOperation(document, userId, DocumentOperation.CHECKOUT);

		String serializable = null;
		Map<String, Serializable> map = null;
		try {
			map = docConvertor.convertDMSToCMFProperties(object, document,
					DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);

			serializable = object.getString(KEY_NODEREF);
			String container = null;
			if (object.has(KEY_SITE_ID)) {
				container = object.getString(KEY_SITE_ID);
			}

			map.put(DocumentProperties.WORKING_COPY_LOCATION,
					new AlfrescoFileDescriptor(serializable, container, restClientInstance.get()));
			map.put(DefaultProperties.LOCKED_BY, userId);
		} catch (Exception e) {
			throw new DMSException("Failed to convert properties from the response", e);
		}

		return new AlfrescoFileAndPropertiesDescriptor(document.getDmsId(), map, restClientInstance.get());
	}

	/**
	 * Performs a given operation on the given instance. The actual service to execute is determined by the 'operation'
	 * argument
	 *
	 * @param document
	 *            the document to perform operation on
	 * @param userId
	 *            the user id
	 * @param operation
	 *            the operation
	 * @return the jSON object
	 * @throws DMSException
	 *             the dMS exception
	 */
	private JSONObject performOperation(DMSInstance document, String userId, DocumentOperation operation)
			throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(document);
		if (operation == null) {
			throw new DMSException("Missing argument for operation on document!");
		}
		String url = null;
		JSONObject request = new JSONObject();
		try {
			Serializable attachmentId = document.getDmsId();
			// fill needed data for each operation
			if (operation == DocumentOperation.CHECKIN) {
				// as uploading new version
				Serializable temp = document.getProperties().get(DocumentProperties.IS_MAJOR_VERSION);
				Boolean isMajor = temp == null ? Boolean.FALSE : Boolean.valueOf(temp.toString());
				temp = document.getProperties().get(DocumentProperties.VERSION_DESCRIPTION);
				String description = temp == null ? "Checked in file" : temp.toString();
				request.put(KEY_DESCRIPTION, description);
				request.put(KEY_MAJOR_VERSION, isMajor);
				url = ServiceURIRegistry.CMF_DOCUMENT_CHECKIN;
				// add the properties
				request.put(KEY_PROPERTIES, docConvertor.convertCMFtoDMSProperties(document.getProperties(), document,
						DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL));
			} else if (operation == DocumentOperation.CHECKOUT) {
				url = ServiceURIRegistry.CMF_DOCUMENT_CHECKOUT;
			} else if (operation == DocumentOperation.LOCK) {
				url = ServiceURIRegistry.CMF_DOCUMENT_LOCK_NODE;
			} else if (operation == DocumentOperation.UNLOCK) {
				url = ServiceURIRegistry.CMF_DOCUMENT_UNLOCK_NODE;
			} else if (operation == DocumentOperation.CANCEL_CHECKOUT) {
				Serializable workingCopyId = document.getProperties().get(DocumentProperties.WORKING_COPY_LOCATION);
				if (workingCopyId != null) {
					attachmentId = workingCopyId;
				}
				url = ServiceURIRegistry.CMF_DOCUMENT_CHECKOUT_CANCEL;
			}
			if (userId != null) {
				request.put(KEY_LOCK_OWNER, userId);
			}

			request.put(KEY_ATTACHMENT_ID, attachmentId);
			HttpMethod createdMethod = restClient.createMethod(new PostMethod(), request.toString(), true);
			String response = restClient.request(url, createdMethod);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_ROOT_DATA)) {
					JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(KEY_DATA_ITEMS);
					if (nodes.length() == 1) {
						// return nodes
						return nodes.getJSONObject(0);
					}
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during execution of operation '" + operation + "' on:" + document, e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + document + "': " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Attachment " + document.getId() + " is not updated during request!");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService# uploadNewVersion
	 * (com.sirma.itt.cmf.beans.model.DMSInstance, com.sirma.itt.cmf.services.adapter.DMSFileDescriptor)
	 */
	@Override
	public FileAndPropertiesDescriptor uploadNewVersion(DMSInstance instance, UploadWrapperDescriptor descriptor)
			throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(instance);
		try {
			// get the file
			String uploadFile = alfrescoUploader.updateFile(buildUpdateContext(instance, descriptor));

			// upload the file now
			if (uploadFile != null) {
				JSONObject fileData = new JSONObject(uploadFile);
				Map<String, Serializable> dmsToCMFProperties = docConvertor.convertDMSToCMFProperties(fileData,
						instance, DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
				return new AlfrescoFileAndPropertiesDescriptor(fileData.getString(KEY_NODEREF), null,
						dmsToCMFProperties, restClient);
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during upload new version of document on: " + instance, e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + instance + "': " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("File is not uploaded!");
	}

	private ContentUpdateContext buildUpdateContext(DMSInstance instance, UploadWrapperDescriptor descriptor) {
		return ContentUpdateContext
				.create(ServiceURIRegistry.CMF_ATTACH_TO_INSTANCE_SERVICE,
						instance.getString(DocumentProperties.WORKING_COPY_LOCATION, instance.getDmsId()))
					.setFilePart(alfrescoUploader.getPartSource(descriptor))
					.setContentType(TYPE_CM_CONTENT)
					.setProperties(getProperties(instance, descriptor))
					.setAspectProperties(Collections.singleton("cm:versionable"))
					.setMajorVersion(instance.getAs(DocumentProperties.IS_MAJOR_VERSION,
							value -> Boolean.valueOf(value.toString()), () -> Boolean.FALSE))
					.setVersionDescription(instance.getAsString(DocumentProperties.VERSION_DESCRIPTION, () -> ""))
					.setThumbnailMode(resolveThumbnailMode(instance));
	}

	private static String resolveThumbnailMode(DMSInstance instance) {
		if (!instance.isUploaded()) {
			return ThumbnailGenerationMode.NONE.toString();
		}

		String mode = instance.getAsString(DocumentProperties.DOCUMENT_THUMB_MODE);
		if (StringUtils.isNotBlank(mode)) {
			return mode;
		}

		return ThumbnailGenerationMode.ASYNCH.toString();
	}

	@Override
	public FileAndPropertiesDescriptor uploadContent(DMSInstance instance, UploadWrapperDescriptor descriptor,
			Set<String> aspectsToInclude) throws DMSException {
		try {
			String uploadFile = alfrescoUploader.uploadFile(buildUploadContext(aspectsToInclude, descriptor, instance));

			// upload the file now
			if (uploadFile != null) {
				JSONObject fileData = new JSONObject(uploadFile);
				Map<String, Serializable> dmsToCMFProperties = docConvertor.convertDMStoCMFPropertiesByValue(fileData,
						instance, DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);

				return new AlfrescoFileAndPropertiesDescriptor(fileData.getString(KEY_NODEREF), null,
						dmsToCMFProperties, restClient);
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during uploading document: " + instance, e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + instance + "': " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("File is not uploaded!");
	}

	private ContentUploadContext buildUploadContext(Set<String> aspectsToInclude, UploadWrapperDescriptor descriptor,
			DMSInstance instance) throws DMSException {
		return ContentUploadContext
				.create(ServiceURIRegistry.CMF_ATTACH_TO_INSTANCE_SERVICE, descriptor.getUploadMode())
					.setFilePart(alfrescoUploader.getPartSource(descriptor))
					.setParentNodeId(getDmsId(descriptor))
					.setContentType(TYPE_CM_CONTENT)
					.setProperties(getProperties(instance, descriptor))
					.setAspectProperties(convertAspectsForDms(aspectsToInclude))
					.setThumbnailMode(resolveThumbnailMode(instance))
					.setOverwrite(Boolean.FALSE)
					.setMajorVersion(Boolean.TRUE);
	}

	private String getDmsId(UploadWrapperDescriptor descriptor) throws DMSException {
		String dmsId = descriptor.getContainerId();
		if (dmsId == null) {
			dmsId = getLibraryDMSId();
		}

		if (dmsId == null) {
			throw new DMSException("Target not specified and library not initialized");
		}

		return dmsId;
	}

	private Map<String, Serializable> getProperties(DMSInstance instance, UploadWrapperDescriptor descriptor) {
		Map<String, Serializable> propertiesProp = null;
		Map<String, Serializable> metadataToInclude = descriptor.getProperties();
		if (metadataToInclude == null) {
			propertiesProp = new HashMap<>(1);
		} else {
			Map<String, Serializable> copyOfData = new HashMap<>(metadataToInclude);
			copyOfData.remove(DefaultProperties.NAME);
			propertiesProp = docConvertor.convertCMFtoDMSProperties(copyOfData, instance,
					DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			// remove name as it is automatically maintained
		}
		fixDMSName(descriptor, propertiesProp);
		return propertiesProp;
	}

	private static void fixDMSName(FileAndPropertiesDescriptor descriptor, Map<String, Serializable> properties) {
		if (properties.containsKey(AlfrescoCommunicationConstants.KEY_DMS_NAME)) {
			String dmsName = String.valueOf(properties.get(AlfrescoCommunicationConstants.KEY_DMS_NAME));
			dmsName = "NO_ID".equals(dmsName) ? descriptor.getId() : dmsName;

			// to be able to save with any non-word characters in the name
			dmsName = dmsName.replaceAll("[\\W]+", "_");

			properties.put(KEY_DMS_NAME, UUID.randomUUID() + "-" + dmsName);
		}
	}

	private Set<String> convertAspectsForDms(Set<String> aspectsToInclude) throws DMSException {
		Set<String> aspectsProp = null;
		if (aspectsToInclude != null) {
			aspectsProp = CollectionUtils.createHashSet(aspectsToInclude.size());
			for (String nextAspect : aspectsToInclude) {

				Pair<String, Serializable> dmsAspect = docConvertor.convertCMFtoDMSProperty(nextAspect, "",
						DMSTypeConverter.PROPERTIES_MAPPING);
				if (dmsAspect == null) {
					throw new DMSException("Invalid aspect provided (" + nextAspect + ")");
				}
				aspectsProp.add(dmsAspect.getFirst());
			}
		}
		return aspectsProp;
	}

	@Override
	public String deleteAttachment(DMSInstance documentInstance) throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(documentInstance);
		Serializable attachmentId = documentInstance.getDmsId();
		Serializable caseId = documentInstance.get(DocumentProperties.CASE_DMS_ID);
		JSONObject request = new JSONObject();
		try {

			request.put(KEY_ATTACHMENT_ID, attachmentId);
			request.put(KEY_NODEID, caseId);
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(), true);
			String restResult = restClient.request(ServiceURIRegistry.CMF_DETTACH_FROM_INSTANCE_SERVICE, createMethod);
			if (restResult != null) {
				JSONObject result = new JSONObject(restResult);
				if (result.has("deleted") && attachmentId.equals(result.getString("deleted"))) {
					return attachmentId.toString();
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during deletion of document: " + documentInstance, e);
		} catch (Exception e) {
			throw new DMSException(
					"Failure during request on '" + documentInstance + "': " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Attachment " + documentInstance.getId() + " is not deleted!");
	}

	@Override
	public FileDescriptor getDocumentPreview(DmsAware document, String targetMimetype) throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(document);
		try {
			HttpMethod createdMethod = restClient.createMethod(new GetMethod(), "", true);
			String idPreview = document.getDmsId().replace(":/", "");
			String uri = MessageFormat.format(ServiceURIRegistry.CONTENT_TRANSFORM_SERVICE, idPreview, targetMimetype);
			String response = restClient.request(uri, createdMethod);
			if (response != null && createdMethod.getStatusCode() == 200) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_NODEID)) {
					String idParts = result.getString(KEY_NODEID).replace(":/", "");
					LOGGER.debug("Generating uri content for {}", result.getString(KEY_NODEID));
					return new ContentPreviewDescriptor(MessageFormat.format(PREVIEW_URL, idParts), targetMimetype);
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Requested document: " + document + " is not transformed! ", e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + document + "': " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Requested document: " + document.getDmsId() + " is not transformed");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFCaseInstanceAdapterService# moveDocument
	 * (com.sirma.itt.cmf.beans.model.DMSInstance, com.sirma.itt.cmf.beans.model.SectionInstance)
	 */
	@Override
	public AlfrescoFileAndPropertiesDescriptor moveDocument(DMSInstance documentInstance, DMSInstance target)
			throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(target);
		JSONObject request = new JSONObject();
		try {
			request.put(KEY_ATTACHMENT_ID, documentInstance.getDmsId());
			request.put(KEY_DESTINATION, target.getDmsId());

			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(), true);

			String restResult = restClient.request(ServiceURIRegistry.CMF_DOCUMENT_MOVE, createMethod);
			LOGGER.debug("Move for documentInstance {} result: {}", documentInstance.getId(), restResult);
			// convert the result and get the id
			if (restResult != null) {
				JSONArray nodes = new JSONObject(restResult).getJSONObject(KEY_ROOT_DATA).getJSONArray(KEY_DATA_ITEMS);
				if (nodes.length() == 1) {
					Map<String, Serializable> convertDMSToCMFProperties = docConvertor.convertDMSToCMFProperties(
							nodes.getJSONObject(0).getJSONObject(KEY_PROPERTIES), DMSTypeConverter.DOCUMENT_LEVEL);
					return new AlfrescoFileAndPropertiesDescriptor(documentInstance.getDmsId(),
							convertDMSToCMFProperties, restClientInstance.get());
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during move document: " + documentInstance, e);
		} catch (Exception e) {
			throw new DMSException(
					"Failure during request on '" + documentInstance + "': " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Document '" + documentInstance + "' is not moved!");
	}

	@Override
	public String getDocumentDirectAccessURI(DMSInstance instance) throws DMSException {
		if (instance == null || instance.getDmsId() == null) {
			return null;
		}
		try {
			String uri = null;
			String filename = (String) instance.getProperties().get(DefaultProperties.NAME);
			String filenameEncoded = null;
			if (StringUtils.isNotBlank(filename)) {
				try {
					filenameEncoded = StringEncoder.encode(filename, "UTF-8");
				} catch (Exception e) {
					LOGGER.error("Filename could not be encoded!", e);
				}
			}
			if (filenameEncoded == null) {
				// TODO find extension by mimetype
				filenameEncoded = "file";
			}
			uri = MessageFormat.format(ServiceURIRegistry.CMF_DOCUMENT_ACCESS_URI,
					instance.getDmsId().replace(":/", ""), filenameEncoded, "true");

			return uri;
		} catch (Exception e) {
			throw new DMSException("Document access url retrieval failed!", e);
		}
	}

	@Override
	public String getLibraryDMSId() {
		return libraryCache.computeIfAbsent(adaptersConfiguration.getDmsContainerId().get(), key -> {
			// TODO when R are ready - search by aspect in container
			SearchArguments<FileDescriptor> args = new SearchArguments<>();
			args.setQuery(new Query("PATH", "/app:company_home/st:sites/cm:" + key + "/cm:documentLibrary"));
			try {
				// we just need something to trigger dms search
				SearchArguments<FileDescriptor> search = searchAdapter.search(args, ObjectInstance.class);
				if (search.getResult().size() == 1) {
					return search.getResult().get(0).getId();
				}
			} catch (Exception e) {
				LOGGER.warn("Could not find the document library for site " + key, e);
			}
			return null;
		});
	}

}
