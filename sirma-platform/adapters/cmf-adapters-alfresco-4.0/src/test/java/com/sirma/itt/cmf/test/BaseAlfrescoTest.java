/**
 *
 */
package com.sirma.itt.cmf.test;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.test.mock.MockupProvider;
import com.sirma.itt.seip.adapters.remote.AlfrescoRESTClient;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;

/**
 * Base remote client test for alfresco. All common methods should be placed here
 *
 * @author borislav banchev
 */
public class BaseAlfrescoTest implements AlfrescoCommunicationConstants {

	/** The http client. */
	protected RESTClient httpClient;

	/** The Constant LOGGER. */
	protected Logger logger = Logger.getLogger(getClass());

	/** The user name. */
	protected String userName = null;

	/** The host. */
	protected String host = null;

	/** The port. */
	protected int port;

	protected String containerId = "cmf";
	/** The upload dir '/app:company_home/app:user_homes/...'. */
	protected String baseUploadPath = "/app:company_home/app:";

	/** The mockup provider. */
	protected MockupProvider mockupProvider;

	protected Set<String> cleanUpDMSIds = new HashSet<>();

	protected Set<String> cleanUpGlobalIds = new HashSet<>();

	protected String tenant;

	/**
	 * Sets the tests. Use <code>-Dtest.host=, -Dtest.port=,-Dtest.user=</code> to setup args <code></code>
	 *
	 * @param host
	 *            is the tested host
	 * @param port
	 *            is the tested socket port
	 * @param userName
	 *            is the user to authenticate
	 */
	@BeforeClass
	@Parameters(value = { "test.host", "test.port", "test.user" })
	public void setUp(String host, String port, String userName) {
		System.out.println(getClass().getSimpleName() + ".setUp() with params: " + host + "_" + port + "@" + userName);
		this.userName = userName;
		tenant = userName.indexOf('@') > 1 ? "@" + userName.split("@")[1] : "";
		this.host = host;
		this.port = Integer.parseInt(port);
		baseUploadPath = baseUploadPath + "guest_home";
		setUp();
	}

	/**
	 * Cleans up the created nodes filled in the collection {@link #cleanUpDMSIds}
	 */
	@AfterMethod
	public void cleanUp() {
		for (String nodeId : cleanUpDMSIds) {
			deleteNode(nodeId, true);
		}
		cleanUpDMSIds.clear();
	}

	/**
	 * On complete test.
	 */
	@AfterClass
	public void tearDown() {
		for (String nodeId : cleanUpGlobalIds) {
			deleteNode(nodeId, true);
		}
		cleanUpGlobalIds.clear();
	}

	/**
	 * Creates the mockup provider.
	 *
	 * @return the mockup provider
	 */
	protected MockupProvider createMockupProvider() {
		return new MockupProvider(userName, host, port);
	}

	/**
	 * Setups completition.
	 */
	protected void setUp() {

		mockupProvider = createMockupProvider();
		httpClient = mockupProvider.getHttpClient();
	}

	/**
	 * Gets the json string value from json string input.
	 *
	 * @param value
	 *            the json string
	 * @param key
	 *            the key
	 * @return the result
	 * @throws JSONException
	 *             the jSON exception
	 */
	protected String getJsonString(String value, String key) throws JSONException {
		JSONObject object = new JSONObject(value);
		if (object.has(key)) {
			return object.getString(key);
		}
		return null;
	}

