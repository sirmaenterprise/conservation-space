package com.sirma.itt.seip.configuration.rest.handlers.reader;

import com.sirma.itt.seip.configuration.db.Configuration;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the JSON to {@link Configuration} conversion logic in {@link ConfigurationCollectionBodyReader}.
 *
 * @author Mihail Radkov
 */
public class ConfigurationCollectionBodyReaderTest {

	private ConfigurationCollectionBodyReader configurationReader;

	@Before
	public void before() {
		configurationReader = new ConfigurationCollectionBodyReader();
	}

	@Test
	public void testIsReadableForDifferentType() {
		Assert.assertFalse(configurationReader.isReadable(Collection.class, null, null, null));

		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { String.class });
		Assert.assertFalse(configurationReader.isReadable(Collection.class, type, null, null));
	}

	@Test
	public void testIsReadableForCorrectType() {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { Configuration.class });
		Assert.assertTrue(configurationReader.isReadable(Collection.class, type, null, null));
	}

	@Test
	public void testReadFromEmptyArray() throws IOException {
		JsonArray emptyJson = Json.createArrayBuilder().build();
		Collection<Configuration> configurations = read(configurationReader, emptyJson);
		Assert.assertTrue(configurations.isEmpty());
	}

	@Test
	public void testReadFrom() throws IOException {
		JsonArray configurationsArray = getTestJson("active");
		Collection<Configuration> configurations = read(configurationReader, configurationsArray);

		Assert.assertEquals(1, configurations.size());
		Configuration configuration = configurations.iterator().next();
		Assert.assertEquals("state", configuration.getConfigurationKey());
		Assert.assertEquals("active", configuration.getValue());
	}

	@Test
	public void testReadFromForIntegralNumber() throws IOException {
		JsonArray configurationsArray = getTestJson(123);
		Collection<Configuration> configurations = read(configurationReader, configurationsArray);

		Assert.assertEquals(1, configurations.size());
		Configuration configuration = configurations.iterator().next();
		Assert.assertEquals("state", configuration.getConfigurationKey());
		// The reading extracts the value as long due to limitations in the JSON API if the value is integer or long
		Assert.assertEquals(123L, configuration.getValue());
	}

	@Test
	public void testReadFromForDecimalNumber() throws IOException {
		JsonArray configurationsArray = getTestJson(123.456);
		Collection<Configuration> configurations = read(configurationReader, configurationsArray);

		Assert.assertEquals(1, configurations.size());
		Configuration configuration = configurations.iterator().next();
		Assert.assertEquals("state", configuration.getConfigurationKey());
		Assert.assertEquals(123.456, configuration.getValue());
	}

	@Test
	public void testReadFromForBoolean() throws IOException {
		JsonArray configurationsArray = getTestJson(false);
		Collection<Configuration> configurations = read(configurationReader, configurationsArray);

		Assert.assertEquals(1, configurations.size());
		Configuration configuration = configurations.iterator().next();
		Assert.assertEquals("state", configuration.getConfigurationKey());
		Assert.assertEquals(false, configuration.getValue());
	}

	@Test
	public void testReadFromForNull() throws IOException {
		JsonArray configurationsArray = getTestJson(null);
		Collection<Configuration> configurations = read(configurationReader, configurationsArray);

		Assert.assertEquals(1, configurations.size());
		Configuration configuration = configurations.iterator().next();
		Assert.assertEquals("state", configuration.getConfigurationKey());
		Assert.assertEquals(null, configuration.getValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIncorrectType() throws IOException {
		JsonArray emptyJson = Json.createArrayBuilder().add("string type value").build();
		InputStream jsonStream = IOUtils.toInputStream(emptyJson.toString(), StandardCharsets.UTF_8);
		configurationReader.readFrom(null, null, null, null, null, jsonStream);
	}

	private JsonArray getTestJson(Object value) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		objectBuilder.add("key", "state");

		if (value instanceof String) {
			objectBuilder.add("value", (String) value);
		} else if (value instanceof Integer) {
			objectBuilder.add("value", (Integer) value);
		} else if (value instanceof Double) {
			objectBuilder.add("value", (Double) value);
		} else if (value instanceof Boolean) {
			objectBuilder.add("value", (Boolean) value);
		} else {
			objectBuilder.addNull("value");
		}

		arrayBuilder.add(objectBuilder.build());
		return arrayBuilder.build();
	}

	private static Collection<Configuration> read(ConfigurationCollectionBodyReader reader, JsonArray array)
			throws IOException {
		InputStream jsonStream = IOUtils.toInputStream(array.toString(), StandardCharsets.UTF_8);
		return reader.readFrom(null, null, null, null, null, jsonStream);
	}
}
