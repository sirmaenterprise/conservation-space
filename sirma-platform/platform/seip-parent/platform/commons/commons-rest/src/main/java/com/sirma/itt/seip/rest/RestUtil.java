package com.sirma.itt.seip.rest;

import java.util.Collection;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonUtil;

/**
 * Utility class for building rest responses
 *
 * @author BBonev
 */
public class RestUtil {

	/**
	 * Default property used to identify error group in responses
	 */
	public static final String DEFAULT_ERROR_PROPERTY = "error";
	/**
	 * Default property used to identify the data group in requests and responses
	 */
	public static final String DEFAULT_DATA_PROPERTY = "data";

	/**
	 * Instantiates a new rest utility.
	 */
	private RestUtil() {
		// utility class
	}

	/**
	 * Build a response object.
	 *
	 * @param status
	 *            The response status code.
	 * @param entity
	 *            Entity object.
	 * @return Created response object.
	 */
	public static Response buildResponse(Status status, Object entity) {
		if (status == null) {
			return null;
		}
		if (entity == null) {
			return Response.status(status).build();
		}
		return Response.status(status).entity(entity).build();
	}

	/**
	 * Builds the bad request response.
	 *
	 * @param entity
	 *            the entity
	 * @return the response
	 */
	public static Response buildBadRequestResponse(Object entity) {
		return buildResponse(Status.BAD_REQUEST, entity);
	}

	/**
	 * Builds the error response.
	 *
	 * @param entity
	 *            the entity
	 * @return the response
	 */
	public static Response buildErrorResponse(Object entity) {
		return buildResponse(Status.INTERNAL_SERVER_ERROR, entity);
	}

	/**
	 * Builds the error response.
	 *
	 * @param statusCode
	 *            the status code
	 * @param errorMessage
	 *            the error message
	 * @return the response
	 */
	public static Response buildErrorResponse(Status statusCode, String errorMessage) {
		JSONObject jsonObject = new JSONObject();
		JsonUtil.addToJson(jsonObject, DEFAULT_ERROR_PROPERTY, JsonUtil.mapError(errorMessage));
		return buildResponse(statusCode, jsonObject.toString());
	}

	/**
	 * Builds the data response with default data field name
	 *
	 * @param data
	 *            the data
	 * @return the response
	 */
	public static Response buildDataResponse(Object data) {
		return buildDataResponse(data, DEFAULT_DATA_PROPERTY);
	}

	/**
	 * Builds data response by wrapping the given collection of elements as {@link JSONArray}.
	 *
	 * @param converted
	 *            the converted
	 * @return the response
	 */
	public static Response buildDataResponse(Collection<JSONObject> converted) {
		return buildDataResponse(new JSONArray(converted));
	}

	/**
	 * Read data request using the default data field name.
	 *
	 * @param <T>
	 *            the generic type
	 * @param request
	 *            the request
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readDataRequest(String request) {
		return (T) readDataRequest(request, DEFAULT_DATA_PROPERTY);
	}

	/**
	 * Builds the data response using the specified data name
	 *
	 * @param data
	 *            the data
	 * @return the response
	 */
	public static Response buildOkResponse(Object data) {
		return buildResponse(Status.OK, data);
	}

	/**
	 * Builds the data response using the specified data name
	 *
	 * @param data
	 *            the data
	 * @param dataName
	 *            the data name
	 * @return the response
	 */
	public static Response buildDataResponse(Object data, String dataName) {
		return buildResponse(Status.OK, buildDataEntity(data, dataName).toString());
	}

	/**
	 * Builds the data entity.
	 *
	 * @param data
	 *            the data
	 * @param dataName
	 *            the data name
	 * @return the JSON object
	 */
	public static JSONObject buildDataEntity(Object data, String dataName) {
		JSONObject result = new JSONObject();
		JsonUtil.addToJson(result, dataName, data);
		return result;
	}

	/**
	 * Read data request from the specified data name
	 *
	 * @param request
	 *            the request
	 * @param dataName
	 *            the data name
	 * @return a {@link JSONObject} or {@link JSONArray} or <code>null</code> if no found
	 */
	public static Object readDataRequest(String request, String dataName) {
		JSONObject jsonObject = JsonUtil.createObjectFromString(request);
		return JsonUtil.getValueOrNull(jsonObject, dataName);
	}
}
