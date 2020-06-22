package com.sirma.itt.seip.instance.properties.entity;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link JsonPropertiesConverter}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonPropertiesConverterTest {

	@InjectMocks
	private JsonPropertiesConverter converter;

	@Test
	public void convertToDatabaseColumn() {
		Map<String, Serializable> toConvert = buildAttributes();
		assertEquals(buildDBData(), converter.convertToDatabaseColumn(toConvert));
	}

	private static Map<String, Serializable> buildAttributes() {
		Map<String, Serializable> toConvert = createHashMap(4);
		toConvert.put("string", "string");
		toConvert.put("number", 1);
		toConvert.put("list", new ArrayList<>());
		toConvert.put("map", new HashMap<>());
		return toConvert;
	}

	private static String buildDBData() {
		return "{\"number\":1,\"string\":\"string\",\"list\":[],\"map\":{}}";
	}

	@Test
	public void convertToEntityAttribute() {
		String dbData = buildDBData();
		assertEquals(buildAttributes(), converter.convertToEntityAttribute(dbData));
	}
}