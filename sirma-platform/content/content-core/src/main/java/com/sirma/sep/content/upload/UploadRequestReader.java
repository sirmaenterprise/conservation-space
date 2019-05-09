package com.sirma.sep.content.upload;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.sirma.sep.content.ContentConfigurations;

/**
 * Converter for {@code multipart/form-data} to {@link UploadRequest}. The converter also checks if the file size is not
 * over the configured limit.
 *
 * @author BBonev
 */
@Provider
@Consumes(MediaType.MULTIPART_FORM_DATA)
public class UploadRequestReader implements MessageBodyReader<UploadRequest> {

	@Inject
	private javax.enterprise.inject.Instance<RepositoryFileItemFactory> fileItemFactory;

	@Inject
	private ContentConfigurations contentConfigurations;

	@Context
	private HttpServletRequest request;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return MediaType.MULTIPART_FORM_DATA_TYPE.isCompatible(mediaType) && UploadRequest.class.isAssignableFrom(type);
	}

	@Override
	public UploadRequest readFrom(Class<UploadRequest> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
					throws IOException {
		try {
			RepositoryFileItemFactory itemFactory = fileItemFactory.get();
			// the actual file upload happens when the parseRequest method is called
			List<FileItem> items = buildRequestParser(itemFactory).parseRequest(request);
			return new UploadRequest(items, itemFactory);
		} catch (FileUploadException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.BAD_REQUEST.getStatusCode());
		}
	}

	private ServletFileUpload buildRequestParser(RepositoryFileItemFactory itemFactory) {
		ServletFileUpload servletUpload = new ServletFileUpload(itemFactory);
		servletUpload.setFileSizeMax(contentConfigurations.getMaxFileSize().get().longValue());
		servletUpload.setHeaderEncoding(StandardCharsets.UTF_8.name());
		return servletUpload;
	}

}
