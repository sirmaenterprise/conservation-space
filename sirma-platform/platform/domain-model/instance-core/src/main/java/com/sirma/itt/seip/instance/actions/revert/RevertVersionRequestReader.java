package com.sirma.itt.seip.instance.actions.revert;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
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

import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Reads the request input from the {@link RevertVersionRestService} and converts it into {@link RevertVersionRequest}
 * object, used to execute the operation.
 *
 * @author A. Kunchev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class RevertVersionRequestReader implements MessageBodyReader<RevertVersionRequest> {

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return RevertVersionRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public RevertVersionRequest readFrom(Class<RevertVersionRequest> clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> headers, InputStream stream) throws IOException {
		return JSON.readObject(stream, toRevertRequest(request));
	}

	private static Function<JsonObject, RevertVersionRequest> toRevertRequest(RequestInfo info) {
		return json -> {
			RevertVersionRequest request = new RevertVersionRequest();
			request.setTargetId(PATH_ID.get(info));
			// get user operation when passed or use default if not
			request.setUserOperation(json.getString(JsonKeys.USER_OPERATION, request.getOperation()));
			return request;
		};
	}

}
