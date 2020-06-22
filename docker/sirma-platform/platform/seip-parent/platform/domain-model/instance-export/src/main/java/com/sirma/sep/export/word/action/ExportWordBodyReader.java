package com.sirma.sep.export.word.action;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.seip.util.file.FileUtil;

/**
 * Converts {@link JsonObject} to {@link ExportWordRequest}. Also the cookies for the request object are extracted from
 * the {@link RequestInfo}.
 *
 * @author Stella D
 */
@Provider
@Consumes(Versions.V2_JSON)
public class ExportWordBodyReader implements MessageBodyReader<ExportWordRequest> {

	private static final String JSON_KEY_FILE_NAME = "file-name";
	private static final String PARAM_SELECTED_TAB_ID = "tab";
	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return ExportWordRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public ExportWordRequest readFrom(Class<ExportWordRequest> clazz, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> headers, InputStream input)
			throws IOException {
		return JSON.readObject(input, toExportWordRequest());
	}

	private Function<JsonObject, ExportWordRequest> toExportWordRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("Request payload is empty.");
			}
			ExportWordRequest exportRequest = new ExportWordRequest();

			String url = json.getString("url");
			if (url.contains(PARAM_SELECTED_TAB_ID)) {
				List<NameValuePair> params = URLEncodedUtils.parse(url, StandardCharsets.UTF_8, '&');
				params.stream().filter(param -> EqualsHelper.nullSafeEquals(param.getName(), PARAM_SELECTED_TAB_ID)).findFirst().ifPresent(param -> exportRequest.setTabId(param.getValue()));
			}
			exportRequest.setUrl(url);
			exportRequest.setFileName(fetchFileName(json));
			// id and user operation are needed for definition scripts event
			exportRequest.setTargetId(PATH_ID.get(request));
			exportRequest.setUserOperation(json.getString("operation"));
			return exportRequest;
		};
	}

	/**
	 * Fetch file name. If file name exist will be used otherwise will be generated.
	 *
	 * @param json
	 *            the json
	 * @return name of file.
	 */
	private static String fetchFileName(JsonObject json) {
		StringBuilder fileName = new StringBuilder();
		if (json.containsKey(JSON_KEY_FILE_NAME)) {
			fileName.append(json.getString(JSON_KEY_FILE_NAME));
		} else {
			fileName.append("export_file_");
			fileName.append(UUID.randomUUID().toString());
		}
		return FileUtil.convertToValidFileName(fileName.toString());
	}
}