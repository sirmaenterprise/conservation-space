package com.sirma.itt.seip.annotations.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.eclipse.rdf4j.rio.RDFFormat;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.parser.AnnotationParser;

/**
 * Message body reader for single {@link Annotation} parsing
 *
 * @author BBonev
 */
@Provider
public class AnnotationReader implements MessageBodyReader<Annotation> {

	private static final MediaType JSON_LD = MediaType.valueOf(RDFFormat.JSONLD.getDefaultMIMEType());

	@Override
	public boolean isReadable(Class<?> type, Type genericType, java.lang.annotation.Annotation[] annotations,
			MediaType mediaType) {
		return Annotation.class.isAssignableFrom(type)
				&& (JSON_LD.isCompatible(mediaType) || MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType));
	}

	@Override
	public Annotation readFrom(Class<Annotation> type, Type genericType, java.lang.annotation.Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
					throws IOException {

		return AnnotationParser.parseSingle(entityStream);
	}

}
