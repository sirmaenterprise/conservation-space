package com.sirma.itt.seip.instance.actions.evaluation;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Writer for response from request for instance actions. Converts collection of actions to {@link JsonArray} with the
 * actions as elements.
 *
 * @author A. Kunchev
 */
@Provider
@Produces(Versions.V2_JSON)
public class ActionsMessageBodyWriter extends AbstractMessageBodyWriter<Collection<Action>> {

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		Class<?> collectionType = Types.getCollectionBaseType(clazz, type);
		boolean actionAssignable = collectionType != null ? Action.class.isAssignableFrom(collectionType) : false;
		return Collection.class.isAssignableFrom(clazz) && actionAssignable;
	}

	@Override
	public void writeTo(Collection<Action> actions, Class<?> clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream stream) throws IOException {
		if (isNotEmpty(actions)) {
			try (JsonGenerator generator = Json.createGenerator(stream)) {
				generator.writeStartArray();
				actions.stream().filter(Action::isVisible).map(Action::convertAction)
								.map(JsonObjectBuilder::build).forEach(generator::write);
				generator.writeEnd().flush();
			}
		}
	}
}
