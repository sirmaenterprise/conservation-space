package com.sirma.itt.seip.template.rest.handlers.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.rest.handlers.writers.AbstractMessageBodyWriter;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.template.TemplateInstance;

/**
 * Converter for v2 json array of {@link TemplateInstance template instances}.
 * 
 * @author yasko
 */
@Provider
@Produces(Versions.V2_JSON)
public class TemplateListMessageBodyWriter extends AbstractMessageBodyWriter<List<TemplateInstance>> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		Class<?> collectionType = Types.getCollectionBaseType(type, genericType);
		boolean isAssignable = collectionType != null ? TemplateInstance.class.isAssignableFrom(collectionType) : false;
		return List.class.isAssignableFrom(type) && isAssignable;
	}

	@Override
	public void writeTo(List<TemplateInstance> collection, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> headers, OutputStream out)
					throws IOException, WebApplicationException {

		try (JsonGenerator generator = Json.createGenerator(out)) {
			generator.writeStartArray();
			if (!CollectionUtils.isEmpty(collection)) {
				collection.forEach(template -> {
					TemplateInstanceToJson.writeJson(generator, template);
				});
			}
			generator.writeEnd().flush();
		}
	}

}
