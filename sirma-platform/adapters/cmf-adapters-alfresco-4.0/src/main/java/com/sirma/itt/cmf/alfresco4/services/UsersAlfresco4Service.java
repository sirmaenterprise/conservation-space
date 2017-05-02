package com.sirma.itt.cmf.alfresco4.services;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
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
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.adapter.CMFUserService;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;

/**
 * The Class UsersAlfresco4Service.
 *
 * @author BBonev
 */
@ApplicationScoped
public class UsersAlfresco4Service implements CMFUserService, AlfrescoCommunicationConstants {

	private static final String FILTER = "?filter=";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Inject
	private RESTClient restClient;

	@Override
	public List<User> getAllUsers() {
		return getFilteredUsers(null);
	}

	@Override
	public List<User> getFilteredUsers(String filter) {
		String uri = ServiceURIRegistry.PEOPLE_SERVICE_URI;
		if (filter != null) {
			try {
				uri += FILTER + URLEncoder.encode(filter, UTF_8);
			} catch (UnsupportedEncodingException e) {
				LOGGER.debug("Unsupported encoding", e);
			}
		}

		try {
			HttpMethod method = restClient.createMethod(new GetMethod(), (String) null, true);
			String response = restClient.request(uri, method);
			return parseResponse(response);
		} catch (Exception e) {
			throw new RollbackedRuntimeException("Failed to retrieve users: " + AlfrescoErrorReader.parse(e), e);
		}
	}

	/**
	 * Parses the JSON response to usable objects.
	 *
	 * @param response
	 *            is the
	 * @return the list of found users or empty list
	 */
	private List<User> parseResponse(String response) {
		JSONObject jsonObject = JsonUtil.createObjectFromString(response);
		if (jsonObject == null) {
			return Collections.emptyList();
		}
		JSONArray jsonArray = JsonUtil.getJsonArray(jsonObject, "people");
		String sysAdminName = securityConfiguration.getSystemAdminUsername().toLowerCase();
		List<User> result = new ArrayList<>(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject personElement = jsonArray.optJSONObject(i);
			String userName = extactUserName(sysAdminName, personElement);
			if (userName == null) {
				continue;
			}
			EmfUser person = new EmfUser(userName);
			person.add(ResourceProperties.FIRST_NAME,
					JsonUtil.getStringValue(personElement, ResourceProperties.FIRST_NAME));
			person.add(ResourceProperties.LAST_NAME,
					JsonUtil.getStringValue(personElement, ResourceProperties.LAST_NAME));
			person.add(ResourceProperties.EMAIL,
					JsonUtil.getStringValue(personElement, ResourceProperties.EMAIL));
			person.add(ResourceProperties.USER_ID, userName);
			if (personElement.has(ResourceProperties.AVATAR)) {
				person.add(ResourceProperties.AVATAR,
						JsonUtil.getStringValue(personElement, ResourceProperties.AVATAR));
			}
			person.add(ResourceProperties.JOB_TITLE,
					JsonUtil.getStringValue(personElement, ResourceProperties.JOB_TITLE));
			result.add(person);
		}
		// result.add(new EmfUser("testUser"));
		return result;
	}

	private static String extactUserName(String sysAdminName, JSONObject personElement) {
		if (personElement == null) {
			// should not happen but just in case
			return null;
		}

		String userName = JsonUtil.getStringValue(personElement, AlfrescoCommunicationConstants.KEY_USER_NAME);
		if (AlfrescoUtils.isAuthoritySystemAdmin(sysAdminName, userName)) {
			return null;
		}
		return userName;
	}

	@Override
	public String getUserRole(String userId, String siteId) {
		String uri = ServiceURIRegistry.SITE_MEMBERSHIP;
		try {
			uri = MessageFormat.format(uri, URLEncoder.encode(siteId, UTF_8), URLEncoder.encode(userId, UTF_8));
		} catch (UnsupportedEncodingException e) {
			LOGGER.debug("Unsupported encoding", e);
		}

		try {
			HttpMethod method = restClient.createMethod(new GetMethod(), (String) null, true);
			String response = restClient.request(uri, method);
			if (response == null) {
				return null;
			}
			JSONObject object = new JSONObject(response);
			if (object.has("role")) {
				String role = object.getString("role");
				if (role.startsWith("Site")) {
					return role.substring(4);
				}
				return role;
			}
			return null;
		} catch (DMSClientException e) {
			if (e.getStatusCode() == 404) {
				LOGGER.debug(e.getMessage());
				return null;
			}
			throw new RollbackedRuntimeException(e.getMessage(), e);
		} catch (Exception e) {
			throw new RollbackedRuntimeException("Failed to retrieve users: " + AlfrescoErrorReader.parse(e));
		}
	}

	@Override
	public User findUser(String userId) {
		try {
			String uri = MessageFormat.format(ServiceURIRegistry.AUTH_USER_SYNCHRONIZATION, userId);

			HttpMethod method = restClient.createMethod(new GetMethod(), (String) null, true);
			String response = restClient.request(uri, method);
			if (response != null) {
				List<User> parseResponse = parseResponse(response);
				if (parseResponse.size() == 1 && userId.equals(parseResponse.get(0).getName())) {
					return parseResponse.get(0);
				}
			}
		} catch (DMSClientException e) {
			throw new RollbackedRuntimeException(e.getMessage(), e);
		} catch (Exception e) {
			throw new RollbackedRuntimeException("Failed to retrieve users: " + AlfrescoErrorReader.parse(e));
		}
		return null;
	}
}
