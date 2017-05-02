package com.sirma.itt.seip.instance.actions.save;

import static com.sirma.itt.seip.rest.utils.JsonKeys.TARGET_INSTANCE;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;
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
 * Reader for the {@link CreateOrUpdateRequest}.
 *
 * @author nvelkov
 */
@Provider
@Consumes(Versions.V2_JSON)
public class CreateOrUpdateRequestReader implements MessageBodyReader<CreateOrUpdateRequest> {

	@BeanParam
	private RequestInfo request;

	@Inject
	private InstanceResourceParser instanceResourceParser;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return CreateOrUpdateRequest.class.isAssignableFrom(type);
	}

	@Override
	public CreateOrUpdateRequest readFrom(Class<CreateOrUpdateRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException {
		return JSON.readObject(entityStream, toSaveRequest());
	}

	private Function<JsonObject, CreateOrUpdateRequest> toSaveRequest() {
		return json ->
			{
				if (json.isEmpty()) {
					throw new BadRequestException("The JSON object is empty");
				}

				return buildActionRequest(json);
			};
	}

	private CreateOrUpdateRequest buildActionRequest(JsonObject json) {
		CreateOrUpdateRequest saveRequest = new CreateOrUpdateRequest();
		String userOperation = json.getString(JsonKeys.USER_OPERATION, ActionTypeConstants.CREATE);
		saveRequest.setUserOperation(userOperation);

		String targetId = PATH_ID.get(request);
		JsonObject instanceJson = json.getJsonObject(TARGET_INSTANCE);
		Instance instance = instanceResourceParser.toInstance(instanceJson, targetId);

		saveRequest.setTargetId(targetId);
		saveRequest.setTarget(instance);
		saveRequest.setTargetReference(instance.toReference());
		saveRequest.setVersionCreatedOn(new Date());

		return saveRequest;
	}
}
