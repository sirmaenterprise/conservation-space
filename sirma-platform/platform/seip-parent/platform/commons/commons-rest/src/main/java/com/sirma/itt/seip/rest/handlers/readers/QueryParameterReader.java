package com.sirma.itt.seip.rest.handlers.readers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.models.QueryParameters;

/**
 * {@link MessageBodyReader} for {@link QueryParameters} that provides access to the query parameters of the invoked
 * rest service
 *
 * @author BBonev
 */
@Provider
public class QueryParameterReader implements MessageBodyReader<QueryParameters> {

	@Context
	private UriInfo uriInfo;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return QueryParameters.class.isAssignableFrom(type);
	}

	@Override
	public QueryParameters readFrom(Class<QueryParameters> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
					throws IOException {

		return new QueryParameters(uriInfo.getQueryParameters());
	}

}
