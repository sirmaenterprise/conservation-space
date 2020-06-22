package com.sirma.itt.seip.template.actions;

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
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Message body reader for {@link ActivateTemplateActionRequest}
 *
 * @author Viliar Tsonev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class ActivateTemplateActionRequestReader implements MessageBodyReader<ActivateTemplateActionRequest> {

	@BeanParam
	private RequestInfo info;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ActivateTemplateActionRequest.class.equals(type);
	}

	@Override
	public ActivateTemplateActionRequest readFrom(Class<ActivateTemplateActionRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException {
		return JSON.readObject(entityStream, buildReqest());
	}

	private Function<JsonObject, ActivateTemplateActionRequest> buildReqest() {
		return json -> {
			ActivateTemplateActionRequest request = new ActivateTemplateActionRequest();
			request.setTargetId(RequestParams.PATH_ID.get(info));
			request.setUserOperation(json.getString(JsonKeys.USER_OPERATION, ActivateTemplateActionRequest.OPERATION_NAME));
			return request;
		};
	}

}
