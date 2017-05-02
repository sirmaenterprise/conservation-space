package com.sirma.itt.cmf.test.webscripts;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;

/**
 * The WorkflowScriptCITest is responsible to test the workflow request transitions.
 */
public class WorkflowScriptCITest extends BaseAlfrescoTest {

	/**
	 * Test start parallel workflow and the initial transitions
	 */
	@Test(enabled = true)
	public void testStartParallelWorkflow() {
		String testFolder = createTestFolder(baseUploadPath);
		cleanUpDMSIds.add(testFolder);
		String nowDateAsLong = new Long(new Date().getTime()).toString();
		try {
			String httpRequest = "{\"workflowId\":\"activiti$WFTYPEPT01\",\"nextStateProperties\":{\"emf:status\":\"APPROVED\",\"emf:containerId\":\""
					+ containerId + "\"},\"referenceId\":\"" + testFolder
					+ "\",\"currentStateProperties\":{\"emf:status\":\"COMPLETED\"},\"properties\":{\"cmfwf:startedBy\":\""
					+ userName
					+ "\",\"bpm:workflowDescription\":\"test parallel\",\"bpm:workflowPriority\":\"1\",\"cmfwf:revision\":\"5\",\"emf:identifier\":\""
					+ UUID.randomUUID().toString() + "\",\"emf:status\":\"COMPLETED\",\"cm:owner\":\"" + userName
					+ "\",\"bpm:workflowDueDate\":" + nowDateAsLong
					+ ",\"emf:type\":\"TSTYPE97\",\"cm:description\":\"WFTYPEPT01\",\"bpm:assignee\":\"" + userName
					+ "\",\"cmfwf:modified\":" + nowDateAsLong + ",\"cmfwf:modifiedBy\":\"" + userName
					+ "\",\"cmfwf:contextType\":\"Folder\",\"cmfwf:title\":\"TSTYPE97\",\"cmfwf:container\":\""
					+ containerId + "\"}}";

			HttpMethod createMethod = httpClient.createMethod(new PostMethod(), httpRequest, true);
			String requestResult = httpClient.request(ServiceURIRegistry.CMF_WORKFLOW_START, createMethod);
			assertNotNull(requestResult, "Workflow should be started");
			JSONArray tasks = new JSONObject(requestResult).getJSONObject("tasks").getJSONArray("data");
			assertTrue(tasks.length() == 1, "Only one task after start");

			String taskId = tasks.getJSONObject(0).getString("id");

			String transition = "{\"taskId\":\"" + taskId
					+ "\",\"transitionId\":\"RT0097\",\"nextStateProperties\":{\"emf:status\":\"APPROVED\",\"emf:containerId\":\""
					+ containerId + "\"},\"properties\":{\"cmfwf:startedBy\":\"" + userName
					+ "\",\"cmfwf:taskOutcome\":\"RT0097\",\"bpm:description\":\"WFTYPEPT01\",\"bpm:assigneeA\":\"banchev\",\"emf:identifier\":\""
					+ UUID.randomUUID().toString()
					+ "\",\"emf:status\":\"COMPLETED\",\"bpm:comment\":\"go to parallel\",\"cm:owner\":\"" + userName
					+ "\",\"emf:type\":\"PT0001\",\"cmfwf:modified\":" + nowDateAsLong + ",\"cmfwf:modifiedBy\":\""
					+ userName + "\",\"bpm:assigneesB\":\"automatron,automatron1,Consumer\",\"cmfwf:priority\":\"1\"}}";
			createMethod = httpClient.createMethod(new PostMethod(), transition, true);
			requestResult = httpClient.request(ServiceURIRegistry.CMF_WORKFLOW_TRANSITION, createMethod);
			tasks = new JSONObject(requestResult).getJSONArray("data");
			assertTrue(tasks.length() == 4, "3 multi instance tasks and 1 single task after transition");
			String managerTaskId = null;
			for (int i = 0; i < 4; i++) {
				JSONObject task = tasks.getJSONObject(i);
				checkOwner(task.getJSONObject(AlfrescoCommunicationConstants.KEY_PROPERTIES).getString("cm_owner"),
						"automatron" + tenant, "automatron1" + tenant, "Consumer" + tenant, "banchev" + tenant);
				if ("PT0002".equals(task.getString("definitionId"))) {
					managerTaskId = task.getString("id");
				}
			}
			assertNotNull(managerTaskId, "Manager task id is not found");

			transition = "{\"taskId\":\"" + managerTaskId
					+ "\",\"transitionId\":\"TR0002\",\"nextStateProperties\":{\"emf:status\":\"APPROVED\",\"emf:containerId\":\""
					+ containerId + "\"},\"properties\":{\"cmfwf:startedBy\":\"" + userName + "\",\"cmfwf:modified\":"
					+ nowDateAsLong
					+ ",\"cmfwf:taskOutcome\":\"TR0002\",\"bpm:description\":\"WFTYPEPT01\",\"cmfwf:modifiedBy\":\""
					+ userName + "\",\"emf:status\":\"COMPLETED\",\"emf:identifier\":\"" + UUID.randomUUID().toString()
					+ "\",\"cm:owner\":\"banchev" + tenant + "\",\"cmfwf:priority\":\"1\",\"emf:type\":\"PT0002\"}}";
			createMethod = httpClient.createMethod(new PostMethod(), transition, true);
			requestResult = httpClient.request(ServiceURIRegistry.CMF_WORKFLOW_TRANSITION, createMethod);
			tasks = new JSONObject(requestResult).getJSONArray("data");
			assertTrue(tasks.length() == 3, "3 multi instance tasks after transition");
			for (int i = 0; i < 3; i++) {
				JSONObject task = tasks.getJSONObject(i);
				checkOwner(task.getJSONObject(AlfrescoCommunicationConstants.KEY_PROPERTIES).getString("cm_owner"),
						"automatron" + tenant, "automatron1" + tenant, "consumer" + tenant);
			}
		} catch (Exception e) {
			fail(e);
		}
	}

	/**
	 * Check if owner of current task is part of expected users.
	 *
	 * @param username
	 *            the username
	 * @param possibleOwner
	 *            list of users to check in
	 * @return true, if successful, fail otherwise
	 */
	private boolean checkOwner(String username, String... possibleOwner) {
		for (String nextUser : possibleOwner) {
			if (username.equals(nextUser)) {
				return true;
			}
		}
		Assert.fail("Expected task owner not found: " + username);
		return false;
	}
}
