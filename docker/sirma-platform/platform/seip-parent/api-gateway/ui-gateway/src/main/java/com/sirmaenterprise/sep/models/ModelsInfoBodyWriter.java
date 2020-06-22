package com.sirmaenterprise.sep.models;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Body writer for {@link ModelsInfo} instances
 *
 * @author BBonev
 */
@Provider
@Produces(Versions.V2_JSON)
public class ModelsInfoBodyWriter extends AbstractMessageBodyWriter<ModelsInfo> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ModelsInfo.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(ModelsInfo info, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

		try (JsonGenerator generator = Json.createGenerator(entityStream)) {
			generator.writeStartObject();
			if (StringUtils.isNotBlank(info.getErrorMessage())) {
				generator.write("errorMessage", info.getErrorMessage());
			}
			generator.writeStartArray("models");

			for (ModelInfo modelInfo : info) {
				write(generator, modelInfo);
			}

			generator.writeEnd();
			generator.writeEnd();
			generator.flush();

		}
	}

	private static void write(JsonGenerator generator, ModelInfo modelInfo) {
		generator.writeStartObject();

		generator.write("id", modelInfo.getId());
		generator.write("label", modelInfo.getLabel());
		generator.write("type", modelInfo.getType());
		if (modelInfo.isClass()) {
			generator.write("creatable", modelInfo.isCreatable());
			generator.write("uploadable", modelInfo.isUploadable());
		}
		JSON.addIfNotNull(generator, "parent", modelInfo.getParentId());
		if (modelInfo.isDefault()) {
			generator.write("default", true);
		}

		generator.writeEnd();
	}
}
