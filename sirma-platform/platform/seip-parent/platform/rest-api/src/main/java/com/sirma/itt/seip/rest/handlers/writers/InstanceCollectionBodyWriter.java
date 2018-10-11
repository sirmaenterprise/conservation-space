package com.sirma.itt.seip.rest.handlers.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.rest.resources.instances.InstancesLoadResponse;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Serializes loaded instance in {@link InstancesLoadResponse} to {@link JsonArray}.
 *
 * @author Mihail Radkov
 */
@Provider
@Produces(Versions.V2_JSON)
public class InstanceCollectionBodyWriter extends AbstractMessageBodyWriter<InstancesLoadResponse> {

	@Inject
	private InstanceToJsonSerializer instanceSerializer;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return InstancesLoadResponse.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(InstancesLoadResponse response, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException {
		try (JsonGenerator generator = Json.createGenerator(out)) {
			generator.writeStartArray();
			Collection<Instance> instances = response.getInstances();
			instanceLoadDecorator.decorateResult(instances);
			instanceSerializer.serialize(instances, response.getPropertiesFilter(), generator);
			generator.writeEnd().flush();
		}
	}
}
