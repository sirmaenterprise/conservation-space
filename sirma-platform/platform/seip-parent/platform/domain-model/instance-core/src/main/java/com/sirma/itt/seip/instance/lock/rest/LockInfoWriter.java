package com.sirma.itt.seip.instance.lock.rest;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Converts {@link LockInfo} to {@link JsonObject}.
 *
 * @author A. Kunchev
 */
@Provider
@Produces(Versions.V2_JSON)
public class LockInfoWriter extends AbstractMessageBodyWriter<LockInfo> {
	@Inject
	private TypeConverter typeConverter;

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return LockInfo.class.isAssignableFrom(clazz);
	}

	@Override
	public void writeTo(LockInfo lockInfo, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> headers, OutputStream output) throws IOException {
		Objects.requireNonNull(lockInfo);
		try (OutputStream bufferedOutput = new BufferedOutputStream(output)) {
			// calls the converter defined by LockInfoConverterProvider
			String value = typeConverter.convert(String.class, lockInfo);
			bufferedOutput.write(value.getBytes(StandardCharsets.UTF_8));
			bufferedOutput.flush();
		}
	}
}
