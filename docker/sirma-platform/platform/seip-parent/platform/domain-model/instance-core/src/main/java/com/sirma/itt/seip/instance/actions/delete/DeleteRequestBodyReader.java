package com.sirma.itt.seip.instance.actions.delete;

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

import com.sirma.itt.seip.instance.actions.delete.DeleteRequest;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Converts a http entity body into a {@link DeleteRequest}.
 *
 * @author Adrian Mitev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class DeleteRequestBodyReader implements MessageBodyReader<DeleteRequest> {

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return DeleteRequest.class.isAssignableFrom(type);
	}

	@Override
	public DeleteRequest readFrom(Class<DeleteRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException {

		return JSON.readObject(entityStream, toActionRequest());
	}

	private Function<JsonObject, DeleteRequest> toActionRequest() {
		return json -> {
			DeleteRequest deleteRequest = new DeleteRequest();
			deleteRequest.setTargetId(PATH_ID.get(request));
			deleteRequest.setUserOperation(json.getString(JsonKeys.USER_OPERATION, DeleteRequest.DELETE_OPERATION));
			return deleteRequest;
		};
	}
}
