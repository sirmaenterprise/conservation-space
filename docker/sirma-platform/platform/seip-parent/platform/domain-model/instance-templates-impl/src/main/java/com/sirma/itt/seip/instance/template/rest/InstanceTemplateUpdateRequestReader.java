package com.sirma.itt.seip.instance.template.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * MessageBodyReader for parsing {@link InstanceTemplateUpdateRequest}.
 *
 * @author Adrian Mitev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class InstanceTemplateUpdateRequestReader implements MessageBodyReader<InstanceTemplateUpdateRequest> {

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return InstanceTemplateUpdateRequest.class.equals(type);
	}

	@Override
	public InstanceTemplateUpdateRequest readFrom(Class<InstanceTemplateUpdateRequest> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {
		return JSON.readObject(entityStream, buildReqest());
	}

	private static Function<JsonObject, InstanceTemplateUpdateRequest> buildReqest() {
		return json -> {
			InstanceTemplateUpdateRequest request = new InstanceTemplateUpdateRequest();
			request.setTemplateInstance(json.getString("templateInstance", null));
			request.setInstance(json.getString("instance", null));
			return request;
		};
	}
}