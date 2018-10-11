package com.sirmaenterprise.sep.annotations;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.github.jsonldjava.utils.JsonUtils;
import com.sirma.itt.seip.annotations.rest.AnnotationWriter;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * JSON body writer for {@link DiscussionsResponse}.
 *
 * @author Vilizar Tsonev
 */
@Provider
@Produces(Versions.V2_JSON)
public class DiscussionsResponseBodyWriter extends AbstractMessageBodyWriter<DiscussionsResponse> {

	@Inject
	private AnnotationWriter annotationWriter;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return DiscussionsResponse.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(DiscussionsResponse discussionsResponse, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException {
		Object convertedAnnotaions = annotationWriter.convert(discussionsResponse.getAnnotations());
		Map<String, Object> response = new HashMap<>();
		response.put("annotations", convertedAnnotaions);
		response.put("instanceHeaders", discussionsResponse.getTargetInstanceHeaders());

		try (Writer output = new OutputStreamWriter(entityStream)) {
			JsonUtils.write(output, response);
		}
	}

}
