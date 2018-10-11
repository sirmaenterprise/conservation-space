package com.sirma.itt.seip.annotations.rest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.parser.AnnotationParser;

/**
 * Message body reader for multiple {@link Annotation}s parsing
 *
 * @author BBonev
 */
@Provider
public class AnnotationCollectionReader implements MessageBodyReader<Collection<Annotation>> {

	private static final MediaType JSON_LD = MediaType.valueOf(RDFFormat.JSONLD.getDefaultMIMEType());

	@Override
	public boolean isReadable(Class<?> type, Type genericType, java.lang.annotation.Annotation[] annotations,
			MediaType mediaType) {
		return Collection.class.isAssignableFrom(type)
				&& Annotation.class.isAssignableFrom(Types.getCollectionBaseType(type, genericType))
				&& (JSON_LD.isCompatible(mediaType) || MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType));
	}

	@Override
	public Collection<Annotation> readFrom(Class<Collection<Annotation>> type, Type genericType,
			java.lang.annotation.Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {

		return AnnotationParser.parse(entityStream);
	}

}
