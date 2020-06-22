package com.sirma.itt.seip.instance.draft.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.instance.draft.DraftInstance;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Converts single {@link DraftInstance} to {@link JsonObject}.
 *
 * @author A. Kunchev
 */
@Provider
@Produces(Versions.V2_JSON)
public class DraftInstanceBodyWriter extends AbstractMessageBodyWriter<DraftInstance> {

	private static final String DRAFT_CONTENT = "draftContentId";

	private static final String DRAFT_INSTANCE_ID = "draftInstanceId";

	private static final String DRAFT_CREATOR = "draftCreator";

	private static final String DRAFT_CREATED_ON = "draftCreatedOn";

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return DraftInstance.class.isAssignableFrom(clazz);
	}

	@Override
	public void writeTo(DraftInstance draft, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> headers, OutputStream output) throws IOException {
		try (JsonGenerator generator = Json.createGenerator(output)) {
			generator.writeStartObject();
			serialize(draft, generator);
			generator.writeEnd().flush();
		}
	}

	private static void serialize(DraftInstance draftInstance, JsonGenerator generator) {
		if (draftInstance == null) {
			return;
		}

		generator
				.write(DRAFT_INSTANCE_ID, draftInstance.getInstanceId())
					.write(DRAFT_CREATOR, draftInstance.getCreator())
					.write(DRAFT_CONTENT, draftInstance.getDraftContentId())
					.write(DRAFT_CREATED_ON, ISO8601DateFormat.format(draftInstance.getCreatedOn()));
	}

}
