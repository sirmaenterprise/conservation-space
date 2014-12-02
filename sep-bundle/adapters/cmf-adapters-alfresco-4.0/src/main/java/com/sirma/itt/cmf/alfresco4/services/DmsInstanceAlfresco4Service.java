/**
 *
 */
package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoErrorReader;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSConverterFactory;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.constants.SectionProperties;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFSearchAdapterService;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.label.LabelDefinition;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.label.LabelService;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;

/**
 * The Class DmsInstanceAlfresco4Service is responsible for general instance objects operations.
 *
 * @author borislav banchev
 */
@ApplicationScoped
public class DmsInstanceAlfresco4Service implements DMSInstanceAdapterService,
		AlfrescoCommunicationConstants {
	/** the logger. */
	private static final Logger LOGGER = Logger.getLogger(DmsInstanceAlfresco4Service.class);
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3714427454516980813L;
	private static final String KEY_PARENT_REF = "parentRef";
	/** The rest client. */
	@Inject
	private RESTClient restClient;

	/** The document update auto description. */
	@Inject
	@Config(name = CmfConfigurationProperties.KEY_DOCUMENT_UPDATE_DESCRIPTION)
	private String documentUpdateAutoDescription;
	/** The label service. */
	@Inject
	private LabelService labelService;
	/** The label provider. */
	@Inject
	private javax.enterprise.inject.Instance<LabelProvider> labelProvider;
	@Inject
	private DMSConverterFactory converterFactory;
	@Inject
	private javax.enterprise.inject.Instance<CMFDocumentAdapterService> documentAdapter;
	@Inject
	private javax.enterprise.inject.Instance<CMFSearchAdapterService> searchAdapter;
	@Inject
	private AuthenticationService authenticationService;

	private Map<String, String> libraryCache = new HashMap<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> updateNode(DMSInstance dmsInstance) throws DMSException {
		if ((dmsInstance == null) || (dmsInstance.getDmsId() == null)
				|| !(dmsInstance instanceof EmfInstance)) {
			throw new DMSException("Invalid element '" + dmsInstance + "' is provided for update!");
		}
		try {
			EmfInstance instance = (EmfInstance) dmsInstance;
			DMSTypeConverter dmsConverter = converterFactory.getConverter(instance.getClass());
			JSONObject request = new JSONObject();
			Map<String, Serializable> convertProperties = dmsConverter.convertCMFtoDMSProperties(
					instance.getProperties(), instance,
					DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			request.put(KEY_PROPERTIES, new JSONObject(convertProperties));
			request.put(KEY_DESCRIPTION, getAutoUpdateDescription());
			request.put(KEY_NODEID, dmsInstance.getDmsId());
			HttpMethod createdMethod = restClient.createMethod(new PostMethod(),
					request.toString(), true);
			String response = restClient.request(ServiceURIRegistry.NODE_UPDATE, createdMethod);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_ROOT_DATA)) {
					JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(
							KEY_DATA_ITEMS);
					if (nodes.length() == 1) {
						// return nodes
						JSONObject jsonObject = nodes.getJSONObject(0);
						return dmsConverter.convertDMSToCMFProperties(jsonObject,
								instance.getRevision(), instance,
								DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);

					}
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Error during DMS update of: " + dmsInstance.getDmsId(), e);
		} catch (Exception e) {
			throw new DMSException("Error during DMS update! " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Instance '" + dmsInstance.getDmsId()
				+ "' is not updated during request!");

	}

	@Override
	public DMSInstance attachFolderToInstance(DMSInstance parent, DMSInstance child)
			throws DMSException {
		if ((parent == null) || (parent.getDmsId() == null)) {
			throw new DMSException("Invalid element '" + parent + "' is provided for parent!");
		}
		if ((child == null) || !(child instanceof Instance)) {
			throw new DMSException("Invalid element  '" + child + "' is provided for child!");
		}
		try {
			Instance childIntance = (Instance) child;
			DMSTypeConverter typeConverter = converterFactory.getConverter(childIntance.getClass());
			Map<String, Serializable> dmsProperties = typeConverter.convertCMFtoDMSProperties(
					childIntance.getProperties(), childIntance,
					DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			JSONObject request = new JSONObject();
			request.put(KEY_NODEID, parent.getDmsId());
			request.put(KEY_PROPERTIES, dmsProperties);
			String type = "cm:folder";
			if (child instanceof SectionInstance) {
				type = "sectionInstance";
			} else {
				// temporary throw until all types are implemented
				throw new RuntimeException("Not implemented for " + child);
			}
			Pair<String, Serializable> typeConverted = typeConverter.convertCMFtoDMSProperty(type,
					"", DMSTypeConverter.PROPERTIES_MAPPING);
			request.put(KEY_TYPE, typeConverted.getFirst().toString());
			HttpMethod createdMethod = restClient.createMethod(new PostMethod(),
					request.toString(), true);
			String response = restClient.request(ServiceURIRegistry.FOLDER_CREATE, createdMethod);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_ROOT_DATA)) {
					JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(
							KEY_DATA_ITEMS);
					if (nodes.length() == 1) {
						String dmsId = nodes.getJSONObject(0).getString(KEY_NODEREF);
						child.setDmsId(dmsId);
						return child;
					}
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Faild to attach '" + child + "' as child to parent '" + parent
					+ "'", e);
		} catch (Exception e) {
			throw new DMSException("Faild to attach '" + child + "' as child to parent '" + parent
					+ "'! " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Faild to attach '" + child + "' as child to parent '" + parent
				+ "'");

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileAndPropertiesDescriptor attachDocumentToInstance(DMSInstance docInstance,
			FileDescriptor descriptor, String customAspect) throws DMSException {
		if ((docInstance == null) || !(docInstance instanceof DocumentInstance)) {
			throw new DMSException("Invalid document is provided: (" + docInstance + ") !");
		}
		DocumentInstance documentInstance = (DocumentInstance) docInstance;
		if (documentInstance.getDmsId() == null) {
			String dmsId = null;
			Instance instance = documentInstance.getOwningInstance();
			if (instance instanceof DMSInstance) {
				dmsId = ((DMSInstance) instance).getDmsId();
			}
			if (dmsId == null) {
				dmsId = (String) instance.getProperties().get(SectionProperties.DMS_ID);
			}
			return uploadInternalNewFile(docInstance, descriptor, dmsId, customAspect);
		}
		UploadWrapperDescriptor adapter = new UploadWrapperDescriptor(descriptor, null,
				documentInstance.getProperties());
		return documentAdapter.get().uploadNewVersion(documentInstance, adapter);
	}

	/**
	 * Wrapper for deleting document from instance
	 *
	 * @param documentInstance
	 *            is the document to delete
	 * @return the dms id of document on success
	 * @throws DMSException
	 *             the dMS exception
	 */
	@Override
	public String dettachDocumentFromInstance(DMSInstance documentInstance) throws DMSException {
		if ((documentInstance.getDmsId() == null)
				|| !(documentInstance instanceof DocumentInstance)) {
			throw new DMSException("Invalid document is provided: (" + documentInstance + ") !");
		}
		return documentAdapter.get().deleteAttachment((DocumentInstance) documentInstance);
	}

	@Override
	public boolean linkAsChild(DMSInstance parent, DMSInstance child, DMSInstance parentToRemove,
			String assocName) throws DMSException {
		if ((parent == null) || (parent.getDmsId() == null)) {
			throw new DMSException("Invalid element id is provided for parent!");
		}
		if ((child == null) || (child.getDmsId() == null)) {
			throw new DMSException("Invalid element id is provided for child!");
		}
		try {
			JSONObject request = new JSONObject();
			request.put(KEY_NODEID, parent.getDmsId());
			request.put(KEY_REFERENCE_ID, child.getDmsId());
			String requestURI = "/cmf/node/link";
			if (parentToRemove != null) {
				request.put(KEY_PARENT_REF, parentToRemove.getDmsId());
				requestURI = "/cmf/node/relink";
			}
			if (StringUtils.isNotNullOrEmpty(assocName)) {
				request.put(KEY_CHILD_ASSOC_NAME, assocName);
			}
			HttpMethod createdMethod = restClient.createMethod(new PostMethod(),
					request.toString(), true);
			String response = restClient.request(requestURI, createdMethod);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_ROOT_DATA)) {
					JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(
							KEY_DATA_ITEMS);
					if (nodes.length() == 1) {
						return true;
					}
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Faild to link '" + child + "' to parent '" + parent + "'", e);
		} catch (Exception e) {
			throw new DMSException("Faild to link '" + child + "' to parent '" + parent + "'! "
					+ AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Faild to link '" + child + "' to parent '" + parent + "'");
	}

	@Override
	public boolean removeLinkAsChild(DMSInstance parent, DMSInstance child, String assocName)
			throws DMSException {
		// TODO implement the method removeLinkAsChild to remove an association from DMS
		return false;
	}

	/**
	 * Gets the auto update description.
	 *
	 * @return the auto update description
	 */
	private String getAutoUpdateDescription() {
		// if configured in external configuration we use it right away
		if (StringUtils.isNotNullOrEmpty(documentUpdateAutoDescription)) {
			return documentUpdateAutoDescription;
		}
		try {
			return labelProvider.get().getLabel(
					CmfConfigurationProperties.KEY_DOCUMENT_UPDATE_DESCRIPTION);
		} catch (Exception e) {
			LOGGER.warn("Failed to get autoversion information!", e);
			// if case of error - like no session....
			LabelDefinition label = labelService
					.getLabel(CmfConfigurationProperties.KEY_DOCUMENT_UPDATE_DESCRIPTION);
			if (label != null) {
				String language = SecurityContextManager.getSystemLanguage();
				// we can override default language
				if (RuntimeConfiguration
						.isConfigurationSet(RuntimeConfigurationProperties.CURRENT_LANGUAGE_CODE)) {
					language = (String) RuntimeConfiguration
							.getConfiguration(RuntimeConfigurationProperties.CURRENT_LANGUAGE_CODE);
				}
				String string = label.getLabels().get(language);
				if (string != null) {
					return string;
				}
			}
			return "";
		}
	}

	/**
	 * Get the library dms id. The library is tenant dependent.
	 *
	 * @param tenantId
	 *            is the tenant to find library in
	 * @return the library dms id or throws exception if not found
	 * @throws DMSException
	 *             if space is not found
	 */
	public String getLibraryDMSId(String tenantId) throws DMSException {
		// TODO when R are ready - search by aspect in container
		if (!libraryCache.containsKey(tenantId)) {
			SearchArguments<FileDescriptor> args = new SearchArguments<>();
			args.setQuery(new Query("PATH", "/app:company_home/st:sites/cm:" + tenantId
					+ "/cm:documentLibrary"));
			try {
				SearchArguments<FileDescriptor> search = searchAdapter.get().search(args,
						CaseInstance.class);
				if (search.getResult().size() == 1) {
					libraryCache.put(tenantId, search.getResult().get(0).getId());
					return libraryCache.get(tenantId);
				}
			} catch (DMSException e) {
				throw e;
			}
			throw new DMSException("Document library for " + tenantId + " is not found!");
		}
		return libraryCache.get(tenantId);
	}

	@Override
	public FileAndPropertiesDescriptor attachDocumenToLibrary(DMSInstance docInstance,
			FileDescriptor descriptor, String customAspect) throws DMSException {
		if (!(docInstance instanceof DocumentInstance)) {
			throw new DMSException("The provided instance (" + docInstance
					+ ") is not an expected type!");
		}
		if (docInstance.getDmsId() == null) {
			return uploadInternalNewFile(docInstance, descriptor,
					getLibraryDMSId(authenticationService.getCurrentContainer()), customAspect);
		}
		UploadWrapperDescriptor adapter = new UploadWrapperDescriptor(descriptor, null,
				((PropertyModel) docInstance).getProperties());
		return documentAdapter.get().uploadNewVersion((DocumentInstance) docInstance, adapter);
	}

	/**
	 * Internal method to upload file to dms. Method is used for new files only.
	 *
	 * @param docInstance
	 *            is the instance that is attached
	 * @param descriptor
	 *            is the desriptor for the upload
	 * @param dmsId
	 *            is the dms id of the container to attach to
	 * @param customAspect
	 *            is the type of the document
	 * @return the desriptor of the upload file
	 * @throws DMSException
	 *             on any error
	 */
	private FileAndPropertiesDescriptor uploadInternalNewFile(DMSInstance docInstance,
			FileDescriptor descriptor, String dmsId, String customAspect) throws DMSException {
		DocumentInstance documentInstance = (DocumentInstance) docInstance;
		String aspectFinal = customAspect;
		if (aspectFinal == null) {
			// if structured set the proper aspect
			if (Boolean.TRUE.equals(documentInstance.getStructured())) {
				aspectFinal = DocumentProperties.TYPE_DOCUMENT_STRUCTURED;
			} else {
				aspectFinal = DocumentProperties.TYPE_DOCUMENT_ATTACHMENT;
			}
		}
		UploadWrapperDescriptor adapter = new UploadWrapperDescriptor(descriptor, dmsId,
				documentInstance.getProperties());
		Set<String> aspectsProp = new HashSet<String>(1);
		aspectsProp.add(aspectFinal);
		return documentAdapter.get().uploadContent(documentInstance, adapter, aspectsProp);
	}

	/**
	 * Delete node in dms.
	 *
	 * @param dmsInstance
	 *            the dms instance
	 * @return true, if successful
	 * @throws DMSException
	 *             the DMS exception
	 * @see com.sirma.itt.emf.adapter.DMSInstanceAdapterService#deleteNode(com.sirma.itt.emf.instance.model.DMSInstance)
	 */
	@Override
	public boolean deleteNode(DMSInstance dmsInstance) throws DMSException {
		if ((dmsInstance == null) || (dmsInstance.getDmsId() == null)
				|| !(dmsInstance instanceof EmfInstance)) {
			throw new DMSException("Invalid element id is provided for delete!");
		}
		String uri;
		if (dmsInstance instanceof DocumentInstance) {
			uri = ServiceURIRegistry.DMS_FILE_DELETE;
		} else if (dmsInstance instanceof SectionInstance) {
			uri = ServiceURIRegistry.DMS_FOLDER_DELETE;
		} else {
			throw new DMSException("Unsupported node type for deletion: " + dmsInstance.getClass());
		}
		try {
			uri = MessageFormat.format(uri, dmsInstance.getDmsId().replace(":/", ""));
			HttpMethod createdMethod = restClient.createMethod(new DeleteMethod(), (String) null,
					true);
			String response = restClient.request(uri, createdMethod);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_OVERALLSUCCESS)) {
					boolean overAllSuccess = result.getBoolean(KEY_OVERALLSUCCESS);
					if (overAllSuccess) {
						return true;
					}
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Faild to delete instance '" + dmsInstance + "'", e);
		} catch (Exception e) {
			throw new DMSException("Faild to delete instance '" + dmsInstance + "'!"
					+ AlfrescoErrorReader.parse(e), e);
		}
		return false;
	}

}
