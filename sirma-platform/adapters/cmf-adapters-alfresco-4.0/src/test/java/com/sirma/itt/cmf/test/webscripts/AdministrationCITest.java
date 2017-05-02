package com.sirma.itt.cmf.test.webscripts;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.test.BaseAlfrescoTest;

/**
 * The Class AdministrationTest tests deletion of case instances. Use with <strong>Caution!!!</strong>
 */
public class AdministrationCITest extends BaseAlfrescoTest {

	/**
	 * Test by id.
	 */
	@Test(enabled = false)
	public void testById() {
		JSONObject request = new JSONObject();
		try {
			request.put("node", "workspace://SpacesStore/6198dcdb-92c1-45cf-b809-ea4ad60073ef");
			request.put("all", Boolean.FALSE);

			HttpMethod createMethod = createMethod(request.toString(), new PostMethod());
			String callWebScript = httpClient.request("case/instance/obsolete/delete", createMethod);

			Assert.assertNotNull(callWebScript, "Result should be json object");

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Test by site.
	 */
	@Test(enabled = false)
	public void testBySite() {
		JSONObject request = new JSONObject();
		try {
			String siteId = "cmf";
			request.put("sites", siteId);
			request.put("all", Boolean.TRUE);

			HttpMethod createMethod = createMethod(request.toString(), new PostMethod());
			String callWebScript = httpClient.request("case/instance/obsolete/delete", createMethod);

			Assert.assertNotNull(callWebScript, "Result should be json object");

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getLocalizedMessage());
		}
	}
}
