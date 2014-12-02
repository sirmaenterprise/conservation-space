/**
 *
 */
package com.sirma.itt.cmf.test.webscripts;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoUtils;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.time.ISO8601DateFormat;

/**
 * The standalone task operations test.
 *
 * @author borislav banchev
 */
public class StandaloneTaskCITests extends BaseAlfrescoTest implements
		AlfrescoCommunicationConstants {
	/**
	 * Test pool task support.
	 */
	@Test(enabled = true)
	public void testHTTPStartPooledCycle() {
		String taskId1 = null;
		String taskId2 = null;
		String taskId3 = null;
		try {
			String ownerKey = "cm_owner";
			// ########### case 1 ################
			ArrayList<String> assignees = new ArrayList<String>();
			assignees.add("bbanchev");
			assignees.add("admin");
			Pair<String, Map<String, Serializable>> assigneesTask = testStartPoolTaskByRequest(
					"bpm:assignees", assignees);
			taskId1 = assigneesTask.getFirst();
			Assert.assertNotNull(taskId1, "Task is not started!");
			assertEquals(assigneesTask.getSecond().get("bpm_assignees"), assignees,
					"Pool actors does not match!");
			assertEquals(assigneesTask.getSecond().get(ownerKey), null,
					"Task owner should not be set!");
			Map<String, Serializable> map = new HashMap<>();
			// update 1
			Map<String, Serializable> updatedTaskByRequest = testUpdateTaskByRequest(taskId1, map);
			assertEquals(updatedTaskByRequest.get("bpm_assignees"), assignees,
					"Pool actors does not match!");
			// update 2
			assignees.add("tester");
			map.put("bpm:assignees", assignees);
			updatedTaskByRequest = testUpdateTaskByRequest(taskId1, map);
			assertEquals(updatedTaskByRequest.get("bpm_assignees"), assignees,
					"Pool actors does not match!");
			assertEquals(updatedTaskByRequest.get(ownerKey), null, "Task owner should not be set!");
			// update 3 and set owner
			map = new HashMap<>();
			map.put("cm:owner", "admin");
			updatedTaskByRequest = testUpdateTaskByRequest(taskId1, map);
			assertEquals(updatedTaskByRequest.get(ownerKey), "admin", "Task owner should be set!");
			testCompleteTaskByRequest(taskId1);

			// ########### case 2 ################
			String groupPoolValue = "GROUP_ALFRESCO_ADMINISTRATORS";
			Pair<String, Map<String, Serializable>> groupAssigneeTask = testStartPoolTaskByRequest(
					"bpm:groupAssignee", groupPoolValue);
			taskId2 = groupAssigneeTask.getFirst();
			Assert.assertNotNull(taskId2, "Task is not started!");
			assertEquals(groupAssigneeTask.getSecond().get("bpm_groupAssignee"), groupPoolValue,
					"Pool actors does not match!");
			assertEquals(groupAssigneeTask.getSecond().get(ownerKey), null,
					"Task owner should not be set!");
			map = new HashMap<>();
			// update 1
			map.put("bpm:groupAssignee", "GROUP_admin");
			updatedTaskByRequest = testUpdateTaskByRequest(taskId2, map);
			assertEquals(updatedTaskByRequest.get("bpm_groupAssignee"), "GROUP_admin",
					"Pool actors does not match!");
			assertEquals(updatedTaskByRequest.get(ownerKey), null, "Task owner should not be set!");
			// update 2
			assertEquals(updatedTaskByRequest.get("bpm_priority"), 2, "Priority is middle!");
			map.put("bpm:priority", 1);
			updatedTaskByRequest = testUpdateTaskByRequest(taskId2, map);
			assertEquals(updatedTaskByRequest.get("bpm_priority"), 1, "Priority is 1!");
			// update 3 and set owner
			map = new HashMap<>();
			map.put("cm:owner", "admin");
			updatedTaskByRequest = testUpdateTaskByRequest(taskId2, map);
			assertEquals(updatedTaskByRequest.get(ownerKey), "admin", "Task owner should be set!");
			testCompleteTaskByRequest(taskId2);

			// ########### case 3 ################
			assignees = new ArrayList<String>();
			assignees.add("bbanchev");
			assignees.add("admin");
			assignees.add(groupPoolValue);
			String multiAssignees = "cmfwf:multiAssignees";
			assigneesTask = testStartPoolTaskByRequest(multiAssignees, assignees);
			taskId3 = assigneesTask.getFirst();
			Assert.assertEquals(assigneesTask.getSecond().get(ownerKey), null,
					"Task owner should not be set!");
			assertEquals(assigneesTask.getSecond().get(multiAssignees.replace(":", "_")),
					assignees, "Pool actors does not match!");
			Assert.assertNotNull(taskId3, "Task is not started!");
			map = new HashMap<>();
			// update 1
			map.put(multiAssignees, assignees);
			updatedTaskByRequest = testUpdateTaskByRequest(taskId3, map);
			assertEquals(updatedTaskByRequest.get(multiAssignees.replace(":", "_")), assignees,
					"Pool actors does not match!");
			assertEquals(updatedTaskByRequest.get(ownerKey), null, "Task owner should not be set!");
			// update 2
			assignees.add("tester");
			map.put(multiAssignees, assignees);
			updatedTaskByRequest = testUpdateTaskByRequest(taskId3, map);
			assertEquals(updatedTaskByRequest.get(multiAssignees.replace(":", "_")), assignees,
					"Pool actors does not match!");
			// update 3 and set owner
			map = new HashMap<>();
			map.put("cm:owner", "admin");
			updatedTaskByRequest = testUpdateTaskByRequest(taskId3, map);
			assertEquals(updatedTaskByRequest.get(ownerKey), "admin", "Task owner should be set!");
			testCompleteTaskByRequest(taskId3);

		} catch (DMSClientException e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		} finally {
			String foundTaskId = findTaskNode(taskId1);
			if (foundTaskId != null) {
				cleanUpDMSIds.add(foundTaskId);
			}
			foundTaskId = findTaskNode(taskId2);
			if (foundTaskId != null) {
				cleanUpDMSIds.add(foundTaskId);
			}
			foundTaskId = findTaskNode(taskId3);
			if (foundTaskId != null) {
				cleanUpDMSIds.add(foundTaskId);
			}
		}
	}

	/**
	 * Test start-complete cycle for standalone tasks via http requests
	 */
	@Test(enabled = true)
	public void testHTTPStartCompleteCycle() {
		String taskId = null;
		try {
			taskId = testStartTaskByRequest();
			Assert.assertNotNull(taskId, "Task is not started!");
			Map<String, Serializable> map = new HashMap<>();
			map.put("bpm:assignee", "bbanchev");
			String owner = "admin";
			map.put("cm:owner", owner);
			Map<String, Serializable> updatedTaskByRequest = testUpdateTaskByRequest(taskId, map);
			assertEquals(updatedTaskByRequest.get("cm_owner"), owner, "Task owner should be set!");
			testCompleteTaskByRequest(taskId);
		} catch (DMSClientException e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		} finally {
			String foundTaskId = findTaskNode(taskId);
			if (foundTaskId != null) {
				cleanUpDMSIds.add(foundTaskId);
			}
		}
	}

	/**
	 * Test http start-cancel cycle for standalone tasks via http requests
	 */
	@Test(enabled = true)
	public void testHTTPStartCancelCycle() {
		String taskId = null;
		try {
			taskId = testStartTaskByRequest();
			Assert.assertNotNull(taskId, "Task is not started!");
			Map<String, Serializable> map = new HashMap<>();
			map.put("bpm:assignee", "bbanchev");
			String owner = "admin";
			map.put("cm:owner", owner);
			testUpdateTaskByRequest(taskId, map);
			testCancelTaskByRequest(taskId);
		} catch (DMSClientException e) {
			Assert.fail(e.getMessage());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		} finally {
			String foundTaskId = findTaskNode(taskId);
			if (foundTaskId != null) {
				cleanUpDMSIds.add(foundTaskId);
			}
		}
	}

	/**
	 * Test list children.
	 *
	 * @return the string
	 * @throws DMSClientException
	 *             the dMS client exception
	 * @throws JSONException
	 *             the jSON exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	private String testStartTaskByRequest() throws DMSClientException, JSONException,
			UnsupportedEncodingException {
		JSONObject request = new JSONObject();
		request.put(KEY_TYPE, "cmfwf:standaloneTask");

		request.put(KEY_REFERENCE_ID, getUserHome(userName));
		Map<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("bpm:assignee", "bbanchev");
		map.put("bpm:dueDate", getDueDate());
		map.put("cm:name", "Task Name");
		map.put("cm:title", "Task Title");
		map.put("bpm:description", "Task Description");
		map.put("bpm:priority", 2);
		request.put(KEY_PROPERTIES, map);

		HttpMethod createMethod = httpClient.createMethod(new PostMethod(), request.toString(),
				true);

		String restResult = httpClient.request("/task/instance/start", createMethod);
		//
		// convert the result and get the id
		if (restResult != null) {
			JSONArray data = new JSONObject(restResult).getJSONObject("tasks").getJSONArray(
					KEY_ROOT_DATA);
			Assert.assertEquals(1, data.length(), "Task after start pooled should be returned!");
			String string = data.getJSONObject(0).getString(KEY_ID);
			return string;
		}
		Assert.fail("Task is not started");
		return null;
	}

	/**
	 * Gets a test due date.
	 *
	 * @return the time for dms
	 */
	private long getDueDate() {
		Calendar instance = Calendar.getInstance();
		instance.add(Calendar.MONTH, -1);
		return instance.getTime().getTime();
	}

	/**
	 * Starts a pool task
	 *
	 * @param poolKey
	 *            is the key for pooled actors
	 * @param poolValue
	 *            is the value for pool actors
	 * @return the id of task.
	 * @throws DMSClientException
	 *             the dMS client exception
	 * @throws JSONException
	 *             the jSON exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	private Pair<String, Map<String, Serializable>> testStartPoolTaskByRequest(String poolKey,
			Serializable poolValue) throws DMSClientException, JSONException,
			UnsupportedEncodingException {
		JSONObject request = new JSONObject();
		request.put(KEY_TYPE, "cmfwf:standaloneTask");

		request.put(KEY_REFERENCE_ID, getUserHome(userName));
		Map<String, Serializable> map = new HashMap<String, Serializable>();
		map.put(poolKey, poolValue);
		map.put("bpm:dueDate", getDueDate());
		map.put("cm:name", "Task Name");
		map.put("cm:title", "Task Title");
		map.put("bpm:description", "Task Description");
		map.put("bpm:priority", 2);
		request.put(KEY_PROPERTIES, map);

		HttpMethod createMethod = httpClient.createMethod(new PostMethod(), request.toString(),
				true);

		String restResult = httpClient.request("/task/instance/start", createMethod);
		//
		// convert the result and get the id
		if (restResult != null) {
			JSONArray data = new JSONObject(restResult).getJSONObject("tasks").getJSONArray(
					KEY_ROOT_DATA);
			Assert.assertEquals(1, data.length(), "Task after start pooled should be returned!");
			String id = data.getJSONObject(0).getString(KEY_ID);
			return new Pair<String, Map<String, Serializable>>(id,
					AlfrescoUtils.jsonObjectToMap(data.getJSONObject(0).getJSONObject(
							KEY_PROPERTIES)));
		}
		Assert.fail("Task is not started");
		return null;
	}

	/**
	 * Test update task by request.
	 *
	 * @param id
	 *            the id of task to update
	 * @param additionalData
	 *            is the data to add to the task
	 * @return the metadata for task
	 * @throws DMSClientException
	 *             the dMS client exception
	 * @throws JSONException
	 *             the jSON exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	private Map<String, Serializable> testUpdateTaskByRequest(String id,
			Map<String, Serializable> additionalData) throws DMSClientException, JSONException,
			UnsupportedEncodingException {
		JSONObject request = new JSONObject();
		request.put(KEY_TASK_ID, id);
		Map<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("cm:name", "Task Randomized!");
		map.put("bpm:message", "Task Message!");
		long due = getDueDate();
		map.put("bpm:dueDate", due);
		String descr = "Description Updated 2";
		map.put("bpm:description", descr);
		map.put("cmfwf:testdata", "Task Data");
		map.putAll(additionalData);
		request.put(KEY_PROPERTIES, map);

		HttpMethod createMethod = httpClient.createMethod(new PostMethod(), request.toString(),
				true);

		String restResult = httpClient.request("/workflow/instance/taskupdate", createMethod);
		logger.info("Update response: " + restResult);
		// convert the result and get the id
		if (restResult != null) {
			JSONArray data = new JSONObject(restResult).getJSONArray(KEY_ROOT_DATA);
			Assert.assertEquals(1, data.length(), "Task after update should be returned!");
			JSONObject jsonObject = data.getJSONObject(0);
			JSONObject props = jsonObject.getJSONObject(KEY_PROPERTIES);
			Assert.assertEquals(props.getString("bpm_description"), descr,
					"Task description is not updated!");
			Assert.assertEquals(props.getString("bpm_dueDate"),
					ISO8601DateFormat.format(new Date(due)), "Due date should be set");
			return AlfrescoUtils.jsonObjectToMap(props);
		}
		Assert.fail("Task is not updated");
		return null;

	}

	/**
	 * Test complete task by request.
	 *
	 * @param id
	 *            the id
	 * @throws DMSClientException
	 *             the dMS client exception
	 * @throws JSONException
	 *             the jSON exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	private void testCompleteTaskByRequest(String id) throws DMSClientException, JSONException,
			UnsupportedEncodingException {
		JSONObject request = new JSONObject();
		request.put(KEY_TASK_ID, id);

		Map<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("bpm:dueDate", new Date().getTime());
		map.put("bpm:description", "Description on Complete");
		map.put("cmfwf:testdata", "Task Data with custom key");
		map.put("bpm:priority", 1);
		String outcomeValue = "JobDone";
		map.put("cmfwf:taskOutcome", outcomeValue);
		request.put(KEY_PROPERTIES, map);

		HttpMethod createMethod = httpClient.createMethod(new PostMethod(), request.toString(),
				true);

		String restResult = httpClient.request("/task/instance/complete", createMethod);

		Assert.assertNotNull(restResult, "Result should be json string");
		if (restResult != null) {
			JSONArray data = new JSONObject(restResult).getJSONArray(KEY_ROOT_DATA);
			Assert.assertEquals(1, data.length(), "Task after update should be returned!");
			JSONObject jsonObject = data.getJSONObject(0);
			Assert.assertEquals(jsonObject.getString("state"), "COMPLETED",
					"Task is not completed!");
			Assert.assertEquals(jsonObject.getString("outcome"), outcomeValue,
					"Task is not completed with requested outcome!");
			return;
		}

		Assert.fail("Task is not completed");
	}

	/**
	 * Test cancel task by request.
	 *
	 * @param id
	 *            the id
	 * @throws DMSClientException
	 *             the dMS client exception
	 * @throws JSONException
	 *             the jSON exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	private void testCancelTaskByRequest(String id) throws DMSClientException, JSONException,
			UnsupportedEncodingException {
		JSONObject request = new JSONObject();
		request.put(KEY_TASK_ID, id);

		Map<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("cmfwf:taskOutcome", "Cancel");
		request.put(KEY_PROPERTIES, map);

		HttpMethod createMethod = httpClient.createMethod(new PostMethod(), request.toString(),
				true);

		String restResult = httpClient.request("/task/instance/cancel", createMethod);

		Assert.assertNotNull(restResult, "Result should be json string");

		Assert.assertNotNull(restResult, "Result should be json string");
		if (restResult != null) {
			JSONArray data = new JSONObject(restResult).getJSONArray(KEY_ROOT_DATA);
			Assert.assertEquals(1, data.length(), "Task after update should be returned!");
			JSONObject jsonObject = data.getJSONObject(0);
			Assert.assertEquals(jsonObject.getString("state"), "COMPLETED",
					"Task is not completed!");
			Assert.assertEquals(jsonObject.getString("outcome"), "Cancel", "Task is not completed!");
			return;
		}
	}

	/**
	 * Finds a task with given id. Currently waits 4 seconds before search to allow index
	 *
	 * @param id
	 *            is the task id
	 * @return the nodeRef or null if not found
	 */
	private String findTaskNode(String id) {
		try {
			// Sleep 5 sec to allow indexing
			Thread.sleep(5000);
			JSONObject request = new JSONObject();
			request.put("query", "PATH:\"/sys:system/cmfwf:taskIndexesSpace//*\" AND cm:name:\""
					+ id + "\"");
			request.put("paging", getPaging());
			HttpMethod createMethod = createMethod(request.toString(), new PostMethod());

			String callWebScript = httpClient.request("/cmf/search", createMethod);

			assertNotNull(callWebScript, "Searching failed!");
			JSONObject result = new JSONObject(callWebScript);
			return result.getJSONObject(KEY_ROOT_DATA).getJSONArray(KEY_DATA_ITEMS)
					.getJSONObject(0).getString(KEY_NODEREF);
		} catch (Exception e) {
			logger.error("Error during task search: " + e.getMessage(), e);
		}
		return null;
	}
}
