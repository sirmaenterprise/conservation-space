package com.sirma.itt.cmf.alfresco4.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.httpclient.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;

/**
 * Unit tests for UsersAlfresco4Service.
 *
 * @author A. Kunchev
 */
@Test
public class UsersAlfresco4ServiceUTest {

	private static final String USER_NAME = "userName";

	private static final String JOB_TITLE = "jobtitle";

	private static final String EMAIL = "email";

	private static final String FIRST_NAME = "firstName";

	private static final String LAST_NAME = "lastName";

	private static final String ENABLED = "enabled";

	@InjectMocks
	private UsersAlfresco4Service service = new UsersAlfresco4Service();

	@Mock
	private RESTClient restClient;

	@Mock
	private SecurityConfiguration securityConfiguration;

	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	// ---------------------- getFilteredUsers -------------------------------

	public void getFilteredUsers_nullFilterNullAlfrescoResponce_emptyCollection()
			throws DMSException, DMSClientException {
		when(restClient.request(anyString(), any(HttpMethod.class))).thenReturn(null);
		List<User> result = service.getFilteredUsers(null);
		assertEquals(0, result.size());
	}

	public void getFilteredUsers_notNullFilterNullAlfrescoResponce_emptyCollection()
			throws DMSException, DMSClientException {
		when(restClient.request(anyString(), any(HttpMethod.class))).thenReturn(null);
		List<User> result = service.getFilteredUsers("users");
		assertEquals(0, result.size());
	}

	@Test(expectedExceptions = RollbackedRuntimeException.class)
	public void getFilteredUsers_AlfrescoException_RollbackedRuntimeException() throws DMSException, DMSClientException {
		when(restClient.request(anyString(), any(HttpMethod.class))).thenThrow(new DMSClientException("", null, 500));
		service.getFilteredUsers(null);
	}

	public void getFilteredUsers_AlfrescoException_returnUserList() throws DMSException, DMSClientException {
		when(restClient.request(anyString(), any(HttpMethod.class))).thenReturn(preparePeopleResponse());
		when(securityConfiguration.getSystemAdminUsername()).thenReturn("admin");
		List<User> result = service.getFilteredUsers(null);
		assertEquals(4, result.size());
	}

	// ----------------------------- common methods ----------------------------------

	/**
	 * Builds JSON object for the response.
	 *
	 * @return JSON object as string
	 */
	private static String preparePeopleResponse() {
		JSONArray users = new JSONArray();
		users.put(buildUserJson("user1"));
		users.put(buildUserJson("user2"));
		users.put(buildUserJson("user3"));
		JSONObject aang = buildUserJson("userWithAvatar");
		JsonUtil.addToJson(aang, "avatar", "aang");
		users.put(aang);
		users.put(new Object());
		return "{ people: " + users.toString() + "}";
	}

	/**
	 * Builds user as JSON object.
	 *
	 * @param name
	 *            the name of the user
	 * @return user as JSON object
	 */
	private static JSONObject buildUserJson(String name) {
		JSONObject user = new JSONObject();
		JsonUtil.addToJson(user, USER_NAME, name + "@tenantId.com");
		JsonUtil.addToJson(user, ENABLED, true);
		JsonUtil.addToJson(user, FIRST_NAME, name);
		JsonUtil.addToJson(user, LAST_NAME, name);
		JsonUtil.addToJson(user, EMAIL, name + "@tenantId.com");
		JsonUtil.addToJson(user, JOB_TITLE, null);
		return user;
	}

}
