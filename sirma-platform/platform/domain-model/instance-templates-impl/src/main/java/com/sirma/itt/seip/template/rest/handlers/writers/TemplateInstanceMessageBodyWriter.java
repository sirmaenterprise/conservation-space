package com.sirma.itt.seip.template.rest.handlers.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.template.TemplateInstance;

/**
 * {@link MessageBodyWriter} for converting {@link TemplateInstance} to {@link Versions#V2_JSON}.
 * 
 * @author yasko
 */
@Provider
@Produces(Versions.V2_JSON)
public class TemplateInstanceMessageBodyWriter extends AbstractMessageBodyWriter<TemplateInstance> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return TemplateInstance.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(TemplateInstance instance, Class<?> type, Type generic, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream out)
					throws IOException, WebApplicationException {
		
		try (JsonGenerator generator = Json.createGenerator(out)) {
			TemplateInstanceToJson.writeJson(generator, instance);
			generator.flush();
		}
	}
}
