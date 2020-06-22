package com.sirma.sep.content.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.sep.content.ContentInfo;

/**
 * Writer for the mapper of the contentId and its contentInfo.
 * 
 * @author Nikolay Ch
 */
@Provider
public class ContentInfoMapperWriter extends AbstractMessageBodyWriter<Map<String, ContentInfo>> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		if (!Map.class.isAssignableFrom(type)) {
			return false;
		}

		Class<?> key = Types.getMapKeyType(genericType);
		Class<?> value = Types.getMapValueType(genericType);
		boolean assignableKey = key != null && String.class.isAssignableFrom(key);
		boolean assignableValue = value != null && ContentInfo.class.isAssignableFrom(value);
		return assignableKey && assignableValue;
	}

	@Override
	public void writeTo(Map<String, ContentInfo> t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException {
		try (JsonGenerator generator = Json.createGenerator(entityStream)) {
			generator.writeStartObject();
			for (Entry<String, ContentInfo> entry : t.entrySet()) {
				generator.writeStartObject(entry.getKey());
				ContentInfoJsonConverter.convertAndWriteToGenerator(generator, entry.getValue());
				generator.writeEnd();
			}
			generator.writeEnd();
			generator.flush();
		}
	}

}
