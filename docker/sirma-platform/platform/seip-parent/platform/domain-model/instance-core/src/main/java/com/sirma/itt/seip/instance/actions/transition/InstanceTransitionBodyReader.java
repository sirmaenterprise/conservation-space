package com.sirma.itt.seip.instance.actions.transition;

import static com.sirma.itt.seip.rest.utils.JsonKeys.TARGET_INSTANCE;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Function;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Converts JSON object to {@link TransitionActionRequest}. Reads input stream, creates JSON, from which is build
 * {@link TransitionActionRequest} object used to execute specific action.
 *
 * @author A. Kunchev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class InstanceTransitionBodyReader implements MessageBodyReader<TransitionActionRequest> {

	@BeanParam
	private RequestInfo request;

	@Inject
	private InstanceResourceParser instanceResourceParser;

	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return TransitionActionRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public TransitionActionRequest readFrom(Class<TransitionActionRequest> clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> headers, InputStream stream) throws IOException {
		return JSON.readObject(stream, toImmediateActionRequest());
	}

	private Function<JsonObject, TransitionActionRequest> toImmediateActionRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}

			return buildActionRequest(json, request, instanceResourceParser);
		};
	}

	private static TransitionActionRequest buildActionRequest(JsonObject json, RequestInfo request,
			InstanceResourceParser instanceParser) {
		TransitionActionRequest actionRequest = new TransitionActionRequest();
		String userOperation = json.getString(JsonKeys.USER_OPERATION, null);
		String targetId = PATH_ID.get(request);
		JsonObject instanceJson = json.getJsonObject(TARGET_INSTANCE);
		Instance instance = instanceParser.toInstance(instanceJson, targetId);

		if (instance == null) {
			throw new ResourceException(Status.NOT_FOUND,
					new ErrorData().setMessage("Could not find instance with id: " + targetId), null);
		}

		actionRequest.setTargetInstance(instance);
		actionRequest.setTargetReference(instance.toReference());
		actionRequest.setUserOperation(userOperation);
		actionRequest.setTargetId(targetId);
		actionRequest.setContextPath(JSON.getStringArray(json, JsonKeys.CONTEXT_PATH));
		return actionRequest;
	}

}
