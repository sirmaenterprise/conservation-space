package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSConverterFactory;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService.UploadMode;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.seip.adapters.remote.AlfrescoErrorReader;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * The Class DmsInstanceAlfresco4Service is responsible for general instance objects operations.
 *
 * @author borislav banchev
 */
@ApplicationScoped
public class DmsInstanceAlfresco4Service implements DMSInstanceAdapterService, AlfrescoCommunicationConstants {

	private static final Logger LOGGER = Logger.getLogger(DmsInstanceAlfresco4Service.class);

	@Inject
	private RESTClient restClient;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "document.update.auto.description", label = "If present will be used as an auto generated comment to populate the version description when updating only document properties.")
	private ConfigurationProperty<String> documentUpdateAutoDescription;

	@Inject
	private LabelService labelService;

	@Inject
	private javax.enterprise.inject.Instance<LabelProvider> labelProvider;

	@Inject
	private DMSConverterFactory converterFactory;

	@Inject
	private javax.enterprise.inject.Instance<CMFDocumentAdapterService> documentAdapter;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Override
	public Map<String, Serializable> updateNode(DMSInstance dmsInstance) throws DMSException {
		if (dmsInstance == null || dmsInstance.getDmsId() == null) {
			throw new DMSException("Invalid element '" + dmsInstance + "' is provided for update!");
		}
		try {
			Serializable originalName = dmsInstance.getProperties().remove(DefaultProperties.NAME);
			DMSTypeConverter dmsConverter = converterFactory.getConverter(dmsInstance.getClass());
			JSONObject request = new JSONObject();
			Map<String, Serializable> convertProperties = dmsConverter.convertCMFtoDMSProperties(
					dmsInstance.getProperties(), dmsInstance, DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			request.put(KEY_PROPERTIES, new JSONObject(convertProperties));
			request.put(KEY_DESCRIPTION, getAutoUpdateDescription());
			request.put(KEY_NODEID, dmsInstance.getDmsId());
			HttpMethod createdMethod = restClient.createMethod(new PostMethod(), request.toString(), true);
			String response = restClient.request(ServiceURIRegistry.NODE_UPDATE, createdMethod);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_ROOT_DATA)) {
					JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(KEY_DATA_ITEMS);
					if (nodes.length() == 1) {
						// return nodes
						JSONObject jsonObject = nodes.getJSONObject(0);
						Map<String, Serializable> convertDMSToCMFProperties = dmsConverter.convertDMSToCMFProperties(
								jsonObject, dmsInstance, DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
						convertDMSToCMFProperties.put(DefaultProperties.NAME, originalName);
						return convertDMSToCMFProperties;
					}
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Error during DMS update of: " + dmsInstance.getDmsId(), e);
		} catch (Exception e) {
			throw new DMSException("Error during DMS update! " + AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Instance '" + dmsInstance.getDmsId() + "' is not updated during request!");
	}

	@Override
	public FileAndPropertiesDescriptor attachDocumentToInstance(DMSInstance docInstance, FileDescriptor descriptor,
			String customAspect) throws DMSException {
		if (docInstance == null) {
			throw new DMSException("Invalid document instance is provided!");
		}
		if (docInstance.getDmsId() == null) {
			String dmsId = null;
			// determine the upload container for the new content
			Instance parentNode = InstanceUtil.getDirectParent(docInstance);
			if (parentNode instanceof DMSInstance) {
				dmsId = ((DMSInstance) parentNode).getDmsId();
			}
			if (dmsId == null) {
				dmsId = parentNode.getString("dmsId");
			}
			UploadMode mode = UploadMode.DIRECT;
			return uploadInternalNewFile(docInstance, descriptor, dmsId, mode, customAspect);
		}
		UploadWrapperDescriptor adapter = new UploadWrapperDescriptor(descriptor, null, docInstance.getProperties());
		return documentAdapter.get().uploadNewVersion(docInstance, adapter);
	}

	/**
	 * Gets the auto update description.
	 *
	 * @return the auto update description
	 */
	private String getAutoUpdateDescription() {
		// if configured in external configuration we use it right away
		if (documentUpdateAutoDescription.isSet()) {
			return documentUpdateAutoDescription.get();
		}
		try {
			return labelProvider.get().getLabel(documentUpdateAutoDescription.getName());
		} catch (Exception e) {
			LOGGER.warn("Failed to get autoversion information!", e);
			// if case of error - like no session....
			LabelDefinition label = labelService.getLabel(documentUpdateAutoDescription.get());
			if (label != null) {
				String string = label.getLabels().get(systemConfiguration.getSystemLanguage());
				if (string != null) {
					return string;
				}
			}
			return "";
		}
	}

	@Override
	public FileAndPropertiesDescriptor attachDocumenToLibrary(DMSInstance docInstance, FileDescriptor descriptor,
			String customAspect) throws DMSException {
		if (docInstance.getDmsId() == null) {
			return uploadInternalNewFile(docInstance, descriptor, documentAdapter.get().getLibraryDMSId(),
					UploadMode.CUSTOM, customAspect);
		}

		UploadWrapperDescriptor adapter = new UploadWrapperDescriptor(descriptor, null, docInstance.getProperties());
		return documentAdapter.get().uploadNewVersion(docInstance, adapter);
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
	 * @param uploadMode
	 *            whether document should be organized using dms logic or directly
	 * @param customAspect
	 *            is the type of the document
	 * @return the desriptor of the upload file
	 * @throws DMSException
	 *             on any error
	 */
	private FileAndPropertiesDescriptor uploadInternalNewFile(DMSInstance docInstance, FileDescriptor descriptor,
			String dmsId, UploadMode uploadMode, String customAspect) throws DMSException {
		String aspectFinal = customAspect;
		if (aspectFinal == null) {
			aspectFinal = DocumentProperties.TYPE_DOCUMENT_ATTACHMENT;
		}

		UploadWrapperDescriptor adapter = new UploadWrapperDescriptor(descriptor, dmsId, docInstance.getProperties());
		adapter.setUploadMode(uploadMode);
		return documentAdapter.get().uploadContent(docInstance, adapter, Collections.singleton(aspectFinal));
	}

	/**
	 * Delete node in dms.
	 *
	 * @param dmsInstance
	 *            the dms instance
	 * @return true, if successful
	 * @throws DMSException
	 *             the DMS exception
	 * @see com.sirma.itt.emf.adapter.DMSInstanceAdapterService#deleteNode(com.sirma.itt.seip.domain.instance.DMSInstance)
	 */
	@Override
	public boolean deleteNode(DMSInstance dmsInstance) throws DMSException {
		if (dmsInstance == null || dmsInstance.getDmsId() == null) {
			throw new DMSException("Invalid element id is provided for delete!");
		}
		try {
			String uri = MessageFormat.format(ServiceURIRegistry.DMS_FILE_DELETE,
					dmsInstance.getDmsId().replace(":/", ""));
			HttpMethod createdMethod = restClient.createMethod(new DeleteMethod(), (String) null, true);
			String response = restClient.request(uri, createdMethod);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_OVERALLSUCCESS) && result.getBoolean(KEY_OVERALLSUCCESS)) {
					return true;
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Faild to delete node '" + dmsInstance.getDmsId() + "', code: " + e.getStatusCode(),
					e);
		} catch (Exception e) {
			throw new DMSException(
					"Faild to delete node '" + dmsInstance.getDmsId() + "'!" + AlfrescoErrorReader.parse(e), e);
		}

		return false;
	}

}
