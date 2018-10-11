package com.sirmaenterprise.sep.bpm.camunda.transitions.model.io;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.rest.DefinitionModelObject;
import com.sirma.itt.seip.definition.rest.DefinitionModelToJsonSerializer;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirmaenterprise.sep.bpm.camunda.transitions.model.BPMDefinitionModelObject;

/**
 * {@link MessageBodyWriter} for bpm transition model containing 1..* related instances and their corresponding models
 * 
 * @author bbanchev
 */
@Provider
@Produces(Versions.V2_JSON)
public class BPMDefinitionModelMapBodyWriter extends AbstractMessageBodyWriter<Map<String, BPMDefinitionModelObject>> {

	@Inject
	private DefinitionModelToJsonSerializer modelSerializer;

	@Inject
	private InstanceToJsonSerializer instanceSerializer;

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		if (!Map.class.isAssignableFrom(clazz)) {
			return false;
		}
		Class<?> key = Types.getMapKeyType(type);
		Class<?> value = Types.getMapValueType(type);
		boolean assignableKey = key != null ? String.class.isAssignableFrom(key) : false;
		boolean assignableValue = value != null ? BPMDefinitionModelObject.class.isAssignableFrom(value) : false;
		return assignableKey && assignableValue;
	}

	@Override
	public void writeTo(Map<String, BPMDefinitionModelObject> objects, Class<?> clazz, Type type,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headers,
			OutputStream outStream) throws IOException {
		if (CollectionUtils.isEmpty(objects)) {
			throw new ResourceException(NOT_FOUND,
					new ErrorData().setMessage("Provided model does not contain any data."), null);
		}
		try (JsonGenerator generator = Json.createGenerator(outStream)) {
			generator.writeStartObject();
			writeResponse(objects, generator);
			generator.writeEnd().flush();
		}

	}

	private void writeResponse(Map<String, BPMDefinitionModelObject> objects, JsonGenerator generator) {
		for (Entry<String, BPMDefinitionModelObject> entry : objects.entrySet()) {
			DefinitionModelObject model = entry.getValue().getModel();
			// nest instance model to provide details for non persisted instances as well
			if (model != null) {
				generator.writeStartObject(entry.getKey());
				generator.writeStartObject("model");
				modelSerializer.serialize(model.getDefinitionModel(), model.getInstance(), model.getOperation(),
						generator);
				generator.writeEnd();
				instanceSerializer.serialize(model.getInstance(), generator, "instance");
				generator.writeEnd();
			}
		}
	}

}
