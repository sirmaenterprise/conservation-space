package com.sirma.itt.seip.definition.rest.writers;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.definition.rest.DefinitionModelObject;
import com.sirma.itt.seip.definition.rest.DefinitionModelToJsonSerializer;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Writer for {@link DefinitionModelObject}. Converts the object to JSON.
 *
 * @see DefinitionModelToJsonSerializer#serialize
 * @author A. Kunchev
 */
@Provider
@Produces(Versions.V2_JSON)
public class DefinitionModelBodyWriter extends AbstractMessageBodyWriter<DefinitionModelObject> {

	@Inject
	private DefinitionModelToJsonSerializer serializer;

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return DefinitionModelObject.class.isAssignableFrom(clazz);
	}

	@Override
	public void writeTo(DefinitionModelObject object, Class<?> clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream outStream) throws IOException {
		if (object == null || object.getDefinitionModel() == null) {
			throw new ResourceException(NOT_FOUND, "Failed to find definition model for the requested instance.");
		}

		try (JsonGenerator generator = Json.createGenerator(outStream)) {
			generator.writeStartObject();
			DefinitionModel model = object.getDefinitionModel();
			Collection<String> requestedFields = object.getRequestedFields();

			if (isEmpty(requestedFields)) {
				serializer.serialize(object.getDefinitionModel(), object.getInstance(), object.getOperation(),
						generator);
			} else {
				Set<String> projection = model
						.getFieldsAndDependencies(requestedFields)
							.map(PropertyDefinition::getName)
							.collect(Collectors.toSet());

				serializer.serialize(object.getDefinitionModel(), object.getInstance(), object.getOperation(),
						projection, generator);
			}
			generator.writeEnd().flush();
		}
	}

}
