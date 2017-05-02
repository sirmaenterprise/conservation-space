package com.sirma.itt.seip.instance.actions.thumbnail;

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

import org.json.JSONObject;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Converts {@link JSONObject} and some requst parameters to {@link AddThumbnailRequest}.
 *
 * @author A. Kunchev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class AddThumbnailRequestBodyReader implements MessageBodyReader<AddThumbnailRequest> {

	private static final String THUMBNAIL_OBJECT_ID = "thumbnailObjectId";

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return AddThumbnailRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public AddThumbnailRequest readFrom(Class<AddThumbnailRequest> clazz, Type type, Annotation[] annotation,
			MediaType mediaType, MultivaluedMap<String, String> headers, InputStream stream) throws IOException {
		return JSON.readObject(stream, buildAddThumbnailRequest());
	}

	private Function<JsonObject, AddThumbnailRequest> buildAddThumbnailRequest() {
		return json -> {
			if (json.isEmpty()) {
				throw new BadRequestException("The JSON object is empty");
			}

			AddThumbnailRequest thumbnailRequest = new AddThumbnailRequest();
			thumbnailRequest.setTargetId(PATH_ID.get(request));
			thumbnailRequest.setThumbnailObjectId(json.getString(THUMBNAIL_OBJECT_ID, null));
			thumbnailRequest.setUserOperation(AddThumbnailRequest.OPERATION_NAME);
			return thumbnailRequest;
		};
	}

}
