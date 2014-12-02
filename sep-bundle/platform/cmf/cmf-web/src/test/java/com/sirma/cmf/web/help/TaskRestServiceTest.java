package com.sirma.cmf.web.help;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.javacrumbs.jsonunit.JsonAssert;

import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.task.TaskRestService;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.util.InstanceProxyMock;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.web.util.DateUtil;

/**
 * Tests for TaskRestService.
 * 
 * @author svelikov
 */
@Test
public class TaskRestServiceTest extends CMFTest {

	/** The service. */
	private TaskRestService service;

	/** The task service. */
	private TaskService taskService;

	/** The standalone task instance. */
	private StandaloneTaskInstance standaloneTaskInstance;

	/** The resource service. */
	private ResourceService resourceService;

	/** The authentication service. */
	private AuthenticationService authenticationService;

	/** The date util. */
	private DateUtil dateUtil;


	/**
	 * Instantiates a new task rest service test.
	 */
	@BeforeMethod
	public void init() {
		service = new TaskRestService() {

			@Override
			public Instance fetchInstance(String instanceId, String instanceType) {
				if ("standalonetaskinstance".equals(instanceType)) {
					return standaloneTaskInstance;
				}
				return null;
			}

			@Override
			public JSONObject convertInstanceToJSON(Instance instance) {
				JSONObject object = new JSONObject();
				JsonUtil.addToJson(object, "dbId", instance.getId());
				JsonUtil.addToJson(object, "type", instance.getClass().getSimpleName()
						.toLowerCase());
				return object;
			}
		};

		taskService = Mockito.mock(TaskService.class);
		resourceService = Mockito.mock(ResourceService.class);
		authenticationService = Mockito.mock(AuthenticationService.class);
		dateUtil = new DateUtil();
		ReflectionUtils.setField(dateUtil, "typeConverter", createTypeConverter());

		ReflectionUtils.setField(service, "log", SLF4J_LOG);
		ReflectionUtils.setField(service, "taskService", taskService);
		ReflectionUtils.setField(service, "resourceService", resourceService);
		ReflectionUtils.setField(service, "dateUtil", dateUtil);
		ReflectionUtils.setField(service, "authenticationService",
				new InstanceProxyMock<AuthenticationService>(authenticationService));
	}

