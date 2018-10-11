package com.sirma.sep.content.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.sep.content.ContentInfo;

/**
 * Message body provider for {@link ContentInfo} instances
 *
 * @author BBonev
 */
@Provider
public class ContentInfoWriter extends AbstractMessageBodyWriter<ContentInfo> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ContentInfo.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(ContentInfo info, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException {

		// if the info reports non exist we check for the content id as well if the id is not present we have missing
		// content, otherwise we have a content that is being uploaded asynchronously
		if (!info.exists() && info.getContentId() == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		try (JsonGenerator generator = Json.createGenerator(entityStream)) {
			generator.writeStartObject();
			ContentInfoJsonConverter.convertAndWriteToGenerator(generator, info);
			generator.writeEnd();
			generator.flush();
		}
	}

}
