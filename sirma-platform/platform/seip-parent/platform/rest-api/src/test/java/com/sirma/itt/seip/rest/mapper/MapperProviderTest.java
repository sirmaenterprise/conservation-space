package com.sirma.itt.seip.rest.mapper;

import java.io.IOException;
import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * The MapperProviderTest tests {@link MapperProvider}
 */
public class MapperProviderTest {

	/**
	 * Test insit and extend.
	 */
	@Test
	public void testInitAndExtend() {
		MapperProvider mapperProvider = new MapperProvider();
		// no extension
		ReflectionUtils.setFieldValue(mapperProvider, "extensions", Collections.emptyList());
		mapperProvider.initialize();
		ObjectMapper provideObjectMapper = mapperProvider.provideObjectMapper();
		Assert.assertFalse(provideObjectMapper.canSerialize(ClassTestSerialize.class));

		ReflectionUtils.setFieldValue(mapperProvider, "extensions",
				Collections.singletonList(new ClassTestSerializeMapperExtension()));
		mapperProvider.initialize();
		provideObjectMapper = mapperProvider.provideObjectMapper();
		Assert.assertTrue(provideObjectMapper.canSerialize(ClassTestSerialize.class));
	}

	private class ClassTestSerialize {

	}

	/**
	 * The Class ClassTestSerializeMapperExtension.
	 */
	class ClassTestSerializeMapperExtension implements MapperExtension {

		@Override
		public void extend(ObjectMapper mapper) {
			SimpleModule module = new SimpleModule();
			module.addSerializer(ClassTestSerialize.class, new ClassTestSerializeSerializer());
			mapper.registerModule(module);
		}

		/**
		 * The Class ClassTestSerializeSerializer.
		 */
		class ClassTestSerializeSerializer extends JsonSerializer<ClassTestSerialize> {

			@Override
			public void serialize(ClassTestSerialize value, JsonGenerator gen, SerializerProvider serializers)
					throws IOException, JsonProcessingException {
				// just skip
			}

		}

	}
}
