package com.sirma.itt.cmf.test.webscripts;

import java.util.Arrays;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;

/**
 * Tests update of permission for documents script.
 */
@Test
public class PermissionServiceAdapterCITest extends BaseAlfrescoTest {
	/*
	 * (non-Javadoc)
	 * @see com.sirma.itt.cmf.test.BaseAlfrescoTest#setUp()
	 */
	@Override
	@BeforeClass
	public void setUp() {
		super.setUp();
	}

	/**
	 * Tests the update process for given document.
	 */
	@Test(enabled = false)
	public void testUpdate() {
		JSONObject request = new JSONObject();
		try {
			request.put(AlfrescoCommunicationConstants.KEY_NODEID,
					"workspace://SpacesStore/8f47e2c9-b4f6-4ea3-82c1-a3dd4c3c24dc");
			JSONObject props = new JSONObject();
			// the allowed users
			props.put("emf:allowedUsers", Arrays.asList(new String[] { "admin", "bbanchev" }));
			request.put(AlfrescoCommunicationConstants.KEY_PROPERTIES, props);
			// the copyable params
			request.put("copyable", "emf:type,cmf:servicedInMunicipality");
			HttpMethod post = createMethod(request.toString(), new PostMethod());
			String callWebScript = httpClient.request(ServiceURIRegistry.CMF_PERMISSIONS_FIX, post);

			Assert.assertNotNull(callWebScript, "Result should be json object");

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getLocalizedMessage());
		}
	}
}
