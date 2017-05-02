package com.sirma.itt.seip.eai.model.mapping;

import java.io.IOException;
import java.io.StringWriter;

import com.sirma.itt.seip.eai.mock.MockProvider;

public class MappingUtil {
	public static String writeValue(Object value) throws IOException {
		StringWriter stringWriter = new StringWriter();
		MockProvider.getMapperProvider().provideObjectMapper().writeValue(stringWriter, value);
		return stringWriter.toString();
	}

	public static <T> T readValue(String value, Class<T> valueType) throws IOException {
		return MockProvider.getMapperProvider().provideObjectMapper().readValue(value, valueType);
	}
}
