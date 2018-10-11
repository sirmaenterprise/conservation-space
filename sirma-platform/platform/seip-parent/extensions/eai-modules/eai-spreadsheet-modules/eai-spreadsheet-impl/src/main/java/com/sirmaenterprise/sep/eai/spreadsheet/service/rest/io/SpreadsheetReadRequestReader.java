package com.sirmaenterprise.sep.eai.spreadsheet.service.rest.io;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
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
import com.sirmaenterprise.sep.eai.spreadsheet.service.rest.SpreadSheetReadRequest;

/**
 * Reads a json payload as {@link SpreadSheetReadRequest}. As a result at least
 * {@link SpreadSheetReadRequest#getTargetId()} is initialized. Optionally {@link SpreadSheetReadRequest#getContext()}
 * could be initialized.
 * 
 * @author bbanchev
 */
@Provider
@Consumes({ MediaType.APPLICATION_JSON, Versions.V2_JSON })
public class SpreadsheetReadRequestReader implements MessageBodyReader<SpreadSheetReadRequest> {
	@BeanParam
	private RequestInfo request;
	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SpreadSheetReadRequest.class.isAssignableFrom(type);
	}

	@Override
	public SpreadSheetReadRequest readFrom(Class<SpreadSheetReadRequest> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {
		return JSON.readObject(entityStream, json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}
			return convertToSpreadsheetRequest(json);
		});
	}

	private SpreadSheetReadRequest convertToSpreadsheetRequest(JsonObject json) {
		SpreadSheetReadRequest readRequest = new SpreadSheetReadRequest();
		readRequest.setTargetId(PATH_ID.get(request));
		// load to check permissions as well
		readRequest.setTargetReference(extractReference(String.valueOf(readRequest.getTargetId())));

		if (json.containsKey("context")) {
			readRequest.setContext(extractReference(json.getString("context", null)));
		}
		return readRequest;
	}

	private InstanceReference extractReference(String id) {
		if (id != null) {
			return domainInstanceService.loadInstance(id).toReference();
		}
		return null;
	}

}
