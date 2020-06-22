package com.sirma.itt.seip.permissions.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Message body writer for {@link RoleIdentifier}
 *
 * @author BBonev
 */
@Provider
@Produces(Versions.V2_JSON)
public class RoleIdentifierCollectionWriter extends AbstractMessageBodyWriter<Collection<RoleIdentifier>> {

	@Inject
	private LabelProvider labelProvider;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return Collection.class.isAssignableFrom(type)
				&& ReflectionUtils.isTypeArgument(genericType, RoleIdentifier.class);
	}

	@Override
	public void writeTo(Collection<RoleIdentifier> roles, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
					throws IOException {

		try (JsonGenerator generator = Json.createGenerator(entityStream)) {
			generator.writeStartArray();

			for (RoleIdentifier role : roles) {
				writeRole(role, generator);
			}
			generator.writeEnd();
			generator.flush();
		}
	}

	private void writeRole(RoleIdentifier role, JsonGenerator generator) {
		generator.writeStartObject();
		generator.write("value", role.getIdentifier());
		generator.write("label", labelProvider.getLabel(role.getIdentifier().toLowerCase() + ".label"));
		generator.writeEnd();
	}

}
