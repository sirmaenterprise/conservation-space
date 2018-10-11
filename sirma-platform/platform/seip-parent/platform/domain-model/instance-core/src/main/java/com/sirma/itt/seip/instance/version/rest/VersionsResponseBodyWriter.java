package com.sirma.itt.seip.instance.version.rest;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.DESCRIPTION;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.HEADER_COMPACT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MODIFIED_ON;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.VERSION;
import static com.sirma.itt.seip.instance.version.VersionProperties.HAS_VIEW_CONTENT;
import static com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer.onlyProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.version.VersionsResponse;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Converts {@link VersionsResponse} to {@link JsonObject}. Uses {@link InstanceToJsonSerializer} to serialize the
 * result version instances.
 *
 * @author A. Kunchev
 */
@Provider
@Produces(Versions.V2_JSON)
public class VersionsResponseBodyWriter extends AbstractMessageBodyWriter<VersionsResponse> {

	private static final String TOTAL_VERSIONS_COUNT = "versionsCount";

	private static final String VERSIONS = "versions";

	/**
	 * Properties that should be send to the web, where modified by represents the creator of the version and modified
	 * on the date, when the version is created.
	 */
	private static final Set<String> VERSIONS_PROPERTIES = new HashSet<>(
			Arrays.asList(HEADER_COMPACT, VERSION, MODIFIED_BY, MODIFIED_ON, DESCRIPTION, HAS_VIEW_CONTENT));

	@Inject
	private InstanceToJsonSerializer instanceToJsonSerializer;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return VersionsResponse.class.isAssignableFrom(clazz);
	}

	@Override
	public void writeTo(VersionsResponse response, Class<?> clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream output) throws IOException {
		try (JsonGenerator generator = Json.createGenerator(output)) {
			generator
					.writeStartObject()
						.write(TOTAL_VERSIONS_COUNT, response.getTotalCount())
						.writeStartArray(VERSIONS);
			Collection<Instance> results = response.getResults();
			instanceLoadDecorator.decorateResult(results);
			instanceToJsonSerializer.serialize(results, onlyProperties(VERSIONS_PROPERTIES), generator);
			generator.writeEnd().writeEnd().flush();
		}
	}

}
