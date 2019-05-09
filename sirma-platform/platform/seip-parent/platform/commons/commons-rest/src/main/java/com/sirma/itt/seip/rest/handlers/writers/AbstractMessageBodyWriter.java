package com.sirma.itt.seip.rest.handlers.writers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * Abstract implementation of a {@link MessageBodyWriter}.
 * @author yasko
 *
 * @param <T> Type that the writer is responsible to write to the client.
 */
public abstract class AbstractMessageBodyWriter<T> implements MessageBodyWriter<T> {

	@Override
	public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		// Deprecated by JAX-RS 2.0
		return -1;
	}
}
