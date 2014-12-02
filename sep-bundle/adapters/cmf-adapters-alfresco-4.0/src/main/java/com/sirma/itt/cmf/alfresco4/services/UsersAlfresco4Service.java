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
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoErrorReader;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.services.adapter.CMFUserService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.security.SecurityConfigurationProperties;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * The Class UsersAlfresco4Service.
 *
 * @author BBonev
 */
@ApplicationScoped
public class UsersAlfresco4Service implements CMFUserService, AlfrescoCommunicationConstants {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6215753468022246000L;
	/** The sso enabled. */
	@Inject
	@Config(name = SecurityConfigurationProperties.SECURITY_SSO_ENABLED, defaultValue = "false")
	private Boolean ssoEnabled;
	/** The config admin user. */
	@Inject
	@Config(name = EmfConfigurationProperties.ADMIN_USERNAME)
	private String configAdminUser;
	/** The logger. */
	@Inject
	private Logger logger;

	/** The rest client. */
	@Inject
	private RESTClient restClient;

	/** The Constant FILTER. */
	private static final String FILTER = "?filter=";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<User> getAllUsers() throws DMSException {
		if (ssoEnabled) {
			return getFilteredUsers(null);
		}
		return getFilteredUsers(configAdminUser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<User> getFilteredUsers(String filter) throws DMSException {
		String uri = ServiceURIRegistry.PEOPLE_SERVICE_URI;
		if (filter != null) {
			try {
				uri += FILTER + URLEncoder.encode(filter, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.debug("Unsupported encoding", e);
			}
		}

		try {
			HttpMethod method = restClient.createMethod(new GetMethod(), (String) null, true);
			String response = restClient.request(uri, method);
			return parseResponse(response);
		} catch (Exception e) {
			throw new DMSException("Failed to retrieve users: " + AlfrescoErrorReader.parse(e),e);
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
		if (response == null) {
			return Collections.emptyList();
		}
		JSONObject jsonObject = JsonUtil.createObjectFromString(response);
		if (jsonObject == null) {
			return Collections.emptyList();
		}
		JSONArray jsonArray = JsonUtil.getJsonArray(jsonObject, "people");
		List<User> result = new ArrayList<User>(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject personElement = jsonArray.optJSONObject(i);
			if (personElement == null) {
				// should not happen but just in case
				continue;
			}
			String userName = JsonUtil.getStringValue(personElement, "userName");
			EmfUser person = new EmfUser(userName);
			person.getProperties().put(ResourceProperties.FIRST_NAME,
					JsonUtil.getStringValue(personElement, "firstName"));
			person.getProperties().put(ResourceProperties.LAST_NAME,
					JsonUtil.getStringValue(personElement, "lastName"));
			person.getProperties().put(ResourceProperties.EMAIL,
					JsonUtil.getStringValue(personElement, ResourceProperties.EMAIL));
			person.getProperties().put(ResourceProperties.USER_ID, userName);
			if (personElement.has(ResourceProperties.AVATAR)) {
				person.getProperties().put(ResourceProperties.AVATAR,
						JsonUtil.getStringValue(personElement, ResourceProperties.AVATAR));
			}
			person.getProperties().put(ResourceProperties.JOB_TITLE,
					JsonUtil.getStringValue(personElement, ResourceProperties.JOB_TITLE));
			result.add(person);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserRole(String userId, String siteId) throws DMSException {
		String uri = ServiceURIRegistry.SITE_MEMBERSHIP;
		try {
			uri = MessageFormat.format(uri, URLEncoder.encode(siteId, "UTF-8"),
					URLEncoder.encode(userId, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.debug("Unsupported encoding", e);
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
				logger.debug(e.getMessage());
				return null;
			}
			throw new DMSException(e.getMessage(), e);
		} catch (Exception e) {
			throw new DMSException("Failed to retrieve users: " + AlfrescoErrorReader.parse(e));
		}
	}

	@Override
	public User findUser(String userId) throws DMSException {
		try {
			String uri = MessageFormat.format(ServiceURIRegistry.AUTH_USER_SYNCHRONIZATION, userId);

			HttpMethod method = restClient.createMethod(new GetMethod(), (String) null, true);
			String response = restClient.request(uri, method);
			if (response != null) {
				List<User> parseResponse = parseResponse(response);
				if ((parseResponse.size() == 1)
						&& userId.equals(parseResponse.get(0).getIdentifier())) {
					return parseResponse.get(0);
				}
			}
		} catch (DMSClientException e) {
			throw new DMSException(e.getMessage(), e);
		} catch (Exception e) {
			throw new DMSException("Failed to retrieve users: " + AlfrescoErrorReader.parse(e));
		}
		throw new DMSException("Missing user: " + userId);
	}

}
