package com.sirma.itt.cmf.alfresco4.services;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoUtils;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.seip.adapters.remote.AlfrescoErrorReader;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.adapter.CMFGroupService;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;

/**
 * The GroupAlfresco4Service is responsible to service requests to DMS regarding authorities.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class GroupAlfresco4Service implements CMFGroupService, AlfrescoCommunicationConstants {

	private static final String FILTER = "?filter=";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private RESTClient restClient;
	@Inject
	private SecurityConfiguration securityConfiguration;

	@Override
	public List<Group> getAllGroups() {
		return getFilteredGroups(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Group> getFilteredGroups(String filter) {
		String uri = ServiceURIRegistry.GROUPS_SERVICE_URI;
		if (filter != null) {
			try {
				uri += FILTER + URIUtil.encodeWithinQuery(filter, "UTF-8") + "&sortBy=displayName";
			} catch (URIException e) {
				LOGGER.debug("Unsupported encoding", e);
			}
		} else {
			uri += "?sortBy=displayName";
		}
		try {
			return invokeGroupRequest(uri);
		} catch (Exception e) {
			throw new RollbackedRuntimeException("Failed to retrieve groups! " + AlfrescoErrorReader.parse(e), e);
		}
	}

	@Override
	public Group findGroup(String groupId) {
		List<Group> filteredGroups = getFilteredGroups(groupId);
		if (filteredGroups.size() == 1) {
			return filteredGroups.get(0);
		}
		return null;
	}

	@Override
	public List<Group> getAuthorities(Resource user) {
		try {
			String uri = MessageFormat.format(ServiceURIRegistry.GROUPS_FOR_USER, user.getName());
			return invokeGroupRequest(uri);
		} catch (Exception e) {
			throw new RollbackedRuntimeException("Failed to retrieve groups for user! " + AlfrescoErrorReader.parse(e),
					e);
		}
	}

	@Override
	public List<String> getUsersInAuthority(Group group) {
		try {
			String uri = MessageFormat.format(ServiceURIRegistry.GROUPS_MEMBERS_SERVICE_URI,
					group.getName().replaceAll("\\AGROUP_", ""));
			HttpMethod method = restClient.createMethod(new GetMethod(), (String) null, true);
			String response = restClient.request(uri, method);
			return parseUsersInAuthorityResponse(response);
		} catch (DMSClientException e) {
			if (e.getStatusCode() == NOT_FOUND.getStatusCode()) {
				return Collections.emptyList();
			}
			throw new RollbackedRuntimeException(
					"Couldn't retrieve members of group - " + group.getName() + ". " + AlfrescoErrorReader.parse(e), e);
		} catch (Exception e) {
			throw new RollbackedRuntimeException(
					"Failed to retrieve user for group - " + group.getName() + "! " + AlfrescoErrorReader.parse(e), e);
		}
	}

	private List<String> parseUsersInAuthorityResponse(String response) throws JSONException {
		JSONObject jsonObject = new JSONObject(response);
		JSONArray jsonArray = jsonObject.getJSONArray(KEY_ROOT_DATA);
		List<String> result = new ArrayList<>(jsonArray.length());
		String sysAdminName = securityConfiguration.getSystemAdminUsername().toLowerCase();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject authority = jsonArray.getJSONObject(i);
			String type = JsonUtil.getStringValue(authority, "authorityType");
			ResourceType resourceType = ResourceType.getByType(type);
			if (resourceType == ResourceType.GROUP) {
				String groupName = authority.getString("fullName");
				if (isValidGroupName(groupName)) {
					result.add(groupName);
				}
			} else {
				String username = authority.getString("shortName");
				if (AlfrescoUtils.isAuthoritySystemAdmin(sysAdminName, username)) {
					continue;
				}
				result.add(username);
			}
		}
		return result;
	}

	/**
	 * Invoke group request internal.
	 *
	 * @param uri
	 *            the uri to invoke
	 * @return the list of groups
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws DMSClientException
	 *             the dMS client exception
	 * @throws JSONException
	 *             the jSON exception
	 */
	private List<Group> invokeGroupRequest(String uri)
			throws UnsupportedEncodingException, DMSClientException, JSONException {
		HttpMethod method = restClient.createMethod(new GetMethod(), (String) null, true);
		String response = restClient.request(uri, method);
		return parseResponse(response);
	}

	/**
	 * Parses the JSON response to usable objects.
	 *
	 * @param response
	 *            is the
	 * @return the list of found users or empty list
	 */
	private static List<Group> parseResponse(String response) {
		if (response == null) {
			return Collections.emptyList();
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONArray jsonArray = jsonObject.getJSONArray(KEY_ROOT_DATA);
			List<Group> result = new ArrayList<>(jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject groupEntry = jsonArray.getJSONObject(i);
				EmfGroup group = new EmfGroup(groupEntry.getString("fullName"), groupEntry.getString("displayName"));
				if (isValidGroupName(group.getName())) {
					result.add(group);
				}
			}
			return result;
		} catch (JSONException e) {
			LOGGER.error("Error retrieving users", e);
		}

		return Collections.emptyList();
	}

	private static boolean isValidGroupName(String name) {
		return name != null && !name.startsWith("GROUP_site");
	}
}
