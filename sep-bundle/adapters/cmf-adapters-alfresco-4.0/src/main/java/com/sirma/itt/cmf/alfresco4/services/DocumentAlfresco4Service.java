/**
 *
 */
package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoErrorReader;
import com.sirma.itt.cmf.alfresco4.AlfrescoUtils;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileAndPropertiesDescriptor;
import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileDescriptor;
import com.sirma.itt.cmf.alfresco4.remote.AlfrescoUploader;
import com.sirma.itt.cmf.alfresco4.services.convert.Converter;
import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.beans.ContentPreviewDescriptor;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.VersionInfo;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.emf.util.StringEncoder;

/**
 * The class adapter for documents in dms<->cmf communication. Has a methods to upload/update
 * documents in dms.
 *
 * @author Borislav Banchev
 */
@ApplicationScoped
public class DocumentAlfresco4Service implements CMFDocumentAdapterService,
		AlfrescoCommunicationConstants {

	private static final String PREVIEW_URL = ServiceURIRegistry.CMF_TO_DMS_PROXY_SERVICE
			+ ServiceURIRegistry.CONTENT_ACCESS_URI;
	/** the logger. */
	private static final Logger LOGGER = Logger.getLogger(DocumentAlfresco4Service.class);
	/** The debug enabled. */
	private boolean debugEnabled;
	/** The rest client. */
	@Inject
	private RESTClient restClient;
	@Inject
	private Instance<RESTClient> restClientInstance;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;
	/** The alfresco uploader. */
	@Inject
	private AlfrescoUploader alfrescoUploader;

	/** The doc convertor. */
	@Inject
	@Converter(name = ConverterConstants.DOCUMENT)
	private DMSTypeConverter docConvertor;

	/**
	 * default contstructor.
	 */
	public DocumentAlfresco4Service() {
		debugEnabled = LOGGER.isDebugEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlfrescoFileAndPropertiesDescriptor performDocumentOperation(DocumentInstance document,
			DocumentOperation operation) throws DMSException {
		return performDocumentOperation(document, null, operation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileAndPropertiesDescriptor checkOut(DocumentInstance document, String userId)
			throws DMSException {
		JSONObject object = performOperation(document, userId, DocumentOperation.CHECKOUT);

		String serializable = null;
		Map<String, Serializable> map = null;
		try {
			map = docConvertor.convertDMSToCMFProperties(object, document.getRevision(), document,
					DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);

			serializable = object.getString(KEY_NODEREF);
			String container = null;
			if (object.has(KEY_SITE_ID)) {
				container = object.getString(KEY_SITE_ID);
			}

			map.put(DocumentProperties.WORKING_COPY_LOCATION, new AlfrescoFileDescriptor(
					serializable, container, restClientInstance.get()));
			map.put(DocumentProperties.LOCKED_BY, userId);
		} catch (Exception e) {
			throw new DMSException("Failed to convert properties from the response", e);
		}

		return new AlfrescoFileAndPropertiesDescriptor(document.getDmsId(), map,
				restClientInstance.get());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlfrescoFileAndPropertiesDescriptor performDocumentOperation(DocumentInstance document,
			String userId, DocumentOperation operation) throws DMSException {
		// perform the operation as specified
		JSONObject object = performOperation(document, userId, operation);
		Map<String, Serializable> dmsToCMFProperties = null;
		try {
			// convert the new data
			dmsToCMFProperties = docConvertor.convertDMSToCMFProperties(object,
					document.getRevision(), document,
					DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
		} catch (Exception e) {
			throw new DMSException("Failed to convert properties from the response", e);
		}

		return new AlfrescoFileAndPropertiesDescriptor(document.getDmsId(), dmsToCMFProperties,
				restClient);
	}

	/**
	 * Performs a given operation on the given instance. The actual service to execute is determined
	 * by the 'operation' argument
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
	private JSONObject performOperation(DocumentInstance document, String userId,
			DocumentOperation operation) throws DMSException {
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
				Serializable temp = document.getProperties().get(
						DocumentProperties.IS_MAJOR_VERSION);
				Boolean isMajor = temp == null ? Boolean.FALSE : Boolean.valueOf(temp.toString());
				temp = document.getProperties().get(DocumentProperties.VERSION_DESCRIPTION);
				String description = temp == null ? "Checked in file" : temp.toString();
				request.put(KEY_DESCRIPTION, description);
				request.put(KEY_MAJOR_VERSION, isMajor);
				url = ServiceURIRegistry.CMF_DOCUMENT_CHECKIN;
				// add the properties
				request.put(KEY_PROPERTIES, docConvertor.convertCMFtoDMSProperties(
						document.getProperties(), document,
						DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL));
			} else if (operation == DocumentOperation.CHECKOUT) {
				url = ServiceURIRegistry.CMF_DOCUMENT_CHECKOUT;
			} else if (operation == DocumentOperation.LOCK) {
				url = ServiceURIRegistry.CMF_DOCUMENT_LOCK_NODE;
			} else if (operation == DocumentOperation.UNLOCK) {
				url = ServiceURIRegistry.CMF_DOCUMENT_UNLOCK_NODE;
			} else if (operation == DocumentOperation.CANCEL_CHECKOUT) {
				Serializable workingCopyId = document.getProperties().get(
						DocumentProperties.WORKING_COPY_LOCATION);
				if (workingCopyId != null) {
					attachmentId = workingCopyId;
				}
				url = ServiceURIRegistry.CMF_DOCUMENT_CHECKOUT_CANCEL;
			}
			if (userId != null) {
				request.put(KEY_LOCK_OWNER, userId);
			}

			request.put(KEY_ATTACHMENT_ID, attachmentId);
			HttpMethod createdMethod = restClient.createMethod(new PostMethod(),
					request.toString(), true);
			String response = restClient.request(url, createdMethod);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_ROOT_DATA)) {
					JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(
							KEY_DATA_ITEMS);
					if (nodes.length() == 1) {
						// return nodes
						JSONObject resultNode = nodes.getJSONObject(0);
						return resultNode;
					}
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during execution of operation '" + operation + "' on:"
					+ document, e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + document + "': "
					+ AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Attachment " + document.getId() + " is not updated during request!");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService#uploadNewVersion
	 * (com.sirma.itt.cmf.beans.model.DocumentInstance,
	 * com.sirma.itt.cmf.services.adapter.DMSFileDescriptor)
	 */
	@Override
	public FileAndPropertiesDescriptor uploadNewVersion(DocumentInstance document,
			FileAndPropertiesDescriptor descriptor) throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(document);
		// get the file
		String uploadFile = null;
		String dmsId = document.getDmsId();
		// if we are performing checkIn operation
		Serializable workingCopyId = document.getProperties().get(
				DocumentProperties.WORKING_COPY_LOCATION);
		if (workingCopyId != null) {
			dmsId = (String) workingCopyId;
		}

		Map<String, Serializable> propertiesProp = descriptor.getProperties();
		Map<String, Serializable> copyOfData = null;
		if (propertiesProp != null) {
			copyOfData = new HashMap<String, Serializable>(propertiesProp);
		} else {
			copyOfData = new HashMap<>();
		}
		// remove the name as it comes from the upload descriptor
		copyOfData.remove(DocumentProperties.NAME);
		copyOfData = docConvertor.convertCMFtoDMSProperties(copyOfData, document,
				DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
		// remove name as it is automatically maintained
		Set<String> aspectsProp = new TreeSet<String>();
		Serializable temp = document.getProperties().get(DocumentProperties.IS_MAJOR_VERSION);
		Boolean isMajor = temp == null ? Boolean.FALSE : Boolean.valueOf(temp.toString());
		temp = document.getProperties().get(DocumentProperties.VERSION_DESCRIPTION);
		String description = temp == null ? "" : temp.toString();
		if (debugEnabled) {
			LOGGER.debug("New version for " + document.getDmsId() + " isMajor " + isMajor
					+ " properties: " + copyOfData);
		}
		try {
			ThumbnailGenerationMode thumbnailMode = (ThumbnailGenerationMode) document
					.getProperties().get(DocumentProperties.DOCUMENT_THUMB_MODE);

			uploadFile = alfrescoUploader.updateFile(
					ServiceURIRegistry.CMF_ATTACH_TO_INSTANCE_SERVICE, descriptor, dmsId,
					TYPE_CM_CONTENT, copyOfData, aspectsProp, isMajor, description,
					thumbnailMode != null ? thumbnailMode.toString() : null);

			// upload the file now
			if (uploadFile != null) {
				JSONObject fileData = new JSONObject(uploadFile);
				Map<String, Serializable> dmsToCMFProperties = docConvertor
						.convertDMSToCMFProperties(fileData, document.getRevision(), document,
								DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
				return new AlfrescoFileAndPropertiesDescriptor(fileData.getString(KEY_NODEREF),
						null, dmsToCMFProperties, restClient);
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during upload new version of document on: " + document,
					e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + document + "': "
					+ AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("File is not uploaded!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileAndPropertiesDescriptor uploadContent(DocumentInstance documentInstance,
			FileAndPropertiesDescriptor descriptor, Set<String> aspectsToInclude)
			throws DMSException {
		if (documentInstance.getDmsId() != null) {
			// throw exception since not all data is used in update - might get unexpected result
			throw new DMSException("File is already known in dms! Use update instead!");
		}

		if (descriptor.getContainerId() == null) {
			throw new DMSException("The container for uploaded file is not known!");
		}
		String uploadFile = null;

		String dmsId = descriptor.getContainerId();
		Map<String, Serializable> propertiesProp = null;
		Map<String, Serializable> metadataToInclude = descriptor.getProperties();
		if (metadataToInclude == null) {
			propertiesProp = new HashMap<String, Serializable>(1);
		} else {
			Map<String, Serializable> copyOfData = new HashMap<String, Serializable>(
					metadataToInclude);
			copyOfData.remove(DocumentProperties.NAME);
			propertiesProp = docConvertor.convertCMFtoDMSProperties(copyOfData, documentInstance,
					DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			// remove name as it is automatically maintained
		}

		Set<String> aspectsProp = new HashSet<String>();
		if (aspectsToInclude != null) {
			for (String nextAspect : aspectsToInclude) {

				Pair<String, Serializable> dmsAspect = docConvertor.convertCMFtoDMSProperty(
						nextAspect, "", DMSTypeConverter.PROPERTIES_MAPPING);
				if (dmsAspect == null) {
					throw new DMSException("Invalid aspect provided (" + nextAspect + ")");
				}
				aspectsProp.add(dmsAspect.getFirst());
			}
		}
		try {
			ThumbnailGenerationMode thumbnailMode = (ThumbnailGenerationMode) documentInstance
					.getProperties().get(DocumentProperties.DOCUMENT_THUMB_MODE);
			uploadFile = alfrescoUploader.uploadFile(
					ServiceURIRegistry.CMF_ATTACH_TO_INSTANCE_SERVICE, descriptor, null, null,
					dmsId, TYPE_CM_CONTENT, propertiesProp, aspectsProp,
					thumbnailMode != null ? thumbnailMode.toString() : null);

			// upload the file now
			if (uploadFile != null) {
				JSONObject fileData = new JSONObject(uploadFile);
				Map<String, Serializable> dmsToCMFProperties = docConvertor
						.convertDMStoCMFPropertiesByValue(fileData, documentInstance.getRevision(),
								documentInstance, DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
				return new AlfrescoFileAndPropertiesDescriptor(fileData.getString(KEY_NODEREF),
						null, dmsToCMFProperties, restClient);
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during uploading document: " + documentInstance, e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + documentInstance + "': "
					+ AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("File is not uploaded!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String deleteAttachment(DocumentInstance documentInstance) throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(documentInstance);
		Serializable attachmentId = documentInstance.getDmsId();
		Serializable caseId = documentInstance.getProperties().get(DocumentProperties.CASE_DMS_ID);
		JSONObject request = new JSONObject();
		try {

			request.put(KEY_ATTACHMENT_ID, attachmentId);
			request.put(KEY_NODEID, caseId);
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);
			String restResult = restClient.request(
					ServiceURIRegistry.CMF_DETTACH_FROM_INSTANCE_SERVICE, createMethod);
			if (restResult != null) {
				JSONObject result = new JSONObject(restResult);
				if (result.has("deleted")) {
					if (attachmentId.equals(result.getString("deleted"))) {
						return attachmentId.toString();
					}
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during deletion of document: " + documentInstance, e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + documentInstance + "': "
					+ AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Attachment " + documentInstance.getId() + " is not deleted!");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService#
	 * getDocumentPreview(com.sirma.itt.cmf.beans.model.DocumentInstance)
	 */
	@Override
	public FileDescriptor getDocumentPreview(DocumentInstance document, String targetMimetype)
			throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(document);
		try {
			HttpMethod createdMethod = restClient.createMethod(new GetMethod(), "", true);
			String idPreview = document.getDmsId().replace(":/", "");
			String uri = MessageFormat.format(ServiceURIRegistry.CONTENT_TRANSFORM_SERVICE,
					idPreview, targetMimetype);
			String response = restClient.request(uri, createdMethod);
			if ((response != null) && (createdMethod.getStatusCode() == 200)) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_NODEID)) {
					String idParts = result.getString(KEY_NODEID).replace(":/", "");
					if (debugEnabled) {
						LOGGER.debug("Generating uri content for " + result.getString(KEY_NODEID));
					}
					return new ContentPreviewDescriptor(MessageFormat.format(PREVIEW_URL, idParts),
							targetMimetype);
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Requested document: " + document + " is not transformed! ", e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + document + "': "
					+ AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Requested document: " + document.getId() + " is not transformed");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService#
	 * performDocumentInfoOperation (com.sirma.itt.cmf.beans.model.DocumentInstance, java.util.Set)
	 */
	@Override
	public Map<DocumentInfoOperation, Serializable> performDocumentInfoOperation(
			DocumentInstance document, Set<DocumentInfoOperation> operations) throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(document);
		if (operations == null) {
			throw new DMSException("No operation provided for execution on: " + document);
		}
		try {
			Map<DocumentInfoOperation, Serializable> resultData = new HashMap<DocumentInfoOperation, Serializable>();
			for (DocumentInfoOperation documentInfoOperation : operations) {
				switch (documentInfoOperation) {
					case DOCUMENT_VERSIONS: {
						HttpMethod createdMethod = restClient.createMethod(new GetMethod(), "",
								true);
						String response = restClient.request(
								ServiceURIRegistry.CMF_DOCUMENT_VERSION_BY_NODE
										+ document.getDmsId(), createdMethod);
						if (response != null) {
							ArrayList<VersionInfo> versions = new ArrayList<VersionInfo>();
							JSONArray result = new JSONArray(response);
							for (int i = 0; i < result.length(); i++) {
								JSONObject versionInfo = result.getJSONObject(i);
								VersionInfo version = new VersionInfo(document,
										versionInfo.getString(KEY_LABEL),
										versionInfo.getString(KEY_NAME), typeConverter.convert(
												Date.class,
												versionInfo.getString(KEY_CREATED_DATE_ISO)),
										versionInfo.getJSONObject(KEY_CREATOR).getString(
												KEY_USER_NAME),
										versionInfo.getString(KEY_DESCRIPTION));
								versions.add(version);
							}
							resultData.put(documentInfoOperation, versions);
						}
						break;
					}
					case DOCUMENT_INFO:
						break;
					case DOCUMENT_WORKFLOWS:
						break;
					default:
						break;
				}
			}
			return resultData;
		} catch (DMSClientException e) {
			throw new DMSException("Failure during operations '" + operations + "' on document: "
					+ document.getIdentifier(), e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + document + "': "
					+ AlfrescoErrorReader.parse(e), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService#
	 * getDocumentVersion(com.sirma.itt.cmf.beans.model.DocumentInstance, java.lang.String)
	 */
	@Override
	public DocumentInstance getDocumentVersion(DocumentInstance documentInstance, String version)
			throws DMSException {
		Map<String, Serializable> dmsToCMFProperties = versionOperation(documentInstance, version,
				ServiceURIRegistry.CMF_DOCUMENT_HISTORIC_VERSION);
		DocumentInstance clone = documentInstance.clone();
		// the location to set
		clone.setDmsId((String) dmsToCMFProperties.get(DocumentProperties.ATTACHMENT_LOCATION));
		clone.getProperties().putAll(dmsToCMFProperties);
		return clone;
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService#
	 * getDocumentVersion(com.sirma.itt.cmf.beans.model.DocumentInstance, java.lang.String)
	 */
	@Override
	public DocumentInstance revertVersion(DocumentInstance documentInstance, String version)
			throws DMSException {
		Map<String, Serializable> dmsToCMFProperties = versionOperation(documentInstance, version,
				ServiceURIRegistry.CMF_DOCUMENT_REVERT_VERSION);
		dmsToCMFProperties.remove(KEY_NODEREF);
		documentInstance.getProperties().putAll(dmsToCMFProperties);
		return documentInstance;
	}

	/**
	 * Version operation.
	 *
	 * @param documentInstance
	 *            the document instance
	 * @param version
	 *            the version
	 * @param service
	 *            the service
	 * @return the map
	 * @throws DMSException
	 *             the dMS exception
	 */
	private Map<String, Serializable> versionOperation(DocumentInstance documentInstance,
			String version, String service) throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(documentInstance);
		Serializable attachmentId = documentInstance.getDmsId();
		JSONObject request = new JSONObject();
		try {

			request.put(KEY_ATTACHMENT_ID, attachmentId);
			request.put(KEY_VERSION, version);
			// update the revert data if revert is pressed
			if (ServiceURIRegistry.CMF_DOCUMENT_REVERT_VERSION.equals(service)) {
				Serializable temp = documentInstance.getProperties().get(
						DocumentProperties.IS_MAJOR_VERSION);
				Boolean isMajor = temp == null ? Boolean.FALSE : Boolean.valueOf(temp.toString());
				temp = documentInstance.getProperties().get(DocumentProperties.VERSION_DESCRIPTION);
				String description = temp == null ? "" : temp.toString();
				request.put(KEY_MAJOR_VERSION, isMajor);
				request.put(KEY_DESCRIPTION, description);
			}
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);
			String restResult = restClient.request(service, createMethod);
			if (restResult != null) {
				JSONObject result = new JSONObject(restResult);
				if (result.has(KEY_ROOT_DATA)) {
					JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(
							KEY_DATA_ITEMS);
					if (nodes.length() == 1) {
						// return metadata
						Map<String, Serializable> dmsToCMFProperties = docConvertor
								.convertDMSToCMFProperties(
										nodes.getJSONObject(0).getJSONObject(KEY_PROPERTIES),
										DMSTypeConverter.DOCUMENT_LEVEL);
						return dmsToCMFProperties;
					}
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during version operation for '" + version
					+ "' on document: " + documentInstance, e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + documentInstance + "': "
					+ AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Attachment " + documentInstance.getId() + " is not deleted!");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFCaseInstanceAdapterService#moveDocument
	 * (com.sirma.itt.cmf.beans.model.DocumentInstance,
	 * com.sirma.itt.cmf.beans.model.SectionInstance)
	 */
	@Override
	public AlfrescoFileAndPropertiesDescriptor moveDocument(DocumentInstance documentInstance,
			DMSInstance target) throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(target);
		JSONObject request = new JSONObject();
		try {
			request.put(KEY_ATTACHMENT_ID, documentInstance.getDmsId());
			request.put(KEY_DESTINATION, target.getDmsId());

			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);

			String restResult = restClient.request(ServiceURIRegistry.CMF_DOCUMENT_MOVE,
					createMethod);
			if (debugEnabled) {
				LOGGER.debug("Move for documentInstance" + documentInstance.getId() + " result: "
						+ restResult);
			}
			// convert the result and get the id
			if (restResult != null) {
				JSONArray nodes = new JSONObject(restResult).getJSONObject(KEY_ROOT_DATA)
						.getJSONArray(KEY_DATA_ITEMS);
				if (nodes.length() == 1) {
					Map<String, Serializable> convertDMSToCMFProperties = docConvertor
							.convertDMSToCMFProperties(
									nodes.getJSONObject(0).getJSONObject(KEY_PROPERTIES),
									DMSTypeConverter.DOCUMENT_LEVEL);
					return new AlfrescoFileAndPropertiesDescriptor(documentInstance.getDmsId(),
							convertDMSToCMFProperties, restClientInstance.get());
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during move document: " + documentInstance, e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + documentInstance + "': "
					+ AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Document '" + documentInstance + "' is not moved!");
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFCaseInstanceAdapterService#copyDocument
	 * (com.sirma.itt.cmf.beans.model.DocumentInstance,
	 * com.sirma.itt.cmf.beans.model.SectionInstance, java.lang.String)
	 */
	@Override
	public AlfrescoFileAndPropertiesDescriptor copyDocument(DocumentInstance documentInstance,
			DMSInstance target, String newDocumentName) throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(documentInstance);
		AlfrescoUtils.validateExistingDMSInstance(target);
		JSONObject request = new JSONObject();
		try {
			request.put(KEY_ATTACHMENT_ID, documentInstance.getDmsId());
			request.put(KEY_DESTINATION, target.getDmsId());
			request.put(KEY_NAME, newDocumentName);
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);

			String restResult = restClient.request(ServiceURIRegistry.CMF_DOCUMENT_COPY,
					createMethod);
			if (debugEnabled) {
				LOGGER.debug("Copy for documentInstance" + documentInstance.getId() + " result: "
						+ restResult);
			}
			// convert the result and get the id
			if (restResult != null) {
				JSONArray nodes = new JSONObject(restResult).getJSONObject(KEY_ROOT_DATA)
						.getJSONArray(KEY_DATA_ITEMS);
				if (nodes.length() == 1) {
					Map<String, Serializable> convertDMSToCMFProperties = docConvertor
							.convertDMSToCMFProperties(
									nodes.getJSONObject(0).getJSONObject(KEY_PROPERTIES),
									DMSTypeConverter.DOCUMENT_LEVEL);
					return new AlfrescoFileAndPropertiesDescriptor(documentInstance.getDmsId(),
							convertDMSToCMFProperties, restClientInstance.get());
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Failure during copy document: " + documentInstance, e);
		} catch (Exception e) {
			throw new DMSException("Failure during request on '" + documentInstance + "': "
					+ AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Document '" + documentInstance + "' is not copied!");
	}

	@Override
	public String getDocumentDirectAccessURI(DocumentInstance instance) throws DMSException {
		if ((instance == null) || (instance.getDmsId() == null)) {
			return null;
		}
		try {
			String uri = null;
			String filename = (String) instance.getProperties().get(DocumentProperties.NAME);
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
			uri = MessageFormat.format(ServiceURIRegistry.CMF_DOCUMENT_ACCESS_URI, instance
					.getDmsId().replace(":/", ""), filenameEncoded, "true");
			return uri;
		} catch (Exception e) {
			throw new DMSException("Document access url retrieval failed!", e);
		}
	}
}
