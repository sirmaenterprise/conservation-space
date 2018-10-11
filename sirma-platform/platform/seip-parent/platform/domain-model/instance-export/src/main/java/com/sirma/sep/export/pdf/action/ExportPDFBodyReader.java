package com.sirma.sep.export.pdf.action;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Converts {@link JsonObject} to {@link ExportPDFRequest}. Also the cookies for the request object are extracted from
 * the {@link RequestInfo}.
 *
 * @author A. Kunchev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class ExportPDFBodyReader implements MessageBodyReader<ExportPDFRequest> {

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return ExportPDFRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public ExportPDFRequest readFrom(Class<ExportPDFRequest> clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> headers, InputStream input) throws IOException {
		return JSON.readObject(input, toExportPDFRequest());
	}

	private Function<JsonObject, ExportPDFRequest> toExportPDFRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("Request payload is empty.");
			}
			ExportPDFRequest exportRequest = new ExportPDFRequest();
			exportRequest.setUrl(json.getString("url"));
			exportRequest.setFileName(json.getString("file-name", null));
			// id and user operation are needed for definition scripts event
			exportRequest.setTargetId(PATH_ID.get(request));
			exportRequest.setUserOperation(json.getString("operation"));
			return exportRequest;
		};
	}

}