	/**
	 * Test log work_invalid data.
	 */
	public void testLogWork_invalidData() {
		String data = "";
		Response response = service.postLoggedWork(null, "taskinstance", null, data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));
		response = service.postLoggedWork("", "taskinstance", null, data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));

		// invalid data
		response = service.postLoggedWork("emf:task", "taskinstance", null, data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));

		data = "{}";
		// not logged in
		response = service.postLoggedWork("emf:task", "taskinstance", null, data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));

		// invalid user
		response = service.postLoggedWork("emf:task", "taskinstance", "emf:user", data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));

		Mockito.verifyZeroInteractions(taskService);

		// missing task
		Mockito.when(resourceService.findResource("emf:user")).thenReturn(new EmfUser("user"));
		response = service.postLoggedWork("emf:task", "taskinstance", "emf:user", data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));
	}

	/**
	 * Test log work.
	 * 
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testLogWork() throws JSONException {
		String data = buildLogWorkRequest(480, "description", "2014-10-10T10:10:00+00:00");
		Map<String, Serializable> loggedData = buildLogWorkProperties(480, "description",
				"2014-10-10T10:10:00+00:00");

		AbstractTaskInstance task = new TaskInstance();
		task.setId("emf:task");

		Mockito.when(taskService.loadByDbId("emf:task")).thenReturn(task);
		EmfUser user = new EmfUser();
		user.setIdentifier("user");
		user.setId("emf:user");
		Mockito.when(authenticationService.getCurrentUser()).thenReturn(user);

		Response response = service.postLoggedWork("emf:task", "taskinstance", null, data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Mockito.verify(taskService).logWork(Mockito.eq(task.toReference()), Mockito.eq("user"),
				Mockito.eq(loggedData));
		Assert.assertTrue(response.getEntity().toString().contains("false"));

		// now return valid response from the service
		Mockito.when(
				taskService.logWork(Mockito.eq(task.toReference()), Mockito.eq("user"),
						Mockito.eq(loggedData))).thenReturn("emf:link");
		response = service.postLoggedWork("emf:task", "taskinstance", null, data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertFalse(response.getEntity().toString().contains("false"));
		Assert.assertNotNull(response.getEntity());
		Mockito.verify(taskService, Mockito.times(2)).logWork(Mockito.eq(task.toReference()),
				Mockito.eq("user"), Mockito.eq(loggedData));

		JSONObject jsonObject = JsonUtil.createObjectFromString(response.getEntity().toString());
		Assert.assertNotNull(jsonObject);
		Assert.assertTrue(jsonObject.has("success"));
		Assert.assertTrue(JsonUtil.getBooleanValue(jsonObject, "success"));
		Assert.assertTrue(jsonObject.has("data"));
		JSONObject returnData = JsonUtil.getJsonObject(jsonObject, "data");
		Assert.assertEquals(JsonUtil.getStringValue(returnData, "id"), "emf:link");
		Assert.assertEquals(JsonUtil.getStringValue(returnData, "userName"), "user");
		Assert.assertEquals(JsonUtil.getStringValue(returnData, "userDisplayName"), null);
		Assert.assertEquals(JsonUtil.getBooleanValue(returnData, "editDetails"), Boolean.TRUE);
		Assert.assertEquals(JsonUtil.getBooleanValue(returnData, "delete"), Boolean.TRUE);
	}

	/**
	 * Test log work_invalid data.
	 */
	public void testUpdateLogWork_invalidData() {
		String data = "";
		Response response = service.putLoggedWork(null, "taskinstance", null, null, data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));
		response = service.putLoggedWork("", "taskinstance", null, null, data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));
		response = service.putLoggedWork("emf:task", "taskinstance", null, "", data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));

		// invalid data
		response = service.putLoggedWork("emf:task", "taskinstance", null, "emf:link", data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));

		data = "{}";
		// not logged in
		response = service.putLoggedWork("emf:task", "taskinstance", null, "emf:link", data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));

		// invalid user
		response = service.putLoggedWork("emf:task", "taskinstance", "emf:user", "emf:link", data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Assert.assertTrue(response.getEntity().toString().contains("false"));

		Mockito.verifyZeroInteractions(taskService);
	}

	/**
	 * Test update log work.
	 * 
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testUpdateLogWork() throws JSONException {
		String data = buildLogWorkRequest(480, "description", "2014-10-10T10:10:00+00:00");
		Map<String, Serializable> loggedData = buildLogWorkProperties(480, "description",
				"2014-10-10T10:10:00+00:00");

		AbstractTaskInstance task = new TaskInstance();
		task.setId("emf:task");

		Mockito.when(taskService.loadByDbId("emf:task")).thenReturn(task);
		EmfUser user = new EmfUser();
		user.setIdentifier("user");
		user.setId("emf:user");
		Mockito.when(authenticationService.getCurrentUser()).thenReturn(user);

		Response response = service.putLoggedWork("emf:task", "taskinstance", null, "emf:link",
				data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertNotNull(response.getEntity());
		Mockito.verify(taskService).updateLoggedWork("emf:link", loggedData);
		Assert.assertTrue(response.getEntity().toString().contains("false"));

		// now return valid response from the service
		Mockito.when(taskService.updateLoggedWork("emf:link", loggedData)).thenReturn(true);
		response = service.putLoggedWork("emf:task", "taskinstance", null, "emf:link", data);
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());
		Assert.assertFalse(response.getEntity().toString().contains("false"));
		Assert.assertNotNull(response.getEntity());
		Mockito.verify(taskService, Mockito.times(2)).updateLoggedWork(Mockito.eq("emf:link"),
				Mockito.eq(loggedData));

		JSONObject jsonObject = JsonUtil.createObjectFromString(response.getEntity().toString());
		Assert.assertNotNull(jsonObject);
		Assert.assertTrue(jsonObject.has("success"));
		Assert.assertTrue(JsonUtil.getBooleanValue(jsonObject, "success"));
		Assert.assertTrue(jsonObject.has("data"));
		JSONObject returnData = JsonUtil.getJsonObject(jsonObject, "data");
		Assert.assertEquals(JsonUtil.getStringValue(returnData, "id"), "emf:link");
		Assert.assertEquals(JsonUtil.getStringValue(returnData, "userName"), "user");
		Assert.assertEquals(JsonUtil.getStringValue(returnData, "userDisplayName"), null);
		Assert.assertEquals(JsonUtil.getBooleanValue(returnData, "editDetails"), Boolean.TRUE);
		Assert.assertEquals(JsonUtil.getBooleanValue(returnData, "delete"), Boolean.TRUE);
	}

	/**
	 * Builds the log work request.
	 * 
	 * @param time
	 *            the time
	 * @param description
	 *            the description
	 * @param date
	 *            the date
	 * @return the string
	 */
	private String buildLogWorkRequest(int time, String description, String date) {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, TaskProperties.TIME_SPENT, time);
		JsonUtil.addToJson(object, TaskProperties.WORK_DESCRIPTION, description);
		JsonUtil.addToJson(object, TaskProperties.START_DATE, date);
		return object.toString();
	}

	/**
	 * Builds the log work properties.
	 * 
	 * @param time
	 *            the time
	 * @param description
	 *            the description
	 * @param date
	 *            the date
	 * @return the map
	 */
	private Map<String, Serializable> buildLogWorkProperties(int time, String description,
			String date) {
		Map<String, Serializable> map = new LinkedHashMap<>();
		map.put(TaskProperties.TIME_SPENT, time);
		map.put(TaskProperties.WORK_DESCRIPTION, description);
		map.put(TaskProperties.START_DATE,
				TypeConverterUtil.getConverter().convert(Date.class, date));
		return map;
	}

	/**
	 * Test for getting subtasks for given task.
	 */
	@SuppressWarnings("boxing")
	public void subtasksTest() {
		Response response = service.subtasks(null, null, true);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

		response = service.subtasks("instance1", null, true);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

		response = service.subtasks(null, "taskinstance", true);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

		//
		response = service.subtasks("instance1", "standalonetaskinstance-notfound", true);
		assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());

		//
		standaloneTaskInstance = createStandaloneTaskInstance(Long.valueOf(1L));
		Mockito.when(taskService.hasSubTasks(standaloneTaskInstance, TaskState.IN_PROGRESS))
				.thenReturn(Boolean.FALSE);
		response = service.subtasks("instance1", "standalonetaskinstance", true);
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		String responseData = response.getEntity().toString();
		JsonAssert.assertJsonEquals("{\"hasSubtasks\":false}", responseData);

		//
		Mockito.when(taskService.hasSubTasks(standaloneTaskInstance, TaskState.IN_PROGRESS))
				.thenReturn(Boolean.TRUE);
		// if checkonly=true, then no data is expected
		response = service.subtasks("instance1", "standalonetaskinstance", true);
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		responseData = response.getEntity().toString();
		JsonAssert.assertJsonEquals("{\"hasSubtasks\":true}", responseData);

		// if checkonly=false, then data is expected but service doesn't return any
		response = service.subtasks("instance1", "standalonetaskinstance", false);
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		responseData = response.getEntity().toString();
		JsonAssert.assertJsonEquals("{\"hasSubtasks\":true,\"subtasks\":[]}", responseData);

		// service returns some task instances as subtasks
		List<AbstractTaskInstance> subtasks = new ArrayList<>();
		subtasks.add(createStandaloneTaskInstance(Long.valueOf(1L)));
		subtasks.add(createStandaloneTaskInstance(Long.valueOf(2L)));
		Mockito.when(taskService.getSubTasks(standaloneTaskInstance)).thenReturn(subtasks);
		response = service.subtasks("instance1", "standalonetaskinstance", false);
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		responseData = response.getEntity().toString();
		JsonAssert
				.assertJsonEquals(
						"{\"hasSubtasks\":true,\"subtasks\":[{\"dbId\":1,\"type\":\"standalonetaskinstance\"},{\"dbId\":2,\"type\":\"standalonetaskinstance\"}]}",
						responseData);
	}
}
