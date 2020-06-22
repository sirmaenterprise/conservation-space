package com.sirma.itt.seip.rest.handlers.readers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.resources.instances.InstanceResourceParser;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Converts a JSON array to list of {@link Instance} objects.
 *
 * @author velikov
 */
@Provider
@Consumes(Versions.V2_JSON)
public class InstanceCollectionBodyReader implements MessageBodyReader<Collection<Instance>> {

	@Inject
	private InstanceResourceParser instanceResourceParser;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		Class<?> collectionType = Types.getCollectionBaseType(type, genericType);
		boolean isAssignable = collectionType != null ? Instance.class.isAssignableFrom(collectionType) : false;
		return Collection.class.isAssignableFrom(type) && isAssignable;
	}

	@Override
	public Collection<Instance> readFrom(Class<Collection<Instance>> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
					throws IOException {
		return instanceResourceParser.toInstanceList(entityStream);
	}

}
