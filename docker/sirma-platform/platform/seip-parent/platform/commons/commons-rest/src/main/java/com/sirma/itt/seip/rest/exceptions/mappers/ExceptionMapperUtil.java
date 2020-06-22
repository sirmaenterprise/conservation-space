package com.sirma.itt.seip.rest.exceptions.mappers;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.rest.models.Error;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.itt.seip.rest.utils.JSON;

/**
 * Helper class for implementing {@link ExceptionMapper}s
 *
 * @author BBonev
 */
public class ExceptionMapperUtil {

	public static final String CODE = "code";
	public static final String ERROR = "error";
	public static final String TYPE = "type";
	public static final String MESSAGE = "message";
	public static final String ERRORS = "errors";


	/**
	 * Builds a response for the given status code and entity and sets the media type as application/json
	 *
	 * @param status
	 * 		the status to set in the response
	 * @param json
	 * 		the json structure to return. Will be converted to string by calling {@link JsonStructure#toString()}
	 * @return the build response
	 */
	public static Response buildJsonExceptionResponse(Status status, JsonStructure json) {
		return Response.status(status).entity(json.toString()).type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Append all exception messages to the given builder. <br>
	 * The method traverses all caused/suppressed exceptions and appends the first non null message as {@code exception}
	 * element in the result object and all others as json array with name {@code other}.
	 *
	 * @param builder
	 * 		the builder to append the messages to
	 * @param exception
	 * 		the exception to parse
	 */
	public static void appendExceptionMessages(JsonObjectBuilder builder, Throwable exception) {
		List<String> messages = collectMessages(exception);

		if (!messages.isEmpty()) {
			// return the first message
			builder.add("exception", messages.remove(0));
			// and all other messages
			if (!messages.isEmpty()) {
				JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
				messages.forEach(arrayBuilder::add);
				builder.add("other", arrayBuilder);
			}
		}
	}

	/**
	 * Collect all exception messages from the current {@link Throwable} and all it's caused/suppressed exception
	 * objects
	 *
	 * @param throwable
	 * 		the throwable to traverse
	 * @return the list of non null exception messages
	 */
	public static List<String> collectMessages(Throwable throwable) {
		List<String> messages = new LinkedList<>();
		collectMessages(messages, throwable);
		return messages;
	}

	private static void collectMessages(List<String> messages, Throwable throwable) {
		if (throwable == null) {
			return;
		}
		addNonNullValue(messages, throwable.getMessage());
		for (Throwable suppressed : throwable.getSuppressed()) {
			collectMessages(messages, suppressed);
		}
		collectMessages(messages, throwable.getCause());
	}

	/**
	 * Converts the provided error data to JSON.
	 *
	 * @param errorData
	 * 		- the error date for conversion
	 * @return the converted error data as a JSON
	 */
	public static JsonObject errorToJson(ErrorData errorData) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JSON.addIfNotNull(builder, CODE, errorData.getCode());
		JSON.addIfNotNull(builder, MESSAGE, errorData.getMessage());

		Map<String, Error> errors = errorData.getErrors();
		if (CollectionUtils.isNotEmpty(errors)) {
			builder.add(ERRORS, errorsToJson(errors));
		}

		return builder.build();
	}

	private static JsonObject errorsToJson(Map<String, Error> errors) {
		JsonObjectBuilder errorsBuilder = Json.createObjectBuilder();
		for (Map.Entry<String, Error> entry : errors.entrySet()) {
			JsonObjectBuilder errorBuilder = Json.createObjectBuilder();
			Error error = entry.getValue();
			JSON.addIfNotNull(errorBuilder, TYPE, error.getType());
			JSON.addIfNotNull(errorBuilder, MESSAGE, error.getMessage());
			JSON.addIfNotNull(errorBuilder, ERROR, error.getError());
			errorsBuilder.add(entry.getKey(), errorBuilder.build());
		}
		return errorsBuilder.build();
	}
}
