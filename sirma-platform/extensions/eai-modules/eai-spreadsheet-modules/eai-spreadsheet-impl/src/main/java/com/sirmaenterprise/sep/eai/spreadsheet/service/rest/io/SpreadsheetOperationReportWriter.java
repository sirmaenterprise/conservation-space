package com.sirmaenterprise.sep.eai.spreadsheet.service.rest.io;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.handlers.writers.InstanceToJsonSerializer;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.ParsedInstance;
import com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadsheetOperationReport;

/**
 * Writer for {@link SpreadsheetOperationReport} that builds a json in format:
 * 
 * <pre>
 {
  "report": {
    "id": "emf:reportid",
    "definitionId": "reportDefinition",
   ...
  },
  "data": [{"exrernalId":...,"dbId":...},...]
}
 * </pre>
 * 
 * @author bbanchev
 */
@Provider
@Produces(Versions.V2_JSON)
public class SpreadsheetOperationReportWriter extends AbstractMessageBodyWriter<SpreadsheetOperationReport> {

	@Inject
	private InstanceToJsonSerializer instanceSerializer;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SpreadsheetOperationReport.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(SpreadsheetOperationReport sheetReport, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException {
		try (JsonGenerator generator = Json.createGenerator(entityStream)) {
			generator.writeStartObject();
			// report is always required
			instanceSerializer.serialize(sheetReport.getReport(), generator, "report");
			generator.writeStartArray("data");
			List<ParsedInstance> instances;
			if (sheetReport.getResult() != null && (instances = sheetReport.getResult().getInstances()) != null) {
				instances.forEach(value -> generator.write(convertEntryToJsonEntry(value)));
			}
			generator.writeEnd();
			generator.writeEnd().flush();
		}
	}

	private static JsonValue convertEntryToJsonEntry(ParsedInstance instance) {
		JsonObjectBuilder idBuilder = Json.createObjectBuilder();
		idBuilder.add("externalId", instance.getExternalId().serialize());
		idBuilder.add("dbId", Objects.toString(instance.getParsed().getId(), null));
		return idBuilder.build();
	}

}
