package com.sirma.itt.seip.template.rest.handlers.readers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.rest.utils.JsonKeys.ID;
import static com.sirma.itt.seip.rest.utils.JsonKeys.PROPERTIES;
import static com.sirma.itt.seip.template.TemplateProperties.FOR_TYPE;
import static com.sirma.itt.seip.template.TemplateProperties.PRIMARY;
import static com.sirma.itt.seip.template.TemplateProperties.SOURCE_INSTANCE;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplateProperties;

/**
 * {@link MessageBodyReader} for converting {@link Versions#V2_JSON} to {@link TemplateInstance}.
 *
 * @author yasko
 */
@Provider
@Consumes(Versions.V2_JSON)
public class TemplateInstanceMessageBodyReader implements MessageBodyReader<TemplateInstance> {

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return TemplateInstance.class.isAssignableFrom(type);
	}

	@Override
	public TemplateInstance readFrom(Class<TemplateInstance> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> headers, InputStream in)
					throws IOException, WebApplicationException {

		JsonObject json = null;
		try (JsonReader reader = Json.createReader(in)) {
			json = reader.readObject();
		}

		TemplateInstance instance = new TemplateInstance();
		if (!json.containsKey(FOR_TYPE)) {
			throw new BadRequestException(new ErrorData().setMessage("'forType' property is required."));
		}

		instance.setId(convertId(json.get(ID)));
		instance.setForType(json.getString(FOR_TYPE));

		String owningInstanceId = json.getString(SOURCE_INSTANCE, null);
		if (StringUtils.isNotNullOrEmpty(owningInstanceId)) {
			Optional<InstanceReference> owningInstance = instanceTypeResolver.resolveReference(owningInstanceId);
			if (owningInstance.isPresent()) {
				instance.setOwningInstance(owningInstance.get().toInstance());
			}
		}

		JsonObject properties = json.getJsonObject(PROPERTIES);
		Map<String, Serializable> instanceProperties = instance.getProperties();
		if (!JSON.isBlank(properties)) {
			instance.addIfNotNull(TITLE, properties.getString(TITLE, null));
			instance.addIfNotNull(PRIMARY, properties.getBoolean(PRIMARY, Boolean.FALSE));
			instance.addIfNotNull(TemplateProperties.PURPOSE, properties.getString(TemplateProperties.PURPOSE, null));
		}

		instanceProperties.put(CONTENT, json.getString(CONTENT, ""));
		return instance;
	}

	private Serializable convertId(JsonValue id) {
		if (id == null) {
			return null;
		}

		switch (id.getValueType()) {
		case NUMBER:
			return Long.valueOf(((JsonNumber) id).longValue());
		case STRING:
			String stringValue = ((JsonString) id).getString();
			try {
				return Long.parseLong(stringValue);
			} catch (NumberFormatException e) {
				// return as string
			}
			return stringValue;
		default:
			return null;
		}
	}
}
