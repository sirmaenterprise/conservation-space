package com.sirma.itt.seip.instance.actions.move;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Converts JSON object to {@link MoveActionRequest}. Reads input stream, creates JSON, from which an
 * {@link MoveActionRequest} is build . The request is used to execute the specific action.
 *
 * @author nvelkov
 */
@Provider
@Consumes(Versions.V2_JSON)
public class InstanceMoveBodyReader implements MessageBodyReader<MoveActionRequest> {

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return MoveActionRequest.class.isAssignableFrom(type);
	}

	@Override
	public MoveActionRequest readFrom(Class<MoveActionRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
		return JSON.readObject(entityStream, toActionRequest());
	}

	private Function<JsonObject, MoveActionRequest> toActionRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}

			return buildActionRequest(json, request);
		};
	}

	/**
	 * Build an action request from the provided json and request using the instance parser.
	 *
	 * @param json
	 *            the json containing the action request data
	 * @param request
	 *            the request
	 * @return the parsed move action request
	 */
	private static MoveActionRequest buildActionRequest(JsonObject json, RequestInfo request) {
		String targetInstanceId = PATH_ID.get(request);
		String destinationId = json.getString("destination");
		String userOperation = json.getString(JsonKeys.USER_OPERATION, ActionTypeConstants.MOVE);

		MoveActionRequest actionRequest = new MoveActionRequest();
		actionRequest.setTargetId(targetInstanceId);
		actionRequest.setDestinationId(destinationId);
		actionRequest.setUserOperation(userOperation);
		return actionRequest;
	}
}
