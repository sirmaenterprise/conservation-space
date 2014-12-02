package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoErrorReader;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.services.convert.Converter;
import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSConverterFactory;
import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.services.adapter.CMFPermissionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * Implementation for {@link CMFPermissionAdapterService}.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class PermissionAlfresco4Service implements CMFPermissionAdapterService,
		AlfrescoCommunicationConstants {
	/** The case converter. */
	@Inject
	@Converter(name = ConverterConstants.CASE)
	private DMSTypeConverter caseConvertor;

	/** The Constant LOGGER. */
	@Inject
	private Logger LOGGER;

	/** The permission propserties. */
	@Inject
	@Config(name = CmfConfigurationProperties.INHERITED_DOCUMENT_PERMISSIONS)
	private String permissionPropserties;

	/** The rest client. */
	@Inject
	private RESTClient restClient;

	/** The convertor factory. */
	@Inject
	private DMSConverterFactory convertorFactory;

	/*
	 * (non-Javadoc)
	 * @see
	 * com.sirma.itt.cmf.services.adapter.CMFPermissionAdapterService#updateCaseDocuments(com.sirma
	 * .itt.cmf.beans.model.CaseInstance, java.util.Map)
	 */
	@Override
	public void updateCaseDocuments(CaseInstance instance, Map<String, Serializable> additionalProps)
			throws DMSException {
		// REVIEW Why this is not in {@link CMFCaseInstanceAdapterService}
		if ((instance == null) || (instance.getDmsId() == null)) {
			throw new DMSException("Invalid case id is provided for update!");
		}
		// if nothing to update
		if ((permissionPropserties == null)
				&& ((additionalProps == null) || additionalProps.isEmpty())) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Skipping permission update!");
			}
			return;
		}
		try {
			JSONObject request = new JSONObject();
			// prepare data for optimized storage
			if (additionalProps.containsKey(LIST_OF_ALLOWED_USERS)) {
				additionalProps.put(
						LIST_OF_ALLOWED_USERS,
						generatePermissionEntriesValue(additionalProps.get(LIST_OF_ALLOWED_USERS),
								false));
			}
			if (additionalProps.containsKey(LIST_OF_ALLOWED_GROUPS)) {
				additionalProps.put(
						LIST_OF_ALLOWED_GROUPS,
						generatePermissionEntriesValue(additionalProps.get(LIST_OF_ALLOWED_GROUPS),
								false));
			}
			if (additionalProps.containsKey(LIST_OF_ACTIVE_USERS)) {
				additionalProps.put(
						LIST_OF_ACTIVE_USERS,
						generatePermissionEntriesValue(additionalProps.get(LIST_OF_ACTIVE_USERS),
								false));
			}
			Map<String, Serializable> convertProperties = caseConvertor
					.convertCMFtoDMSPropertiesByValue(additionalProps,
							DMSTypeConverter.PROPERTIES_MAPPING);
			request.put(KEY_PROPERTIES, new JSONObject(convertProperties));
			request.put(KEY_COPYABLE, permissionPropserties == null ? JSONObject.NULL
					: permissionPropserties);
			request.put(KEY_NODEID, instance.getDmsId());
			HttpMethod updateRequest = restClient.createMethod(new PostMethod(),
					request.toString(), true);
			String response = restClient.request(ServiceURIRegistry.CMF_PERMISSIONS_FIX,
					updateRequest);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_ROOT_DATA)) {
					JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(
							KEY_DATA_ITEMS);
					if (nodes.length() != 1) {
						throw new DMSException("Case is not updated");
					}
					return;
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Permission update failed! " + e.getMessage(), e);
		} catch (Exception e) {
			throw new DMSException(AlfrescoErrorReader.parse(e), e);
		}
		throw new DMSException("Element " + instance.getDmsId() + " is not updated during request!");

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.sirma.itt.cmf.services.adapter.CMFPermissionAdapterService#searchableUserId(java.lang
	 * .Object)
	 */
	@Override
	public String searchableUserId(Serializable users) {
		if (users == null) {
			return null;
		}
		if ((users instanceof Collection) || users.getClass().isArray()) {
			throw new RuntimeException("Only single values is allowed for user id");
		}

		return generatePermissionEntriesValue(users, true);
	}

	/**
	 * Internal methtod to generate comma separated string. Separator is | as it is forbidden for
	 * users.
	 *
	 * @param all
	 *            the whole value
	 * @param searchable
	 *            is this searchable or storable argument
	 * @return the updated string
	 */
	private String generatePermissionEntriesValue(Object all, boolean searchable) {
		StringBuilder builder = new StringBuilder(searchable ? "*|" : "|");
		if (all instanceof Collection) {
			Collection<?> items = (Collection<?>) all;
			// add trailing
			for (Object item : items) {
				if (item instanceof Resource) {
					builder.append(((Resource) item).getIdentifier()).append("|");
				} else if (item != null) {
					builder.append(item.toString()).append("|");
				}
			}
		} else if (all instanceof String) {
			builder.append(all).append("|");
		} else {
			throw new RuntimeException("Invalid user id!");
		}
		if (builder.length() > (searchable ? 2 : 1)) {
			return builder.append(searchable ? "*" : "").toString();
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.sirma.itt.cmf.services.adapter.CMFPermissionAdapterService#updateMembers(com.sirma.itt
	 * .emf.instance.model.DMSInstance, java.util.List)
	 */
	@Override
	public void updateMembers(DMSInstance to, List<Resource> resources) throws DMSException {
		List<String> groups = new LinkedList<String>();
		List<String> users = new LinkedList<String>();
		try {
			JSONObject request = new JSONObject();
			for (Resource resource : resources) {
				ResourceType resourceType = resource.getType();
				if (resourceType == ResourceType.USER) {
					users.add(resource.getIdentifier());
				} else if (resourceType == ResourceType.GROUP) {
					groups.add(resource.getIdentifier());
				}
			}
			DMSTypeConverter typeConverter = convertorFactory.getConverter(((Instance) to)
					.getClass());
			JSONObject properties = new JSONObject();
			Pair<String, Serializable> property = typeConverter.convertCMFtoDMSProperty(
					LIST_OF_ALLOWED_USERS, generatePermissionEntriesValue(users, false),
					DMSTypeConverter.PROPERTIES_MAPPING);
			properties.put(property.getFirst(), property.getSecond());
			property = typeConverter.convertCMFtoDMSProperty(LIST_OF_ALLOWED_GROUPS,
					generatePermissionEntriesValue(groups, false),
					DMSTypeConverter.PROPERTIES_MAPPING);
			properties.put(property.getFirst(), property.getSecond());
			request.put(KEY_PROPERTIES, properties);
			request.put(KEY_NODEID, to.getDmsId());
			HttpMethod updateRequest = restClient.createMethod(new PostMethod(),
					request.toString(), true);
			String response = restClient.request(ServiceURIRegistry.CMF_INSTANCE_MEMBERS,
					updateRequest);
			if (response != null) {
				JSONObject result = new JSONObject(response);
				if (result.has(KEY_ROOT_DATA)) {
					JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(
							KEY_DATA_ITEMS);
					if (nodes.length() != 1) {
						throw new DMSException(to.getClass().getSimpleName() + " is not updated");
					}
					return;
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException("Permission update failed! " + e.getMessage(), e);
		} catch (Exception e) {
			throw new DMSException(AlfrescoErrorReader.parse(e), e);
		}
	}
}
