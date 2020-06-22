package com.sirma.itt.seip.definition.rest.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Body writer for {@link ClassInstance} instances.
 *
 * @author Vilizar Tsonev
 */
@Provider
@Produces(Versions.V2_JSON)
public class ClassInstanceBodyWriter extends AbstractMessageBodyWriter<ClassInstance> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ClassInstance.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(ClassInstance classInstance, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException {
		try (JsonGenerator generator = Json.createGenerator(entityStream)) {
			generator.writeStartObject().write(JsonKeys.ID, (String) classInstance.getId())
					.write(JsonKeys.CREATABLE, classInstance.isCreatable())
					.write(JsonKeys.UPLOADABLE, classInstance.isUploadable())
					.write(JsonKeys.VERSIONABLE, classInstance.isVersionable())
					.write(JsonKeys.MAILBOX_SUPPORTABLE, classInstance.isMailboxSupportable()).writeEnd().flush();
		}
	}

}
