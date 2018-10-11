package com.sirma.itt.seip.annotations.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.eclipse.rdf4j.rio.RDFFormat;

import com.sirma.itt.seip.annotations.model.Annotation;

/**
 * Body writer for {@link Annotation} classes in REST services. The class uses the existing content and adds all other
 * properties to the original body before returning the response.
 *
 * @author BBonev
 */
@Provider
public class AnnotationBodyWriter implements MessageBodyWriter<Annotation> {

	private static final MediaType JSON_LD = MediaType.valueOf(RDFFormat.JSONLD.getDefaultMIMEType());

	@Inject
	private AnnotationWriter writer;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, java.lang.annotation.Annotation[] annotations,
			MediaType mediaType) {
		return Annotation.class.isAssignableFrom(type)
				&& (JSON_LD.isCompatible(mediaType) || MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType));
	}

	@Override
	public long getSize(Annotation t, Class<?> type, Type genericType, java.lang.annotation.Annotation[] annotations,
			MediaType mediaType) {
		// Deprecated by JAX-RS 2.0
		return -1;
	}

	@Override
	public void writeTo(Annotation toWrite, Class<?> type, Type genericType,
			java.lang.annotation.Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

		writer.writeTo(toWrite, entityStream, true);
	}
}
