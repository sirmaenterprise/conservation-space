package com.sirma.itt.pm.alfresco4.services;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoErrorReader;
import com.sirma.itt.cmf.alfresco4.AlfrescoUtils;
import com.sirma.itt.cmf.alfresco4.services.convert.Converter;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.pm.alfresco4.ServiceURIRegistry;
import com.sirma.itt.pm.alfresco4.services.convert.PmConverterContants;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.services.adapter.CMFProjectInstanceAdapterService;

/**
 * Service responsible for working with project instances in dms.
 */
@ApplicationScoped
public class ProjectInstanceAlfresco4Service implements CMFProjectInstanceAdapterService,
		AlfrescoCommunicationConstants {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3918037079181596698L;
	/** the logger. */
	private static final Logger LOGGER = Logger.getLogger(ProjectInstanceAlfresco4Service.class);

	/** The Constant DEBUG_ENABLED. */
	private static final boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();
	/** The rest client. */
	@Inject
	private RESTClient restClient;
	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;
	@Inject
	@Converter(name = PmConverterContants.PROJECT)
	private DMSTypeConverter projectConvertor;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createProjectInstance(ProjectInstance projectInstance) throws DMSException {
		ProjectDefinition projectDefinition = dictionaryService.getDefinition(
				ProjectDefinition.class, projectInstance.getIdentifier());
		if (projectDefinition == null) {
			throw new DMSException("Project '" + projectInstance
					+ "' has no valid definition attached!");
		}
		JSONObject request = new JSONObject();
		try {
			request.put(KEY_DEFINITION_ID, projectDefinition.getDmsId());
			generateProjectNumber(projectInstance);
			Map<String, Serializable> convertProperties = projectConvertor
					.convertCMFtoDMSProperties(projectInstance.getProperties(), projectInstance,
							DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			convertProperties.put("cmf:revision", projectInstance.getRevision());
			if (StringUtils.isNotEmpty(projectInstance.getContainer())) {
				request.put(KEY_SITE_ID, projectInstance.getContainer());
			}
			request.put(KEY_PROPERTIES, new JSONObject(convertProperties));

			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);

			String restResult = restClient.request(
					ServiceURIRegistry.PM_PROJECT_INSTANCE_CREATE_SERVICE, createMethod);
			if (DEBUG_ENABLED) {
				LOGGER.debug("Create project '" + projectInstance + "' result: " + restResult);
			}
			// convert the result and get the id
			if (restResult != null) {
				JSONObject parent = new JSONObject(restResult).getJSONObject(KEY_ROOT_DATA)
						.getJSONObject(KEY_PARENT);
				if (parent != null) {
					return parent.getString(KEY_NODEREF);
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Project creation failed! " + e.getMessage(), e);
		} catch (Exception e) {
			throw new DMSException(AlfrescoErrorReader.parse(e), e);
		}

		throw new DMSException("Project '" + projectInstance + "' is not saved!");
	}

	/**
	 * Fix project number if not exists and set it to id property from the map.
	 *
	 * @param projectInstance
	 *            to generate id for
	 */
	private void generateProjectNumber(ProjectInstance projectInstance) {
		// TODO Auto-generated method stub

	}

	@Override
	public String updateProjectInstance(ProjectInstance projectInstance) throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(projectInstance);
		JSONObject request = new JSONObject();
		try {
			request.put(KEY_NODEID, projectInstance.getDmsId());
			// add the properties
			Map<String, Serializable> convertProperties = projectConvertor
					.convertCMFtoDMSProperties(projectInstance.getProperties(), projectInstance,
							DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
			request.put(KEY_PROPERTIES, new JSONObject(convertProperties));

			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);

			String restResult = restClient.request(
					ServiceURIRegistry.PM_PROJECT_INSTANCE_UPDATE_SERVICE, createMethod);
			if (DEBUG_ENABLED) {
				LOGGER.debug("Update project '" + projectInstance + "' result: " + restResult);
			}
			// convert the result and get the id
			if (restResult != null) {
				JSONArray nodes = new JSONObject(restResult).getJSONObject(KEY_ROOT_DATA)
						.getJSONArray(KEY_DATA_ITEMS);
				if (nodes.length() == 1) {
					return nodes.getJSONObject(0).getString(KEY_NODEREF);
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Project update failed! " + e.getMessage(), e);
		} catch (Exception e) {
			throw new DMSException(AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Project '" + projectInstance + "' is not updated!");
	}

	@Override
	public String deleteProjectInstance(ProjectInstance projectInstance, boolean permanent)
			throws DMSException {
		AlfrescoUtils.validateExistingDMSInstance(projectInstance);
		JSONObject request = new JSONObject();
		try {
			request.put(KEY_NODEID, projectInstance.getDmsId());
			request.put(KEY_FORCED_OPERATION, Boolean.valueOf(permanent));

			if (!permanent) { // add properties for not forced delete
				Map<String, Serializable> convertProperties = projectConvertor
						.convertCMFtoDMSProperties(projectInstance.getProperties(),
								projectInstance, DMSTypeConverter.EDITABLE_HIDDEN_MANDATORY_LEVEL);
				request.put(KEY_PROPERTIES, new JSONObject(convertProperties));
			}

			// fixActiveWorkflowData(request, autoWorkflowProperties);
			HttpMethod createMethod = restClient.createMethod(new PostMethod(), request.toString(),
					true);

			String restResult = restClient.request(
					ServiceURIRegistry.PM_PROJECT_INSTANCE_DELETE_SERVICE, createMethod);
			if (DEBUG_ENABLED) {
				LOGGER.debug("Delete project '" + projectInstance + "' result: " + restResult);
			}
			// convert the result and get the id
			if (restResult != null) {
				JSONArray nodes = new JSONObject(restResult).getJSONObject(KEY_ROOT_DATA)
						.getJSONArray(KEY_DATA_ITEMS);
				if (nodes.length() == 1) {
					return nodes.getJSONObject(0).getString(KEY_NODEREF);
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Project deletion failed! " + e.getMessage(), e);
		} catch (Exception e) {
			throw new DMSException(AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Project '" + projectInstance + "' is not deleted!");
	}

}
