/*
 *
 */
package com.sirma.itt.emf.rest;

import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.EXECUTE_ATOMICALLY;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.OPERATION;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.OPERATIONS;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.RESPONSE;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.RESPONSE_STATE;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.STATUS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.exceptions.StaleDataModificationException;
import com.sirma.itt.seip.instance.actions.ExecutableOperation;
import com.sirma.itt.seip.instance.actions.ExecutableOperationFactory;
import com.sirma.itt.seip.instance.actions.ExecutableOperationFactoryImpl;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.actions.OperationResponse;
import com.sirma.itt.seip.instance.actions.OperationStatus;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.util.ReflectionUtils;

import net.javacrumbs.jsonunit.JsonAssert;

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
		ReflectionUtils.setFieldValue(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<>();
		ReflectionUtils.setFieldValue(factory, "operationsMapping", operationsMapping);

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
	}

	/**
	 * Test with invalid data_no op.
	 */
	public void testWithInvalidData_noOp() {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setFieldValue(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<>();
		ReflectionUtils.setFieldValue(factory, "operationsMapping", operationsMapping);

		JSONArray array = new JSONArray();
		JSONObject json = new JSONObject();
		JsonUtil.addToJson(json, OPERATION, "someAction");
		array.put(json);

		JSONObject request = new JSONObject();
		// no operations send but valid json object
		Response response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Test execution_at least one operation.
	 *
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testExecution_atLeastOneOperation() throws JSONException {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setFieldValue(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<>();
		ReflectionUtils.setFieldValue(factory, "operationsMapping", operationsMapping);

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
		checkResponse(response, OperationStatus.NOT_RUN, OperationStatus.NOT_RUN, null);
	}

	/**
	 * Test execution_default response handling.
	 *
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testExecution_defaultResponseHandling() throws JSONException {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setFieldValue(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<>();
		ReflectionUtils.setFieldValue(factory, "operationsMapping", operationsMapping);

		JSONArray array = new JSONArray();
		JSONObject json = new JSONObject();
		JsonUtil.addToJson(json, OPERATION, "someAction");
		array.put(json);

		Response response;

		JSONObject request = new JSONObject();
		JsonUtil.addToJson(request, OPERATIONS, array);

		createOperation(operationsMapping, "someAction");
		// default response handling - OK
		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());

		checkResponse(response, OperationStatus.COMPLETED, OperationStatus.COMPLETED, null);
	}

	/**
	 * Test execution_managed_ok.
	 *
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testExecution_managed_ok() throws JSONException {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setFieldValue(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<>();
		ReflectionUtils.setFieldValue(factory, "operationsMapping", operationsMapping);

		JSONArray array = new JSONArray();
		JSONObject json = new JSONObject();
		JsonUtil.addToJson(json, OPERATION, "someAction");
		array.put(json);

		Response response;

		JSONObject request = new JSONObject();
		JsonUtil.addToJson(request, OPERATIONS, array);

		ExecutableOperation operation = createOperation(operationsMapping, "someAction");

		// managed response - OK
		JSONObject toReturn = new JSONObject();
		JsonUtil.addToJson(toReturn, "key", "someValue");
		when(operation.execute(any(OperationContext.class)))
				.thenReturn(new OperationResponse(OperationStatus.COMPLETED, toReturn));

		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());

		checkResponse(response, OperationStatus.COMPLETED, OperationStatus.COMPLETED, toReturn.toString());
	}

	/**
	 * Test execution_managed_failed.
	 *
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testExecution_managed_failed() throws JSONException {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setFieldValue(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<>();
		ReflectionUtils.setFieldValue(factory, "operationsMapping", operationsMapping);

		JSONArray array = new JSONArray();
		JSONObject json = new JSONObject();
		JsonUtil.addToJson(json, OPERATION, "someAction");
		array.put(json);

		Response response;

		JSONObject request = new JSONObject();
		JsonUtil.addToJson(request, OPERATIONS, array);

		ExecutableOperation operation = createOperation(operationsMapping, "someAction");
		JSONObject toReturn = new JSONObject();
		JsonUtil.addToJson(toReturn, "key", "someValue");
		when(operation.execute(any(OperationContext.class)))
				.thenReturn(new OperationResponse(OperationStatus.COMPLETED, toReturn));

		// managed response - failed
		when(operation.execute(any(OperationContext.class)))
				.thenReturn(new OperationResponse(OperationStatus.FAILED, toReturn));

		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());

		checkResponse(response, OperationStatus.FAILED, OperationStatus.FAILED, toReturn.toString());
	}

	/**
	 * Test rollback_fail to rollback.
	 *
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testRollback_failToRollback() throws JSONException {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setFieldValue(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<>();
		ReflectionUtils.setFieldValue(factory, "operationsMapping", operationsMapping);

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
		when(bad.execute(any(OperationContext.class))).thenThrow(new StaleDataModificationException());

		ExecutableOperation good = createOperation(operationsMapping, "goodAction");
		JSONObject toReturn = new JSONObject();
		JsonUtil.addToJson(toReturn, "key", "someValue");
		when(good.execute(any(OperationContext.class)))
				.thenReturn(new OperationResponse(OperationStatus.COMPLETED, toReturn));

		// fail to rollback
		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());

		checkResponse(response, "failAction", 2, 0, OperationStatus.ROLLBACK_FAILED, OperationStatus.ROLLBACK_FAILED,
				null);
		checkResponse(response, "goodAction", 2, 1, OperationStatus.SKIPPED, OperationStatus.ROLLBACK_FAILED, null);
	}

	/**
	 * Test rollback_successfull rollback.
	 *
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testRollback_successfullRollback() throws JSONException {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setFieldValue(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<>();
		ReflectionUtils.setFieldValue(factory, "operationsMapping", operationsMapping);

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
		when(bad.execute(any(OperationContext.class))).thenThrow(new StaleDataModificationException());

		ExecutableOperation good = createOperation(operationsMapping, "goodAction");
		JSONObject toReturn = new JSONObject();
		JsonUtil.addToJson(toReturn, "key", "someValue");
		when(good.execute(any(OperationContext.class)))
				.thenReturn(new OperationResponse(OperationStatus.COMPLETED, toReturn));

		// successful rollback
		when(bad.rollback(any(OperationContext.class))).thenReturn(true);

		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());

		checkResponse(response, "failAction", 2, 0, OperationStatus.ROLLBACKED, OperationStatus.ROLLBACKED,
 null);
		checkResponse(response, "goodAction", 2, 1, OperationStatus.SKIPPED, OperationStatus.ROLLBACKED, null);
	}

	/**
	 * Test rollback_ atomic operations.
	 *
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testRollback_AtomicOperations() throws JSONException {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setFieldValue(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<>();
		ReflectionUtils.setFieldValue(factory, "operationsMapping", operationsMapping);

		JSONArray array = new JSONArray();
		JSONObject badJson = new JSONObject();
		JsonUtil.addToJson(badJson, OPERATION, "failAction");
		JSONObject goodJson = new JSONObject();
		JsonUtil.addToJson(goodJson, OPERATION, "goodAction");

		array.put(goodJson);
		array.put(badJson);

		Response response;

		JSONObject request = new JSONObject();
		JsonUtil.addToJson(request, OPERATIONS, array);
		JsonUtil.addToJson(request, EXECUTE_ATOMICALLY, Boolean.FALSE);

		ExecutableOperation bad = createOperation(operationsMapping, "failAction");
		when(bad.execute(any(OperationContext.class))).thenThrow(new StaleDataModificationException());

		ExecutableOperation good = createOperation(operationsMapping, "goodAction");
		JSONObject toReturn = new JSONObject();
		JsonUtil.addToJson(toReturn, "key", "someValue");
		when(good.execute(any(OperationContext.class)))
				.thenReturn(new OperationResponse(OperationStatus.COMPLETED, toReturn));

		// successful rollback
		when(bad.rollback(any(OperationContext.class))).thenReturn(true);

		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.OK.getStatusCode());

		checkResponse(response, "goodAction", 2, 0, OperationStatus.COMPLETED, OperationStatus.FAILED,
				toReturn.toString());
		checkResponse(response, "failAction", 2, 1, OperationStatus.FAILED, OperationStatus.FAILED,
				"{\"operation\":\"failAction\"}");
	}

	/**
	 * Test rollback_reverse order.
	 *
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testRollback_reverseOrder() throws JSONException {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setFieldValue(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<>();
		ReflectionUtils.setFieldValue(factory, "operationsMapping", operationsMapping);

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
		when(bad.execute(any(OperationContext.class))).thenThrow(new StaleDataModificationException());

		ExecutableOperation good = createOperation(operationsMapping, "goodAction");
		JSONObject toReturn = new JSONObject();
		JsonUtil.addToJson(toReturn, "key", "someValue");
		when(good.execute(any(OperationContext.class)))
				.thenReturn(new OperationResponse(OperationStatus.COMPLETED, toReturn));

		when(bad.rollback(any(OperationContext.class))).thenReturn(true);
		// reverse order

		array = new JSONArray();
		array.put(goodJson);
		array.put(badJson);

		request = new JSONObject();
		JsonUtil.addToJson(request, OPERATIONS, array);

		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());

		checkResponse(response, "goodAction", 2, 0, OperationStatus.ROLLBACK_FAILED, OperationStatus.ROLLBACK_FAILED,
				null);
		checkResponse(response, "failAction", 2, 1, OperationStatus.ROLLBACKED, OperationStatus.ROLLBACK_FAILED, null);
	}

	/**
	 * Test rollback_reverse order successful.
	 *
	 * @throws JSONException
	 *             the JSON exception
	 */
	public void testRollback_reverseOrderSuccessful() throws JSONException {
		ExecutorRestService service = new ExecutorRestService();
		ExecutableOperationFactory factory = new ExecutableOperationFactoryImpl();
		ReflectionUtils.setFieldValue(service, "factory", factory);
		Map<String, ExecutableOperation> operationsMapping = new HashMap<>();
		ReflectionUtils.setFieldValue(factory, "operationsMapping", operationsMapping);

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
		when(bad.execute(any(OperationContext.class))).thenThrow(new StaleDataModificationException());

		ExecutableOperation good = createOperation(operationsMapping, "goodAction");
		JSONObject toReturn = new JSONObject();
		JsonUtil.addToJson(toReturn, "key", "someValue");
		when(good.execute(any(OperationContext.class)))
				.thenReturn(new OperationResponse(OperationStatus.COMPLETED, toReturn));

		when(bad.rollback(any(OperationContext.class))).thenReturn(true);
		// reverse order

		array = new JSONArray();
		array.put(goodJson);
		array.put(badJson);

		request = new JSONObject();
		JsonUtil.addToJson(request, OPERATIONS, array);

		// successful rollback
		when(good.rollback(any(OperationContext.class))).thenReturn(true);

		response = service.execute(request.toString());
		Assert.assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());

		checkResponse(response, "goodAction", 2, 0, OperationStatus.ROLLBACKED, OperationStatus.ROLLBACKED,
 null);
		checkResponse(response, "failAction", 2, 1, OperationStatus.ROLLBACKED, OperationStatus.ROLLBACKED,
 null);
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
	private ExecutableOperation createOperation(Map<String, ExecutableOperation> operationsMapping, String action) {
		ExecutableOperation operation = Mockito.mock(ExecutableOperation.class);
		when(operation.getOperation()).thenReturn(action);
		when(operation.parseRequest(any(JSONObject.class))).thenReturn(new OperationContext());

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
	private void checkResponse(Response response, OperationStatus status, OperationStatus overall,
			Object expectedResult) throws JSONException {
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
			JsonAssert.assertJsonEquals(expectedResult, null);
		}

		JSONObject responseState = JsonUtil.getJsonObject(jsonObject, RESPONSE_STATE);
		Assert.assertNotNull(responseState);
		String s = JsonUtil.getStringValue(responseState, STATUS);
		OperationStatus entryStatus = OperationStatus.valueOf(s);
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
	private void checkResponse(Response response, String action, int length, int index, OperationStatus status,
			OperationStatus overall, Object expectedResult) throws JSONException {
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
			JsonAssert.assertJsonEquals(expectedResult, null);
		}

		JSONObject responseState = JsonUtil.getJsonObject(jsonObject, RESPONSE_STATE);
		Assert.assertNotNull(responseState);
		String s = JsonUtil.getStringValue(responseState, STATUS);
		OperationStatus entryStatus = OperationStatus.valueOf(s);
		Assert.assertEquals(entryStatus, overall);
	}

	/**
	 * Gets the status.
	 *
	 * @param responceObject
	 *            the responce object
	 * @return the status
	 */
	private OperationStatus getStatus(JSONObject responceObject) {
		JSONObject responseState = JsonUtil.getJsonObject(responceObject, RESPONSE_STATE);
		Assert.assertNotNull(responseState);
		String statusKey = JsonUtil.getStringValue(responseState, STATUS);
		Assert.assertNotNull(statusKey);
		OperationStatus status = OperationStatus.valueOf(statusKey);
		Assert.assertNotNull(status);
		return status;
	}
}
