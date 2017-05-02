package com.sirma.itt.seip.instance.lock.rest;

import static com.sirma.itt.seip.rest.utils.JSON.readObject;
import static com.sirma.itt.seip.rest.utils.JsonKeys.TYPE;
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

import com.sirma.itt.seip.instance.lock.action.LockRequest;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Reads {@link LockRequest} for lock rest service.
 *
 * @author A. Kunchev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class LockRequestReader implements MessageBodyReader<LockRequest> {

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return LockRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public LockRequest readFrom(Class<LockRequest> clazz, Type type, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> headers, InputStream input) throws IOException {
		return readObject(input, toLockRequest(request));
	}

	private static Function<JsonObject, LockRequest> toLockRequest(RequestInfo request) {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The request payload is empty.");
			}

			LockRequest lockRequest = new LockRequest();
			lockRequest.setTargetId(PATH_ID.get(request));
			lockRequest.setLockType(json.getString(TYPE, ""));
			return lockRequest;
		};
	}

}
