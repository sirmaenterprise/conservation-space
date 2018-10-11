package com.sirma.itt.seip.annotations.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.annotations.model.Annotation;

/**
 * Body writer for collection of {@link Annotation}s in REST services. The actual writer used is
 * {@link AnnotationWriter}.
 *
 * @author BBonev
 */
@Provider
public class AnnotationCollectionWriter implements MessageBodyWriter<Collection<Annotation>> {

	private static final MediaType JSON_LD = MediaType.valueOf(RDFFormat.JSONLD.getDefaultMIMEType());

	@Inject
	private AnnotationWriter writer;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, java.lang.annotation.Annotation[] annotations,
			MediaType mediaType) {
		return Collection.class.isAssignableFrom(type) && type != null
				&& Types.getCollectionBaseType(type, genericType) != null
				&& Annotation.class.isAssignableFrom(Types.getCollectionBaseType(type, genericType))
				&& (JSON_LD.isCompatible(mediaType) || MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType));
	}

	@Override
	public long getSize(Collection<Annotation> t, Class<?> type, Type genericType,
			java.lang.annotation.Annotation[] annotations, MediaType mediaType) {
		// Deprecated by JAX-RS 2.0
		return -1;
	}

	@Override
	public void writeTo(Collection<Annotation> toWrite, Class<?> type, Type genericType,
			java.lang.annotation.Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

		writer.writeTo(toWrite, entityStream, true);
	}
}
