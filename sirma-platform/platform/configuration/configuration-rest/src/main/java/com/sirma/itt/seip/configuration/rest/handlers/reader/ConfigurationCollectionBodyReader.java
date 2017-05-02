package com.sirma.itt.seip.configuration.rest.handlers.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.util.Types;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;

/**
 * Converts {@link JsonArray} to a {@link Collection} of {@link Configuration}.
 * <br>
 * The conversion looks only for the key and value in every object from the provided {@link JsonArray}.
 * <br>
 * The conversion supports string, number, boolean & null values. Anything else will be stored as {@link #toString()}.
 *
 * @author Mihail Radkov
 */
@Provider
public class ConfigurationCollectionBodyReader implements MessageBodyReader<Collection<Configuration>> {

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		Class<?> collectionType = Types.getCollectionBaseType(type, genericType);
		boolean isAssignable = collectionType != null && Configuration.class.isAssignableFrom(collectionType);
		return Collection.class.isAssignableFrom(type) && isAssignable;
	}

	@Override
	public Collection<Configuration> readFrom(Class<Collection<Configuration>> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {

		return JSON.readArray(entityStream, array -> array.stream()
				.map(configEntry -> toConfiguration(configEntry))
				.collect(Collectors.toList()));
	}

	private static Configuration toConfiguration(JsonValue configurationJson) {
		JsonValue.ValueType valueType = configurationJson.getValueType();
		if (!JsonValue.ValueType.OBJECT.equals(valueType)) {
			throw new IllegalArgumentException("Wrong type of configuration object was provided -> " + valueType);
		}

		JsonObject configurationObject = (JsonObject) configurationJson;
		Configuration configuration = new Configuration();
		configuration.setConfigurationKey(configurationObject.getString(JsonKeys.KEY));

		Serializable value = JSON.readJsonValue(configurationObject.get(JsonKeys.VALUE));
		configuration.setValue(value);

		return configuration;
	}
}