package com.sirmaenterprise.sep.eai.spreadsheet.service.rest.io;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetEntryId;
import com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadsheetDataIntegrataionRequest;

/**
 * Reader of {@link SpreadsheetDataIntegrataionRequest} transforming json payload. As a result at least
 * {@link SpreadsheetDataIntegrataionRequest#getTargetId()} and {@link SpreadsheetDataIntegrataionRequest#getReport()}
 * are initialized.
 *
 * @author bbanchev
 */
@Provider
@Consumes({ MediaType.APPLICATION_JSON, Versions.V2_JSON })
public class SpreadsheetDataIntegrataionRequestReader implements MessageBodyReader<SpreadsheetDataIntegrataionRequest> {
	@BeanParam
	private RequestInfo request;
	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SpreadsheetDataIntegrataionRequest.class.isAssignableFrom(type);
	}

	@Override
	public SpreadsheetDataIntegrataionRequest readFrom(Class<SpreadsheetDataIntegrataionRequest> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {
		return JSON.readObject(entityStream, json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}
			return convertToSpreadsheetRequest(json);
		});
	}

	private SpreadsheetDataIntegrataionRequest convertToSpreadsheetRequest(JsonObject json) {
		SpreadsheetDataIntegrataionRequest dataIntegration = new SpreadsheetDataIntegrataionRequest();
		dataIntegration.setSystemId(SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID);
		dataIntegration.setTargetId(PATH_ID.get(request));
		dataIntegration.setTargetReference(extractReference(String.valueOf(dataIntegration.getTargetId())));
		JsonArray requestData = json.getJsonArray("data");
		if (requestData == null || requestData.isEmpty()) {
			throw new BadRequestException("Missing ids for importable data. Check client code!");
		}
		dataIntegration.setRequestData(requestData
				.stream()
					.map(JsonObject.class::cast)
					.map(data -> convertJsonEntryToEntry(data))
					.collect(Collectors.toList()));
		if (!json.containsKey("report")) {
			throw new BadRequestException("Missing value for generated report. Check client code!");
		}

		if (json.containsKey("context")) {
			dataIntegration.setContext(extractReference(json.getString("context", null)));
		}
		dataIntegration.setReport(extractReference(json.getJsonString("report").getString()));
		return dataIntegration;
	}

	private static SpreadsheetEntryId convertJsonEntryToEntry(JsonObject instance) {
		return SpreadsheetEntryId.deserialize(instance.getString("externalId"));
	}

	private InstanceReference extractReference(String id) {
		if (id != null) {
			return domainInstanceService.loadInstance(id).toReference();
		}
		return null;
	}
}