	/**
	 * Set by reflection an object value.
	 *
	 * @param object
	 *            is the object
	 * @param field
	 *            is the field
	 * @param value
	 *            is the new value
	 * @return true on success
	 */
	protected boolean setParam(Object object, String field, Object value) {
		try {
			Field declaredField = object.getClass().getDeclaredField(field);
			declaredField.setAccessible(true);
			declaredField.set(object, value);
			declaredField.setAccessible(false);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Shortcut to current time.
	 *
	 * @return the long
	 */
	protected long currentTime() {
		return System.currentTimeMillis();
	}

	/**
	 * Creates the http method with given content
	 *
	 * @param content
	 *            the content
	 * @param method
	 *            the method
	 * @return the http method
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	protected HttpMethod createMethod(String content, HttpMethod method) throws UnsupportedEncodingException {

		method.setParams(new HttpMethodParams());
		if (method instanceof EntityEnclosingMethod) {
			EntityEnclosingMethod enclosingMethod = (EntityEnclosingMethod) method;
			enclosingMethod.setRequestEntity(new StringRequestEntity(content,
					AlfrescoRESTClient.MIME_TYPE_APPLICATION_JSON, AlfrescoRESTClient.UTF_8));
		}
		String useCompression = "false"; // Config.getInstance().getPropoperty(
		// Config.USE_COMPRESSION, "false");
		if (Boolean.valueOf(useCompression)) {
			method.addRequestHeader(AlfrescoRESTClient.PROP_CONTENT_ENCODING,
					AlfrescoRESTClient.COMPRESSION_FORMAT_GZIP);
			method.addRequestHeader(AlfrescoRESTClient.PROP_ACCEPT_ENCODING,
					AlfrescoRESTClient.COMPRESSION_FORMAT_GZIP);
		}

		return method;
	}

	/**
	 * Gets the user home.
	 *
	 * @param user
	 *            the user
	 * @return the user home
	 * @throws JSONException
	 *             the jSON exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws DMSClientException
	 *             the dMS client exception
	 */
	protected String getUserHome(String user) throws JSONException, UnsupportedEncodingException, DMSClientException {
		JSONObject request = new JSONObject();
		request.put("query", "PATH:\"/app:company_home/app:guest_home\"");
		request.put("paging", getPaging());
		HttpMethod createMethod = createMethod(request.toString(), new PostMethod());

		String callWebScript = httpClient.request(ServiceURIRegistry.CMF_SEARCH_SERVICE, createMethod);

		Assert.assertNotNull(callWebScript);
		JSONObject result = new JSONObject(callWebScript);
		return result.getJSONObject("data").getJSONArray("items").getJSONObject(0).getString("nodeRef");
	}

	/**
	 * Gets the paging.
	 *
	 * @return the paging
	 */
	protected JSONObject getPaging() {
		JSONObject paging = new JSONObject();
		try {
			paging.put(KEY_PAGING_TOTAL, 0);
			paging.put(KEY_PAGING_SIZE, 1000);
			paging.put(KEY_PAGING_SKIP, 0);
			paging.put(KEY_PAGING_MAX, 100);
		} catch (Exception e) {
		}
		return paging;
	}

	/**
	 * Gets the test parent node by path. Could return empty id if path is not known.
	 *
	 * @param xPath
	 *            is the path as {@link BaseAlfrescoTest#baseUploadPath}
	 * @return the test parent node id
	 * @throws JSONException
	 *             the jSON exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 * @throws DMSClientException
	 *             the dMS client exception
	 */
	protected String getTestParentId(String xPath)
			throws JSONException, UnsupportedEncodingException, DMSClientException {
		JSONObject request = new JSONObject();
		request.put("query", "PATH:\"" + xPath + "\"");
		request.put("paging", getPaging());
		HttpMethod createMethod = createMethod(request.toString(), new PostMethod());

		String callWebScript = httpClient.request("/cmf/search", createMethod);

		assertNotNull(callWebScript, "Searching failed!");
		JSONObject result = new JSONObject(callWebScript);
		return result.getJSONObject(KEY_ROOT_DATA).getJSONArray(KEY_DATA_ITEMS).getJSONObject(0).getString(KEY_NODEREF);
	}

	/**
	 * Creates the test folder.
	 *
	 * @param xPath
	 *            the x path
	 * @return the node ref of folder or fails with assert
	 */
	protected String createTestFolder(String xPath) {
		try {
			String testParentId = getTestParentId(xPath);
			JSONObject request = new JSONObject();
			request.put(AlfrescoCommunicationConstants.KEY_NODEID, testParentId);
			JSONObject props = new JSONObject();
			props.put("emf:identifier", new Long(System.currentTimeMillis()).toString());
			props.put("cm:description", "For test purpose! Created on " + new Date().toString());
			props.put("cm:title", "For test purpose!");
			request.put(AlfrescoCommunicationConstants.KEY_PROPERTIES, props);
			HttpMethod createMethod = createMethod(request.toString(), new PostMethod());

			String callWebScript = httpClient.request(ServiceURIRegistry.FOLDER_CREATE, createMethod);

			Assert.assertNotNull(callWebScript);
			JSONObject result = new JSONObject(callWebScript);
			return result
					.getJSONObject(KEY_ROOT_DATA)
						.getJSONArray(KEY_DATA_ITEMS)
						.getJSONObject(0)
						.getString(KEY_NODEREF);
		} catch (Exception e) {
			fail(e);
			return null;
		}
	}

	/**
	 * Delete node forcibly or not as system user.
	 *
	 * @param nodeId
	 *            the node id
	 * @param force
	 *            is it force deletion
	 */
	protected void deleteNode(String nodeId, boolean force) {
		try {
			if (nodeId == null) {
				return;
			}
			JSONObject request = new JSONObject();
			request.put("node", nodeId);
			request.put("all", Boolean.valueOf(force));

			HttpMethod createMethod = createMethod(request.toString(), new PostMethod());
			String requested = httpClient.request("case/instance/obsolete/delete", createMethod);

			assertNotNull(requested, "Node should be deleted: " + nodeId);

		} catch (Exception e) {
			// TODO fail or not?
			e.printStackTrace();
		}
	}

	/**
	 * Assert not null.
	 *
	 * @param testable
	 *            the testable
	 * @param explain
	 *            the explain
	 */
	protected void assertNotNull(Object testable, String explain) {
		Assert.assertNotNull(testable, explain);
	}

	/**
	 * Assert equality.
	 *
	 * @param actual
	 *            the actual value
	 * @param expected
	 *            the expected value
	 * @param message
	 *            the message for error
	 */
	protected void assertEquals(Serializable actual, Serializable expected, String message) {
		Assert.assertEquals(actual, expected, message);
	}

	/**
	 * Assert true for boolean clause
	 *
	 * @param testable
	 *            the testable expression
	 * @param explain
	 *            the explain
	 */
	protected void assertTrue(boolean testable, String explain) {
		Assert.assertTrue(testable, explain);
	}

	/**
	 * Fail.
	 *
	 * @param e
	 *            the e
	 */
	protected void fail(Exception e) {
		Assert.fail(e.getMessage(), e);
	}

}
