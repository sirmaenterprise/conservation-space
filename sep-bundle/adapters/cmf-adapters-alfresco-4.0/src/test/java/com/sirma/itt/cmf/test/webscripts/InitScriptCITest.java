/**
 *
 */
package com.sirma.itt.cmf.test.webscripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.alfresco4.remote.AlfrescoUploader;
import com.sirma.itt.cmf.beans.LocalFileDescriptor;
import com.sirma.itt.cmf.services.adapter.ThumbnailGenerationMode;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;
import com.sirma.itt.emf.remote.DMSClientException;

/**
 * Tests the init of cmf structures in dms.
 *
 * @author borislav banchev
 */
@Test
public class InitScriptCITest extends BaseAlfrescoTest {

	/** The Constant CASE_XML. */
	private static final String CASE_XML = "src/test/resources/genericCase.xml";
	private static final String EGOV_CASE_XML = "src/test/resources/eGovCase.xml";
	private static final String DOCUMENT_XML = "src/test/resources/documentDefinitions.xml";
	private static final String EAS20001_XML = "src/test/resources/case_EAS20001.xml";
	private static final String BASE_TASKS_XML = "src/test/resources/baseTasks.xml";
	private static final String BASE_WORKFLOW_XML = "src/test/resources/baseWorkflowDefinition.xml";
	private static final String RANDOM_WORKFLOW_XML = "src/test/resources/randomProcessWorkflowDefinition.xml";
	private static final String EAS20001_WORKFLOW_XML = "src/test/resources/EAS20001_wfDef.xml";
	private static final String THUMB_MODE_GENERATION = ThumbnailGenerationMode.ASYNCH
			.toString();

	/**
	 * Test init.
	 */
	@Test(enabled = false)
	public void testInit() {
		JSONObject request = new JSONObject();
		try {
			String siteId = "cmf";
			request.put("sites", siteId);
			JSONObject properties = new JSONObject();
			// def config
			JSONObject caseDefinition = new JSONObject();
			properties.put("cm:name", "Case Definitions");
			properties.put("cm:title", "Stored CMF definitions");
			properties.put("cm:description", "Stored CMF definitions");
			caseDefinition.put("properties", properties);
			caseDefinition.put("lock", Boolean.TRUE);
			request.put("case", caseDefinition);

			JSONObject document = new JSONObject();
			properties = new JSONObject();
			properties.put("cm:name", "Document Definitions");
			properties.put("cm:title", "Stored CMF document definitions");
			properties.put("cm:description", "Stored CMF document definitions");
			document.put("properties", properties);
			document.put("lock", Boolean.TRUE);
			request.put("document", document);

			JSONObject instances = new JSONObject();
			properties = new JSONObject();
			properties.put("cm:name", "Case Instances");
			properties.put("cm:title", "Stored CMF cases");
			properties.put("cm:description", "Stored CMF cases");
			instances.put("properties", properties);
			instances.put("lock", Boolean.TRUE);
			request.put("instance", instances);

			JSONObject workflow = new JSONObject();
			properties = new JSONObject();
			properties.put("cm:name", "Workflow Definitions");
			properties.put("cm:title", "Stored CMF workfklow definitions");
			properties
					.put("cm:description", "Stored CMF workfklow definitions");
			workflow.put("properties", properties);
			workflow.put("lock", Boolean.TRUE);
			request.put("workflow", workflow);

			JSONObject task = new JSONObject();
			properties = new JSONObject();
			properties.put("cm:name", "Tasks Definitions");
			properties.put("cm:title", "Stored CMF workfklow task definitions");
			properties.put("cm:description",
					"Stored CMF workfklow task definitions");
			task.put("properties", properties);
			task.put("lock", Boolean.TRUE);
			request.put("task", task);

			HttpMethod createMethod = createMethod(request.toString(),
					new PostMethod());
			String callWebScript = httpClient
					.request("/cmf/init", createMethod);

			Assert.assertNotNull(callWebScript, "Result should be json object");
			// // upload the generic case
			AlfrescoUploader alfrescoUploader = mockupProvider.mockupAlfrescoUploader();

			Set<String> aspectsProp = new HashSet<String>();
			Object uploadFile = null;
			Map<String, Serializable> props = new HashMap<String, Serializable>();

			uploadCaseDef(CASE_XML, alfrescoUploader, siteId);
			uploadCaseDef(EGOV_CASE_XML, alfrescoUploader, siteId);
			uploadCaseDef(EAS20001_XML, alfrescoUploader, siteId);

			aspectsProp = new HashSet<String>();
			aspectsProp.add("cmf:documentDefinition");
			props = new HashMap<String, Serializable>();
			props.put("cm:title", "The document definitions");
			uploadFile = alfrescoUploader.uploadFile(new LocalFileDescriptor(
					new File(DOCUMENT_XML)), siteId, "Case Definitions", null,
					"cm:content", props, aspectsProp, THUMB_MODE_GENERATION);
			Assert.assertNotNull(uploadFile, "Result should be json object");

			aspectsProp = new HashSet<String>();
			aspectsProp.add("cmf:tasksDefinition");
			props = new HashMap<String, Serializable>();
			props.put("cm:title", "The tasks definitions");
			uploadFile = alfrescoUploader.uploadFile(new LocalFileDescriptor(
					new File(BASE_TASKS_XML)), siteId, "Tasks Definitions",
					null, "cm:content", props, aspectsProp,
					THUMB_MODE_GENERATION);
			Assert.assertNotNull(uploadFile, "Result should be json object");

			uploadWorkflowDef(BASE_WORKFLOW_XML, alfrescoUploader, siteId);

			uploadWorkflowDef(RANDOM_WORKFLOW_XML, alfrescoUploader, siteId);

			uploadWorkflowDef(EAS20001_WORKFLOW_XML, alfrescoUploader, siteId);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Upload workflow definition.
	 *
	 * @param def
	 *            the def
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param siteId
	 *            the site id
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws DMSClientException
	 */
	private void uploadWorkflowDef(String def,
			AlfrescoUploader alfrescoUploader, String siteId)
			throws FileNotFoundException, DMSClientException {
		HashSet<String> aspectsProp = new HashSet<String>();
		aspectsProp.add("cmf:workflowDefinition");
		HashMap<String, Serializable> props = new HashMap<String, Serializable>();
		props.put("cm:title", "The workflow definitions");
		String uploadFile = alfrescoUploader.uploadFile(
				new LocalFileDescriptor(new File(def)), siteId,
				"Workflow Definitions", null, "cm:content", props, aspectsProp,
				THUMB_MODE_GENERATION);

		Assert.assertNotNull(uploadFile, "Result should be json object");
	}

	/**
	 * Upload case def.
	 *
	 * @param def
	 *            the def
	 * @param alfrescoUploader
	 *            the alfresco uploader
	 * @param siteId
	 *            the site id
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws DMSClientException
	 */
	private void uploadCaseDef(String def, AlfrescoUploader alfrescoUploader,
			String siteId) throws FileNotFoundException, DMSClientException {
		HashSet<String> aspectsProp = new HashSet<String>();
		HashMap<String, Serializable> props = new HashMap<String, Serializable>();
		aspectsProp.add("cmf:caseDefinition");
		props.put("cm:title", "Case definition");
		String uploadFile = alfrescoUploader.uploadFile(
				new LocalFileDescriptor(new File(def)), siteId,
				"Case Definitions", null, "cm:content", props, aspectsProp,
				THUMB_MODE_GENERATION);
		Assert.assertNotNull(uploadFile, "Result should be json object");
	}
}
