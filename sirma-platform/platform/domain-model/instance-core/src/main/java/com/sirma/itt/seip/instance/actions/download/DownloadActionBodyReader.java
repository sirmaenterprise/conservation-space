package com.sirma.itt.seip.instance.actions.download;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.QUERY_DOWNLOAD_PURPOSE;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Reads the request input from the {@link DownloadRestService} and converts it in to {@link DownloadRequest} object,
 * used to execute the operation.
 *
 * @author A. Kunchev
 */
@Provider
@Consumes(Versions.V2_JSON)
public class DownloadActionBodyReader implements MessageBodyReader<DownloadRequest> {

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return DownloadRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public DownloadRequest readFrom(Class<DownloadRequest> clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> headers, InputStream stream) throws IOException {
		DownloadRequest downloadRequest = new DownloadRequest();
		downloadRequest.setUserOperation(DownloadRequest.DOWNLOAD);
		downloadRequest.setTargetId(PATH_ID.get(request));
		downloadRequest.setPurpose(QUERY_DOWNLOAD_PURPOSE.get(request));
		return downloadRequest;
	}

}
