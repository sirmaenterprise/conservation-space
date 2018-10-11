package com.sirma.itt.seip.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;
import org.testng.annotations.Test;

import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.rest.RestUtil;

/**
 * Test class for {@link RestUtil}
 *
 * @author BBonev
 */
@Test
public class RestUtilTest {

	/**
	 * Test build response.
	 */
	public void testBuildResponse() {
		Response response = RestUtil.buildResponse(Status.OK, "test");
		validateResponse(response, Status.OK);
		assertEquals(response.getEntity(), "test");
	}

	/**
	 * Test build bad request response.
	 */
	public void testBuildBadRequestResponse() {
		Response response = RestUtil.buildBadRequestResponse("test");
		validateResponse(response, Status.BAD_REQUEST);
		assertEquals(response.getEntity(), "test");
	}

	/**
	 * Test build error response.
	 */
	public void testBuildErrorResponse() {
		Response response = RestUtil.buildErrorResponse("test");
		validateResponse(response, Status.INTERNAL_SERVER_ERROR);
		assertEquals(response.getEntity(), "test");
	}

	/**
	 * Test build error response message.
	 */
	public void testBuildErrorResponseMessage() {
		Response response = RestUtil.buildErrorResponse(Status.BAD_REQUEST, "test");
		validateResponse(response, Status.BAD_REQUEST);
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, RestUtil.DEFAULT_ERROR_PROPERTY, JsonUtil.mapError("test"));
		assertEquals(response.getEntity(), object.toString());
	}

	/**
	 * Test data encoding and decoding.
	 */
	public void testDataEncodingAndDecoding() {
		JSONObject data = new JSONObject();
		JsonUtil.addToJson(data, "property", "someData");
		Response response = RestUtil.buildDataResponse(data);
		validateResponse(response, Status.OK);

		Object object = RestUtil.readDataRequest(response.getEntity().toString());
		assertEquals(object.toString(), data.toString());
	}

	/**
	 * Validate response.
	 *
	 * @param response
	 *            the response
	 * @param expectedStatus
	 *            the expected status
	 */
	private void validateResponse(Response response, Status expectedStatus) {
		assertNotNull(response);
		assertEquals(response.getStatus(), expectedStatus.getStatusCode());
		assertNotNull(response.getEntity());
	}

}
