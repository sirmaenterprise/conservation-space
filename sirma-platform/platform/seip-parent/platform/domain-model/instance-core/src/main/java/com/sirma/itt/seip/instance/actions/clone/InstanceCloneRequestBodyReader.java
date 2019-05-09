package com.sirma.itt.seip.instance.actions.clone;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

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
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Reads the request input from the http requests passed to {@link InstanceCloneRestService} and converts it in to
 * {@link InstanceCloneRequest} object used to execute the clone operation.
 *
 * @author Ivo Rusev on 14.12.2016
 */
@Provider
@Consumes(Versions.V2_JSON)
public class InstanceCloneRequestBodyReader implements MessageBodyReader<InstanceCloneRequest> {

	@BeanParam
	private RequestInfo request;

	@Inject
	private InstanceResourceParser instanceResourceParser;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return InstanceCloneRequest.class.isAssignableFrom(type);
	}

	@Override
	public InstanceCloneRequest readFrom(Class<InstanceCloneRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
		return JSON.readObject(entityStream, toActionRequest());
	}

	private Function<JsonObject, InstanceCloneRequest> toActionRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}

			String userOperation = json.getString(JsonKeys.USER_OPERATION, ActionTypeConstants.CLONE);
			// In the url is stored the existing instance id, no the cloned one.
			String targetId = PATH_ID.get(request);
			// Coverts the json to instance object. The second parameter is empty because we need instance with no id.
			// One will be generated when the instance goes to persist logic.
			Instance instanceToClone = instanceResourceParser.toInstance(json, "");

			InstanceCloneRequest actionRequest = new InstanceCloneRequest();
			actionRequest.setTargetId(targetId);
			actionRequest.setUserOperation(userOperation);
			actionRequest.setClonedInstance(instanceToClone);
			actionRequest.setContextPath(JSON.getStringArray(json, JsonKeys.CONTEXT_PATH));
			actionRequest.setTargetReference(instanceToClone.toReference());
			return actionRequest;
		};
	}
}
