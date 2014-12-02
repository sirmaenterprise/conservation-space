package com.sirma.itt.emf.rest;

import static com.sirma.itt.emf.executors.ExecutableOperationProperties.OPERATION;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.OPERATIONS;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.RESPONSE;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.RESPONSE_STATE;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.STATUS;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.javacrumbs.jsonunit.JsonAssert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.exceptions.StaleDataModificationException;
import com.sirma.itt.emf.executors.ExecutableOperation;
import com.sirma.itt.emf.executors.ExecutableOperationFactory;
import com.sirma.itt.emf.executors.ExecutableOperationFactoryImpl;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;
import com.sirma.itt.emf.util.EmfTest;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * The Class ExecutorRestServiceTest.
 * 
 * @author BBonev
 */
@Test
public class ExecutorRestServiceTest extends EmfTest {

	/**
	 * Test with invalid data.
	 */
	public void testWithInvalidData() {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setField(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<String, ExecutableOperation>();
		ReflectionUtils.setField(factory, "operationsMapping", operationsMapping);

		JSONArray array = new JSONArray();
		JSONObject json = new JSONObject();
		JsonUtil.addToJson(json, OPERATION, "someAction");
		array.put(json);

		Response response;
		// test with invalid data
		response = service.execute(null);
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
		response = service.execute("");
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
		response = service.execute("[]");
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
		response = service.execute("ala bala");
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

		JSONObject request = new JSONObject();
		// no operations send but valid json object
		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Test execution.
	 * 
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testExecution() throws JSONException {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setField(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<String, ExecutableOperation>();
		ReflectionUtils.setField(factory, "operationsMapping", operationsMapping);

		JSONArray array = new JSONArray();
		JSONObject json = new JSONObject();
		JsonUtil.addToJson(json, OPERATION, "someAction");
		array.put(json);

		Response response;

		JSONObject request = new JSONObject();
		JsonUtil.addToJson(request, OPERATIONS, array);

		// at least one operation
		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		checkResponse(response, SchedulerEntryStatus.NOT_RUN, SchedulerEntryStatus.NOT_RUN, null);

		// default response handling - OK
		ExecutableOperation operation = createOperation(operationsMapping, "someAction");
		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());

		checkResponse(response, SchedulerEntryStatus.COMPLETED, SchedulerEntryStatus.COMPLETED,
				null);

		// managed response - OK
		JSONObject toReturn = new JSONObject();
		JsonUtil.addToJson(toReturn, "key", "someValue");
		Mockito.when(operation.execute(Mockito.any(SchedulerContext.class))).thenReturn(
				new OperationResponse(SchedulerEntryStatus.COMPLETED, toReturn));

		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());

		checkResponse(response, SchedulerEntryStatus.COMPLETED, SchedulerEntryStatus.COMPLETED,
				toReturn.toString());

		// managed response - failed
		Mockito.when(operation.execute(Mockito.any(SchedulerContext.class))).thenReturn(
				new OperationResponse(SchedulerEntryStatus.FAILED, toReturn));

		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());

		checkResponse(response, SchedulerEntryStatus.FAILED, SchedulerEntryStatus.FAILED,
				toReturn.toString());
	}

	/**
	 * Test rollback.
	 * 
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testRollback() throws JSONException {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setField(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<String, ExecutableOperation>();
		ReflectionUtils.setField(factory, "operationsMapping", operationsMapping);

		JSONArray array = new JSONArray();
		JSONObject badJson = new JSONObject();
		JsonUtil.addToJson(badJson, OPERATION, "failAction");
		array.put(badJson);
		JSONObject goodJson = new JSONObject();
		JsonUtil.addToJson(goodJson, OPERATION, "goodAction");
		array.put(goodJson);

		Response response;

		JSONObject request = new JSONObject();
		JsonUtil.addToJson(request, OPERATIONS, array);

		ExecutableOperation bad = createOperation(operationsMapping, "failAction");
		Mockito.when(bad.execute(Mockito.any(SchedulerContext.class))).thenThrow(
				new StaleDataModificationException());

		ExecutableOperation good = createOperation(operationsMapping, "goodAction");
		JSONObject toReturn = new JSONObject();
		JsonUtil.addToJson(toReturn, "key", "someValue");
		Mockito.when(good.execute(Mockito.any(SchedulerContext.class))).thenReturn(new OperationResponse(SchedulerEntryStatus.COMPLETED, toReturn));

		// fail to rollback
		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());

		checkResponse(response, "failAction", 2, 0, SchedulerEntryStatus.ROLLBACK_FAILED,
				SchedulerEntryStatus.ROLLBACK_FAILED, null);
		checkResponse(response, "goodAction", 2, 1, SchedulerEntryStatus.SKIPPED,
				SchedulerEntryStatus.ROLLBACK_FAILED, null);

		// successful rollback
		Mockito.when(bad.rollback(Mockito.any(SchedulerContext.class))).thenReturn(true);

		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());

		checkResponse(response, "failAction", 2, 0, SchedulerEntryStatus.ROLLBACKED,
				SchedulerEntryStatus.ROLLBACKED, null);
		checkResponse(response, "goodAction", 2, 1, SchedulerEntryStatus.SKIPPED,
				SchedulerEntryStatus.ROLLBACKED, null);

		// reverse order

		array = new JSONArray();
		array.put(goodJson);
		array.put(badJson);

		request = new JSONObject();
		JsonUtil.addToJson(request, OPERATIONS, array);

		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());

		checkResponse(response, "goodAction", 2, 0, SchedulerEntryStatus.ROLLBACK_FAILED,
				SchedulerEntryStatus.ROLLBACK_FAILED, null);
		checkResponse(response, "failAction", 2, 1, SchedulerEntryStatus.ROLLBACKED,
				SchedulerEntryStatus.ROLLBACK_FAILED, null);

		// successful rollback
		Mockito.when(good.rollback(Mockito.any(SchedulerContext.class))).thenReturn(true);

		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());

		checkResponse(response, "goodAction", 2, 0, SchedulerEntryStatus.ROLLBACKED,
				SchedulerEntryStatus.ROLLBACKED, "{\"key\":\"someValue\"}");
		checkResponse(response, "failAction", 2, 1, SchedulerEntryStatus.ROLLBACKED,
				SchedulerEntryStatus.ROLLBACKED, null);
	}

	/**
	 * Creates the operation.
	 * 
	 * @param operationsMapping
	 *            the operations mapping
	 * @param action
	 *            the action
	 * @return the executable operation
	 */
	private ExecutableOperation createOperation(Map<String, ExecutableOperation> operationsMapping,
			String action) {
		ExecutableOperation operation = Mockito.mock(ExecutableOperation.class);
		Mockito.when(operation.getOperation()).thenReturn(action);
		Mockito.when(operation.parseRequest(Mockito.any(JSONObject.class))).thenReturn(
				new SchedulerContext());

		operationsMapping.put(action, operation);
		return operation;
	}

