package com.sirma.itt.seip.instance.lock.rest;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY;
import static com.sirma.itt.seip.rest.utils.JSON.addIfNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Converts {@link LockInfo} to {@link JsonObject}.
 *
 * @author A. Kunchev
 */
@Provider
@Produces(Versions.V2_JSON)
public class LockInfoWriter extends AbstractMessageBodyWriter<LockInfo> {

	@Inject
	private ResourceService resourceService;

	@Inject
	private InstanceToJsonSerializer instanceSerializer;

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return LockInfo.class.isAssignableFrom(clazz);
	}

	@Override
	public void writeTo(LockInfo lockInfo, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> headers, OutputStream output) throws IOException {
		Objects.requireNonNull(lockInfo);
		try (JsonGenerator generator = Json.createGenerator(output)) {
			generator.writeStartObject().write("isLocked", lockInfo.isLocked());
			addIfNotNull(generator, "lockInfo", lockInfo.getLockInfo());
			addIfNotNull(generator, "lockOn", ISO8601DateFormat.format(lockInfo.getLockedOn()));
			addLockedByIfAvailable(generator, lockInfo);
			generator.writeEnd().flush();
		}
	}

	private void addLockedByIfAvailable(JsonGenerator generator, LockInfo info) {
		Resource user = resourceService.getResource(info.getLockedBy());
		if (user != null) {
			instanceSerializer.serialize(LOCKED_BY, user, generator);
		}
	}

}
