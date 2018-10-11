package com.sirma.itt.seip.search.rest.handlers.writers;

import static com.sirma.itt.seip.rest.utils.JsonKeys.AGGREGATED;
import static com.sirma.itt.seip.rest.utils.JsonKeys.HIGHLIGHT;
import static com.sirma.itt.seip.rest.utils.JsonKeys.MESSAGE;
import static com.sirma.itt.seip.rest.utils.JsonKeys.PAGE;
import static com.sirma.itt.seip.rest.utils.JsonKeys.RESULT_SIZE;
import static com.sirma.itt.seip.rest.utils.JsonKeys.VALUES;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.BeanParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Converter for the search results to JSON.
 *
 * @author yasko
 */
@Provider
@Produces(Versions.V2_JSON)
public class SearchArgumentsWriter extends AbstractMessageBodyWriter<SearchArguments<Instance>> {

	@Inject
	private InstanceToJsonSerializer serializer;

	@BeanParam
	private RequestInfo info;

	@Override
	public boolean isWriteable(Class<?> type, Type generic, Annotation[] annotations, MediaType media) {
		if (!ReflectionUtils.isTypeArgument(generic, Instance.class)) {
			return false;
		}
		return SearchArguments.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(SearchArguments<Instance> args, Class<?> type, Type generic, Annotation[] annotations,
			MediaType media, MultivaluedMap<String, Object> headers, OutputStream out) throws IOException {

		try (JsonGenerator generator = Json.createGenerator(out)) {
			serialize(args, generator);
			out.flush();
		}
	}

	private void serialize(SearchArguments<Instance> args, JsonGenerator generator) {
		generator.writeStartObject();
		generator.write(RESULT_SIZE, args.getTotalItems());
		generator.write(PAGE, args.getPageNumber());

		if (!args.getHighlight().isEmpty()) {
			generator.writeStartArray(HIGHLIGHT);
			args.getHighlight().forEach(generator::write);
			generator.writeEnd();
		}

		if (args.getSearchError() != null) {
			Exception searchError = args.getSearchError();
			generator.write(MESSAGE, Objects.toString(searchError.getMessage(), searchError.toString()));
		} else {
			generator.write(MESSAGE, JsonValue.NULL);
		}

		generator.writeStartArray(VALUES);
		if (args.getTotalItems() > 0) {
			try {
				serializer.serialize(args.getResult(),
						InstanceToJsonSerializer.allOrGivenProperties(RequestParams.PROPERTY_NAMES.get(info)),
						generator);
			} catch (Exception e) {
				throw new InternalServerErrorException("Unable to serialize search results due to "
															   + e.getMessage(), e);
			}
		}
		generator.writeEnd();

		if (args.shouldGroupBy() && args.getAggregatedData() != null) {
			serializeAggregatedResults(args.getAggregatedData(), generator);
		}

		generator.writeEnd();
	}

	private static void serializeAggregatedResults(Map<String, Map<String, Serializable>> aggregated,
			JsonGenerator generator) {
		generator.writeStartObject(AGGREGATED);
		aggregated.entrySet().stream().forEach(property -> {
			generator.writeStartObject(property.getKey());
			if (property.getValue() != null) {
				property.getValue().entrySet().stream().forEach(aggregatedData -> {
					if (aggregatedData.getValue() instanceof Integer) {
						generator.write(aggregatedData.getKey(), (Integer) aggregatedData.getValue());
					}
				});
			}
			generator.writeEnd();
		});
		generator.writeEnd();
	}
}
