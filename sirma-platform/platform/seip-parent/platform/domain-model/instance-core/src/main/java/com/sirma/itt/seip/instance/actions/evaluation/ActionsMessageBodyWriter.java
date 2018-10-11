package com.sirma.itt.seip.instance.actions.evaluation;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Writer for response from request for instance actions. Converts collection of actions to {@link JsonArray} with the
 * actions as elements.
 *
 * @author A. Kunchev
 */
@Provider
@Produces(Versions.V2_JSON)
public class ActionsMessageBodyWriter implements MessageBodyWriter<Collection<Action>> {

	@Override
	public long getSize(Collection<Action> arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		Class<?> collectionType = Types.getCollectionBaseType(clazz, type);
		boolean actionAssignable = collectionType != null ? Action.class.isAssignableFrom(collectionType) : false;
		return Collection.class.isAssignableFrom(clazz) && actionAssignable;
	}

	@Override
	public void writeTo(Collection<Action> actions, Class<?> clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream stream) throws IOException {
		if (CollectionUtils.isNotEmpty(actions)) {
			try (JsonGenerator array = Json.createGenerator(stream).writeStartArray()) {
				actions.stream().filter(Action::isVisible).forEach(action -> array.write(Action.convertAction(action)));
				array.writeEnd().flush();
			}
		}
	}

}