	/**
	 * Check response.
	 * 
	 * @param response
	 *            the response
	 * @param status
	 *            the status
	 * @param overall
	 *            the overall
	 * @param expectedResult
	 *            the expected result
	 * @throws JSONException
	 *             the JSON exception
	 */
	private void checkResponse(Response response, SchedulerEntryStatus status,
			SchedulerEntryStatus overall, Object expectedResult) throws JSONException {
		Object object = response.getEntity();
		Assert.assertNotNull(object);

		JSONObject jsonObject = new JSONObject(object.toString());

		JSONArray jsonArray = JsonUtil.getJsonArray(jsonObject, OPERATIONS);
		Assert.assertEquals(jsonArray.length(), 1);

		JSONObject responceObject = (JSONObject) jsonArray.get(0);
		Assert.assertEquals(JsonUtil.getStringValue(responceObject, OPERATION), "someAction");
		Assert.assertEquals(getStatus(responceObject), status);

		Object value = JsonUtil.getValueOrNull(responceObject, RESPONSE);
		if (value != null) {
			JsonAssert.assertJsonEquals(expectedResult, value.toString());
		} else {
			JsonAssert.assertJsonEquals(expectedResult, value);
			// Assert.assertEquals(value, expectedResult);
		}

		JSONObject responseState = JsonUtil.getJsonObject(jsonObject, RESPONSE_STATE);
		Assert.assertNotNull(responseState);
		String s = JsonUtil.getStringValue(responseState, STATUS);
		SchedulerEntryStatus entryStatus = SchedulerEntryStatus.valueOf(s);
		Assert.assertEquals(entryStatus, overall);
	}

	/**
	 * Check response.
	 * 
	 * @param response
	 *            the response
	 * @param action
	 *            the action
	 * @param length
	 *            the length
	 * @param index
	 *            the index
	 * @param status
	 *            the status
	 * @param overall
	 *            the overall
	 * @param expectedResult
	 *            the expected result
	 * @throws JSONException
	 *             the JSON exception
	 */
	private void checkResponse(Response response, String action, int length, int index,
			SchedulerEntryStatus status, SchedulerEntryStatus overall, Object expectedResult)
			throws JSONException {
		Object object = response.getEntity();
		Assert.assertNotNull(object);

		JSONObject jsonObject = new JSONObject(object.toString());
		JSONArray jsonArray = JsonUtil.getJsonArray(jsonObject, OPERATIONS);
		Assert.assertEquals(jsonArray.length(), length);

		JSONObject responceObject = (JSONObject) jsonArray.get(index);
		Assert.assertEquals(JsonUtil.getStringValue(responceObject, OPERATION), action);
		Assert.assertEquals(getStatus(responceObject), status);

		Object value = JsonUtil.getValueOrNull(responceObject, RESPONSE);
		if (value != null) {
			JsonAssert.assertJsonEquals(expectedResult, value.toString());
		} else {
			JsonAssert.assertJsonEquals(expectedResult, value);
			// Assert.assertEquals(value, expectedResult);
		}

		JSONObject responseState = JsonUtil.getJsonObject(jsonObject, RESPONSE_STATE);
		Assert.assertNotNull(responseState);
		String s = JsonUtil.getStringValue(responseState, STATUS);
		SchedulerEntryStatus entryStatus = SchedulerEntryStatus.valueOf(s);
		Assert.assertEquals(entryStatus, overall);
	}

	/**
	 * Gets the status.
	 * 
	 * @param responceObject
	 *            the responce object
	 * @return the status
	 */
	private SchedulerEntryStatus getStatus(JSONObject responceObject) {
		JSONObject responseState = JsonUtil.getJsonObject(responceObject, RESPONSE_STATE);
		Assert.assertNotNull(responseState);
		String statusKey = JsonUtil.getStringValue(responseState, STATUS);
		Assert.assertNotNull(statusKey);
		SchedulerEntryStatus status = SchedulerEntryStatus.valueOf(statusKey);
		Assert.assertNotNull(status);
		return status;
	}
}
