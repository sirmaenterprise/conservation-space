package com.sirma.itt.seip.instance.properties.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Converter for entity attributes of type Map. The attributes will be serialized to string representation of JSON
 * object that can be stored directly in DB column. This converter is applied automatically, when the entities
 * attributes are persisted/loaded. <br>
 * This particular implementation uses {@link ObjectMapper} to perform the serialization/deserialization of the
 * registered attributes. The attributes should be of type {@link Map}. <br>
 * Note that {@link AttributeConverter} does not work with parametric types so the types of the {@link Map} elements
 * could not be defined.
 * <p>
 * The converter will throw {@link EmfRuntimeException} when there is an error while serialization/deserialization
 * process is executed.
 * </p>
 *
 * @author A. Kunchev
 */
@Converter(autoApply = true)
@SuppressWarnings("rawtypes")
public class JsonPropertiesConverter implements AttributeConverter<Map, String> {

	@Override
	public String convertToDatabaseColumn(Map attribute) {
		try {
			return new ObjectMapper().writeValueAsString(attribute);
		} catch (JsonProcessingException e) {
			throw new EmfRuntimeException("Error while serializing properties map to Json." + e.getMessage(), e);
		}
	}

	@Override
	public Map<String, Serializable> convertToEntityAttribute(String dbData) {
		try {
			return new ObjectMapper().readValue(dbData, Map.class);
		} catch (IOException e) {
			throw new EmfRuntimeException("Error while deserializing properties to Map." + e.getMessage(), e);
		}
	}
}