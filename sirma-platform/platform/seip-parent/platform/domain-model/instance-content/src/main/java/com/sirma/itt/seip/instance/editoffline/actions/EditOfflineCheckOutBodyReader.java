package com.sirma.itt.seip.instance.editoffline.actions;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;
import static com.sirma.sep.content.Content.PRIMARY_CONTENT;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.Range;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Reads the request input from the {@link EditOfflineRestService} and converts it in to {@link EditOfflineCheckOutRequest}
 * object, used to execute the operation.
 *
 * @author T. Dossev
 */
@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class EditOfflineCheckOutBodyReader implements MessageBodyReader<EditOfflineCheckOutRequest> {

	@Context
	private HttpServletResponse response;

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return EditOfflineCheckOutRequest.class.isAssignableFrom(type);
	}

	@Override
	public EditOfflineCheckOutRequest readFrom(Class<EditOfflineCheckOutRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException {

		EditOfflineCheckOutRequest editOfflineRequest = new EditOfflineCheckOutRequest();
		editOfflineRequest.setTargetId(PATH_ID.get(request));
		editOfflineRequest.setUserOperation(EditOfflineCheckOutRequest.EDIT_OFFLINE_CHECK_OUT);
		editOfflineRequest.setForDownload(true);
		editOfflineRequest.setPurpose(PRIMARY_CONTENT);
		editOfflineRequest.setRange(Range.fromString(Range.DEFAULT_RANGE));
		editOfflineRequest.setResponse(response);
		return editOfflineRequest;
	}

}
