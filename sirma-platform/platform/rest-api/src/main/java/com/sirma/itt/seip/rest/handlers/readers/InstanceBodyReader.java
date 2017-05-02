package com.sirma.itt.seip.rest.handlers.readers;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.PATH_ID;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Converts a JSON object to {@link Instance}.
 *
 * @author yasko
 */
@Provider
@Consumes(Versions.V2_JSON)
public class InstanceBodyReader implements MessageBodyReader<Instance> {

	@Inject
	private InstanceResourceParser instanceResourceParser;

	@BeanParam
	private RequestInfo request;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return Instance.class.isAssignableFrom(type);
	}

	@Override
	public Instance readFrom(Class<Instance> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> headers, InputStream stream) throws IOException {
		String id = PATH_ID.get(request);
		Instance instance = JSON.readObject(stream, instanceResourceParser.toSingleInstance(id));

		if (instance != null) {
			return instance;
		}

		throw new BadRequestException("There was a problem with the stream reading or instance resolving.");
	}
}
