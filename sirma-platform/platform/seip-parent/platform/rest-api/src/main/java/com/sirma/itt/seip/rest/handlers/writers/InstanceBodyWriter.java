package com.sirma.itt.seip.rest.handlers.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * {@link Instance} to {@link JsonObject} serializer.
 *
 * @author yasko
 */
@Provider
@Produces(Versions.V2_JSON)
public class InstanceBodyWriter extends AbstractMessageBodyWriter<Instance> {

	@Inject
	private InstanceToJsonSerializer instanceSerializer;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@BeanParam
	private RequestInfo info;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return Instance.class.isAssignableFrom(type);
	}

	/**
	 * Converts an {@link Instance} to json using the object's definition.
	 */
	@Override
	public void writeTo(Instance instance, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream out) throws IOException {
		try (JsonGenerator generator = Json.createGenerator(out)) {
			instanceLoadDecorator.decorateInstance(instance);
			instanceSerializer.serialize(instance,
					InstanceToJsonSerializer.allOrGivenProperties(RequestParams.PROPERTY_NAMES.get(info)), generator);
			generator.flush();
		}
	}
}
