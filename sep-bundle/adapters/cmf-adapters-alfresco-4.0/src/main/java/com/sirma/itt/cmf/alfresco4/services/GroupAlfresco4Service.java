package com.sirma.itt.cmf.alfresco4.services;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoErrorReader;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.services.adapter.CMFGroupService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.emf.security.SecurityConfigurationProperties;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.Group;
import com.sirma.itt.emf.security.model.User;

/**
 * The GroupAlfresco4Service is responsible to servce requests to DMS regarding authorities.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class GroupAlfresco4Service implements CMFGroupService, AlfrescoCommunicationConstants {

	/** The sso enabled. */
	@Inject
	@Config(name = SecurityConfigurationProperties.SECURITY_SSO_ENABLED, defaultValue = "false")
	private Boolean ssoEnabled;
	/** The logger. */
	@Inject
	private Logger logger;

	/** The rest client. */
	@Inject
	private RESTClient restClient;

	/** The Constant FILTER. */
	private static final String FILTER = "?filter=";
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6215753468022246000L;

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFGroupService#getAllGroups()
	 */
	@Override
	public List<Group> getAllGroups() throws DMSException {
		return getFilteredGroups(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Group> getFilteredGroups(String filter) throws DMSException {
		String uri = ServiceURIRegistry.GROUPS_SERVICE_URI;
		if (filter != null) {
			try {
				uri += FILTER + URLEncoder.encode(filter, "UTF-8") + "&sortBy=displayName";
			} catch (UnsupportedEncodingException e) {
				logger.debug("Unsupported encoding", e);
			}
		} else {
			uri += "?sortBy=displayName";
		}
		try {
			return invokeGroupRequest(uri);
		} catch (DMSClientException e) {
			throw new DMSException("Group search failed for filter: " + filter, e);
		} catch (Exception e) {
			throw new DMSException("Failed to retrieve groups! " + AlfrescoErrorReader.parse(e), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.services.adapter.CMFGroupService#findGroup(java.lang.String)
	 */
	@Override
	public Group findGroup(String groupId) throws DMSException {
		List<Group> filteredGroups = getFilteredGroups(groupId);
		if (filteredGroups.size() == 1) {
			return filteredGroups.get(0);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.sirma.itt.cmf.services.adapter.CMFGroupService#getAuthorities(com.sirma.itt.emf.security
	 * .model.EmfUser)
	 */
	@Override
	public List<Group> getAuthorities(User user) throws DMSException {
		try {
			String uri = MessageFormat.format(ServiceURIRegistry.GROUPS_FOR_USER,
					URLEncoder.encode(user.getIdentifier(), "UTF-8"));
			return invokeGroupRequest(uri);
		} catch (DMSClientException e) {
			throw new DMSException("Authorities search failed for user: " + user, e);
		} catch (Exception e) {
			throw new DMSException("Failed to retrieve groups for user! " + AlfrescoErrorReader.parse(e), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.sirma.itt.cmf.services.adapter.CMFGroupService#getUsersInAuthority(com.sirma.itt.emf.
	 * security.model.EmfGroup)
	 */
	@Override
	public List<String> getUsersInAuthority(Group group) throws DMSException {
		try {
			String uri = MessageFormat.format(ServiceURIRegistry.GROUPS_MEMBERS_SERVICE_URI, group
					.getIdentifier().replaceAll("\\AGROUP_", ""))
					+ "?authorityType=USER";
			HttpMethod method = restClient.createMethod(new GetMethod(), (String) null, true);
			String response = restClient.request(uri, method);
			JSONObject jsonObject = new JSONObject(response);
			JSONArray jsonArray = jsonObject.getJSONArray(KEY_ROOT_DATA);
			List<String> result = new ArrayList<String>(jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject userEntry = jsonArray.getJSONObject(i);
				result.add(userEntry.getString("shortName"));
			}
			jsonObject = null;
			jsonArray = null;
			return result;
		} catch (DMSClientException e) {
			throw new DMSException("User in group search failed for group: " + group, e);
		} catch (Exception e) {
			throw new DMSException("Failed to retrieve user for group! " + AlfrescoErrorReader.parse(e), e);
		}
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
	private List<Group> invokeGroupRequest(String uri) throws UnsupportedEncodingException,
			DMSClientException, JSONException {
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
	private List<Group> parseResponse(String response) {
		if (response == null) {
			return Collections.emptyList();
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONArray jsonArray = jsonObject.getJSONArray(KEY_ROOT_DATA);
			List<Group> result = new ArrayList<Group>(jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject groupEntry = jsonArray.getJSONObject(i);
				EmfGroup group = new EmfGroup(groupEntry.getString("fullName"),
						groupEntry.getString("displayName"));
				result.add(group);
			}
			return result;
		} catch (JSONException e) {
			logger.error("Error retrieving users", e);
		}

		return Collections.emptyList();
	}
}
