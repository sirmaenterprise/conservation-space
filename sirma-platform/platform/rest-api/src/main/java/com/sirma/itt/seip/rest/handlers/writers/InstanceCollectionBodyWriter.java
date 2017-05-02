package com.sirma.itt.seip.rest.handlers.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * Serializes {@link Collection} of {@link Instance} to {@link JsonArray}.
 *
 * @author Mihail Radkov
 */
@Provider
@Produces(Versions.V2_JSON)
public class InstanceCollectionBodyWriter extends AbstractMessageBodyWriter<Collection<Instance>> {

	@Inject
	private InstanceToJsonSerializer instanceSerializer;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@BeanParam
	private RequestInfo info;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		Class<?> collectionType = Types.getCollectionBaseType(type, genericType);
		boolean isAssignable = collectionType != null ? Instance.class.isAssignableFrom(collectionType) : false;
		return Collection.class.isAssignableFrom(type) && isAssignable;
	}

	@Override
	public void writeTo(Collection<Instance> instances, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException {
		try (JsonGenerator generator = Json.createGenerator(out)) {
			generator.writeStartArray();
			instanceLoadDecorator.decorateResult(instances);
			instanceSerializer.serialize(instances,
					InstanceToJsonSerializer.allOrGivenProperties(RequestParams.PROPERTY_NAMES.get(info)), generator);
			generator.writeEnd().flush();
		}
	}
}
