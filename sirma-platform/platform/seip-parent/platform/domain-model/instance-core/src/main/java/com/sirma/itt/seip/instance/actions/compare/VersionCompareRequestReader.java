package com.sirma.itt.seip.instance.actions.compare;

import static com.sirma.itt.seip.instance.actions.compare.VersionCompareRequest.COMPARE_VERSIONS;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Reads the request input from the {@link VersionCompareRestService#compareVersions} and converts it in to
 * {@link VersionCompareRequest} object, used to execute the operation.
 *
 * @author A. Kunchev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class VersionCompareRequestReader implements MessageBodyReader<VersionCompareRequest> {

	@BeanParam
	private RequestInfo info;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return VersionCompareRequest.class.isAssignableFrom(type);
	}

	@Override
	public VersionCompareRequest readFrom(Class<VersionCompareRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException {
		return JSON.readObject(entityStream, toActionRequest());
	}

	private Function<JsonObject, VersionCompareRequest> toActionRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}

			VersionCompareRequest actionRequest = new VersionCompareRequest();
			actionRequest.setTargetId(PATH_ID.get(info));
			actionRequest.setUserOperation(json.getString(JsonKeys.USER_OPERATION, COMPARE_VERSIONS));
			actionRequest.setFirstSourceId(json.getString("firstSourceId"));
			actionRequest.setSecondSourceId(json.getString("secondSourceId"));
			actionRequest.setAuthenticationHeaders(getAuthenticationHeaders());
			return actionRequest;
		};
	}

	private Map<String, String> getAuthenticationHeaders() {
		HttpHeaders headers = info.getHeaders();
		String cookieHeader = headers.getHeaderString(HttpHeaders.COOKIE);
		String authorizationHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
		if (StringUtils.isBlank(cookieHeader) && StringUtils.isBlank(authorizationHeader)) {
			throw new BadRequestException("Missing required authendication headers!");
		}

		Map<String, String> authenticationHeaders = new HashMap<>(2);
		authenticationHeaders.put(HttpHeaders.COOKIE, cookieHeader);
		authenticationHeaders.put(HttpHeaders.AUTHORIZATION, authorizationHeader);
		return authenticationHeaders;
	}

}
