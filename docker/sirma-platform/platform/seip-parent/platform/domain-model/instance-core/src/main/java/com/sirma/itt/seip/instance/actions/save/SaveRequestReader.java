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
 * Reader for the {@link SaveRequest}.
 *
 * @author nvelkov
 */
@Provider
@Consumes(Versions.V2_JSON)
public class SaveRequestReader implements MessageBodyReader<SaveRequest> {

	@BeanParam
	private RequestInfo request;

	@Inject
	private InstanceResourceParser instanceResourceParser;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SaveRequest.class.isAssignableFrom(type);
	}

	@Override
	public SaveRequest readFrom(Class<SaveRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException {
		return JSON.readObject(entityStream, toSaveRequest());
	}

	private Function<JsonObject, SaveRequest> toSaveRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}

			return buildActionRequest(json);
		};
	}

	private SaveRequest buildActionRequest(JsonObject json) {
		String targetId = PATH_ID.get(request);
		Instance instance = instanceResourceParser.toInstance(json.getJsonObject(TARGET_INSTANCE), targetId);
		return SaveRequest.buildSaveRequest(instance, new Date(),
				json.getString(JsonKeys.USER_OPERATION, ActionTypeConstants.CREATE));
	}
}