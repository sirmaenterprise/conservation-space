package com.sirma.itt.seip.tenant;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationExternalModel;

/**
 * Body writer for collections of {@link TenantInitializationExternalModel} objects.
 *
 * @author nvelkov
 */
@Provider
public class TenantInitializationExternalModelBodyWriter
		extends AbstractMessageBodyWriter<Collection<TenantInitializationExternalModel>> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return Collection.class.isAssignableFrom(type) && type != null
				&& Types.getCollectionBaseType(type, genericType) != null && TenantInitializationExternalModel.class
						.isAssignableFrom(Types.getCollectionBaseType(type, genericType));
	}

	@Override
	public void writeTo(Collection<TenantInitializationExternalModel> models, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException {
		try (JsonGenerator generator = Json.createGenerator(entityStream)) {
			generator.writeStartArray();
			for (TenantInitializationExternalModel model : models) {
				generator.writeStartObject();
				generator.write("id", model.getId());
				generator.write("text", model.getLabel());
				generator.writeEnd();
			}
			generator.writeEnd().flush();
		}

	}

}
