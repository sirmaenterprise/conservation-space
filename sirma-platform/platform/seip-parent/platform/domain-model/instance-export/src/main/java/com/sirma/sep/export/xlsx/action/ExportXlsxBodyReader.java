package com.sirma.sep.export.xlsx.action;

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
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.util.file.FileUtil;

/**
 * Converts {@link JsonObject} to {@link ExportXlsxRequest}.
 *
 * @author gshefkedov
 */
@Provider
@Consumes(Versions.V2_JSON)
public class ExportXlsxBodyReader implements MessageBodyReader<ExportXlsxRequest> {

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ExportXlsxRequest.class.isAssignableFrom(type);
	}

	@Override
	public ExportXlsxRequest readFrom(Class<ExportXlsxRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException {
		return JSON.readObject(entityStream, toExportWidgetRequest());
	}

	/**
	 * Process data and converts it to {@link ExportXlsxRequest}.
	 *
	 * @return executable function on json creation.
	 */
	private Function<JsonObject, ExportXlsxRequest> toExportWidgetRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("Request payload is empty.");
			}

			ExportXlsxRequest exportRequest = new ExportXlsxRequest();
			exportRequest.setFileName(FileUtil.convertToValidFileName(json.getString("filename")));
			exportRequest.setRequestJson(json);
			exportRequest.setTargetId(PATH_ID.get(request));
			exportRequest.setUserOperation(json.getString(JsonKeys.USER_OPERATION, ExportXlsxRequest.EXPORT_XLSX));
			return exportRequest;
		};
	}
}
