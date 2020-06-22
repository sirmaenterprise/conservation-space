package com.sirma.itt.seip.definition.rest.writers;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.rest.DefinitionModelObject;
import com.sirma.itt.seip.definition.rest.DefinitionModelToJsonSerializer;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Writer for map, where the key is the instance id and the value is actual definition object (
 * {@link DefinitionModelObject}) that is should be converted to JSON.
 *
 * @see DefinitionModelToJsonSerializer#serialize
 * @author A. Kunchev
 */
@Provider
@Produces(Versions.V2_JSON)
public class DefinitionModelMapBodyWriter extends AbstractMessageBodyWriter<Map<String, DefinitionModelObject>> {

	@Inject
	private DefinitionModelToJsonSerializer serializer;

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		if (!Map.class.isAssignableFrom(clazz)) {
			return false;
		}

		Class<?> key = Types.getMapKeyType(type);
		Class<?> value = Types.getMapValueType(type);
		boolean assignableKey = key != null ? String.class.isAssignableFrom(key) : false;
		boolean assignableValue = value != null ? DefinitionModelObject.class.isAssignableFrom(value) : false;
		return assignableKey && assignableValue;
	}

	@Override
	public void writeTo(Map<String, DefinitionModelObject> objects, Class<?> clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream outStream) throws IOException {
		if (CollectionUtils.isEmpty(objects)) {
			throw new ResourceException(NOT_FOUND, "There are no request object.");
		}

		try (JsonGenerator generator = Json.createGenerator(outStream)) {
			generator.writeStartObject();
			writeResponse(objects, generator);
			generator.writeEnd().flush();
		}
	}

	private void writeResponse(Map<String, DefinitionModelObject> objects, JsonGenerator generator) {
		for (Entry<String, DefinitionModelObject> entry : objects.entrySet()) {
			DefinitionModelObject value = entry.getValue();
			DefinitionModel model = value.getDefinitionModel();

			if (model == null) {
				continue;
			}

			generator.writeStartObject(entry.getKey());
			Collection<String> requestedFields = value.getRequestedFields();
			if (isEmpty(requestedFields)) {
				serializer.serialize(model, value.getInstance(), value.getOperation(), generator);
			} else {
				serializer.serialize(model, value.getInstance(), value.getOperation(),
						getProjection(requestedFields, model), generator);
			}
			generator.writeEnd();
		}
	}

	private static Set<String> getProjection(Collection<String> requestedFields, DefinitionModel model) {
		return model
				.getFieldsAndDependencies(requestedFields)
					.map(PropertyDefinition::getName)
					.collect(Collectors.toSet());
	}

}
