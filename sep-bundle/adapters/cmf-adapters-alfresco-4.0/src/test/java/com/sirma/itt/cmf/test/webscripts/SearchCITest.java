/**
 *
 */
package com.sirma.itt.cmf.test.webscripts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoErrorReader;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.alfresco4.services.DmsInstanceAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.SearchAlfresco4Service;
import com.sirma.itt.cmf.alfresco4.services.TenantAlfresco4Service;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * The searchTest is testing general search scenarios against diff adapters.
 *
 * @author borislav banchev
 */
@Test
public class SearchCITest extends BaseAlfrescoTest implements AlfrescoCommunicationConstants {

	/**
	 * Test list children.
	 */
	@Test(enabled = false)
	public void testAdapter() {
		SearchArguments<FileDescriptor> argss = new SearchArguments<FileDescriptor>();
		argss.setPageSize(10);
		argss.setSkipCount(15);
		argss.setQuery(new Query(CommonProperties.KEY_SEARCHED_ASPECT, new TreeSet<String>(Arrays
				.asList(CaseProperties.TYPE_CASE_INSTANCE))));
		try {
			SearchAlfresco4Service searchAlfresco4Service = new SearchAlfresco4Service();
			DmsInstanceAlfresco4Service value = new DmsInstanceAlfresco4Service();
			setParam(searchAlfresco4Service, "documentAdapterService", value);
			setParam(searchAlfresco4Service, "restClient", httpClient);
			searchAlfresco4Service.search(argss, CaseInstance.class);

		} catch (DMSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Search for containers id dms. Search is both as rest and adapter code.
	 */
	@Test(enabled = true)
	public void searchContainers() {
		JSONObject request = new JSONObject();
		Set<String> results = null;
		try {

			HttpMethod createMethod = httpClient.createMethod(new PostMethod(), request.toString(),
					true);
			String callWebScript = httpClient.request(
					ServiceURIRegistry.CMF_SEARCH_CASE_CONTAINERS, createMethod);
			if (callWebScript == null) {
				Assert.fail("Invalid response!");
			}
			JSONObject result = new JSONObject(callWebScript);
			JSONArray nodes = result.getJSONObject(KEY_ROOT_DATA).getJSONArray(KEY_DATA_ITEMS);
			results = new HashSet<String>(nodes.length());
			for (int i = 0; i < nodes.length(); i++) {
				JSONObject jsonObject = (JSONObject) nodes.get(i);
				// we look for containers only
				String containerId = null;
				if (jsonObject.has(KEY_SITE_ID)) {
					containerId = jsonObject.getString(KEY_SITE_ID);
					results.add(containerId);
				}
			}
		} catch (DMSClientException e) {
			Assert.fail(AlfrescoErrorReader.parse(e));
		} catch (Exception e) {
			Assert.fail(AlfrescoErrorReader.parse(e));
		}
		try {
			TenantAlfresco4Service searchAlfresco4Service = new TenantAlfresco4Service();
			setParam(searchAlfresco4Service, "restClient", httpClient);
			Set<String> cmfContainers = searchAlfresco4Service.getEmfContainers();
			Assert.assertEquals(cmfContainers, results);
		} catch (DMSException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Basic search for users.
	 */
	@Test
	public void testPeopleSearch() {
		try {
			String requested = httpClient.request("/api/people", new GetMethod());
			Assert.assertNotNull(requested, "/api/people script should have result");
			JSONObject people = new JSONObject(requested);
			Assert.assertTrue(people.getJSONArray("people").length() > 0);
		} catch (Exception e) {
			e.printStackTrace();
			org.testng.Assert.fail(e.getMessage());
		}
	}
}
