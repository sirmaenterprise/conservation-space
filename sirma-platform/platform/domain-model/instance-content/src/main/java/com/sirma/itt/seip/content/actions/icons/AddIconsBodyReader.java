package com.sirma.itt.seip.content.actions.icons;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.json.JsonArray;
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
 * Reads the request input from the {@link AddIconsRestService} and converts it in to {@link AddIconsRequest} object,
 * used to execute the operation.
 *
 * @author Nikolay Ch
 */
@Provider
@Consumes(Versions.V2_JSON)
public class AddIconsBodyReader implements MessageBodyReader<AddIconsRequest> {

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return AddIconsRequest.class.isAssignableFrom(type);
	}

	@Override
	public AddIconsRequest readFrom(Class<AddIconsRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException {
		return JSON.readObject(entityStream, buildAddIconsRequest());
	}

	private Function<JsonObject, AddIconsRequest> buildAddIconsRequest(){
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}
			AddIconsRequest addIconsRequest = new AddIconsRequest();
			addIconsRequest.setTargetId(PATH_ID.get(request));
			JsonArray icons = json.getJsonArray("icons");
			Map<Serializable, String> contentMapping = new HashMap<Serializable, String>();
			for (int i = 0; i < icons.size(); i++) {
				JsonObject icon = icons.getJsonObject(i);
				contentMapping.put(icon.getInt("size"), icon.getString("image"));
			}
			addIconsRequest.setPurposeIconMapping(contentMapping);
			return addIconsRequest;
		};
	}

}
