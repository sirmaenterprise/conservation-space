package com.sirma.itt.seip.configuration.rest.handlers.writer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.util.AnnotationLiteral;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.Before;
import org.junit.Test;
import org.testng.Assert;

import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.convert.TypeConverterContext;
import com.sirma.itt.seip.configuration.db.Configuration;

/**
 * Tests the {@link Configuration} to JSON conversion logic in {@link ConfigurationCollectionBodyWriter}.
 *
 * @author Mihail Radkov
 */
public class ConfigurationCollectionBodyWriterTest {

	private ConfigurationCollectionBodyWriter configurationWriter;

	@Before
	public void setUp() {
		configurationWriter = new ConfigurationCollectionBodyWriter();
	}

	@Test
	public void testIsWritableForDifferentType() {
		assertFalse(configurationWriter.isWriteable(Configuration.class, null, null, null));
		assertFalse(configurationWriter.isWriteable(Collection.class, null, null, null));
	}

	@Test
	public void testIsWritableForCorrectType() throws NoSuchFieldException {
		ParameterizedType type = mock(ParameterizedType.class);
		when(type.getActualTypeArguments()).thenReturn(new Type[] { Configuration.class });
		assertTrue(configurationWriter.isWriteable(Collection.class, type, null, null));
	}

	@Test
	public void testWriteToForEmptyConfigurationList() throws IOException {
		Collection<Configuration> configurations = Collections.emptyList();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		configurationWriter.writeTo(configurations, null, null, null, null, null, out);

		JsonArray configurationsAsJson = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readArray();
		Assert.assertEquals(0, configurationsAsJson.size());
	}

	@Test
	public void testConfigurationPropertyConversion() throws IOException {
		ConfigurationPropertyDefinitionMock definition = new ConfigurationPropertyDefinitionMock().setType(String.class)
				.setAlias("Alias")
				.setDefaultValue("defaultValue")
				.setLabel("Label")
				.setName("Some name")
				.setSensitive(true)
				.setShared(false)
				.setSubSystem("subSystem")
				.setSystem(true);
		Configuration configuration = getConfiguration(definition, "string.config", "Some vAlUe", "default.tenant");
		configuration.setRawValue("rawValue");

		JsonArray configurationsAsJson = writeConfigurations(configurationWriter, configuration);
		Assert.assertEquals(1, configurationsAsJson.size());

		JsonObject configurationAsJson = configurationsAsJson.getJsonObject(0);
		assertStringConfiguration(configurationAsJson, "key", "string.config");
		assertStringConfiguration(configurationAsJson, "value", "Some vAlUe");
		assertStringConfiguration(configurationAsJson, "defaultValue", "defaultValue");
		assertStringConfiguration(configurationAsJson, "rawValue", "rawValue");
		assertStringConfiguration(configurationAsJson, "tenantId", "default.tenant");
		assertStringConfiguration(configurationAsJson, "javaType", "java.lang.String");
		assertStringConfiguration(configurationAsJson, "name", "Some name");
		assertStringConfiguration(configurationAsJson, "alias", "Alias");
		assertStringConfiguration(configurationAsJson, "label", "Label");
		assertStringConfiguration(configurationAsJson, "subSystem", "subSystem");
		assertBooleanConfiguration(configurationAsJson, "sensitive", true);
		assertBooleanConfiguration(configurationAsJson, "shared", false);
		assertBooleanConfiguration(configurationAsJson, "system", true);
	}

	@Test
	public void testNumericConfigurationsConversion() throws IOException {
		ConfigurationPropertyDefinitionMock integerDefinition = new ConfigurationPropertyDefinitionMock();
		integerDefinition.setType(Integer.class);
		Configuration integerConfiguration = getConfiguration(integerDefinition, "integer.config", 123,
															  "default.tenant");

		ConfigurationPropertyDefinitionMock longDefinition = new ConfigurationPropertyDefinitionMock();
		longDefinition.setType(Long.class);
		Configuration longConfiguration = getConfiguration(longDefinition, "long.config", 1234567890L,
														   "default.tenant");

		ConfigurationPropertyDefinitionMock doubleDefinition = new ConfigurationPropertyDefinitionMock();
		doubleDefinition.setType(Double.class);
		Configuration doubleConfiguration = getConfiguration(doubleDefinition, "double.config", 1.2, "default.tenant");

		JsonArray configurationsAsJson = writeConfigurations(configurationWriter, integerConfiguration,
															 longConfiguration, doubleConfiguration);
		Assert.assertEquals(3, configurationsAsJson.size());

		JsonObject integerConfigurationAsJson = configurationsAsJson.getJsonObject(0);
		Assert.assertEquals("integer.config", integerConfigurationAsJson.getString("key"));
		Assert.assertEquals(JsonValue.ValueType.NUMBER, integerConfigurationAsJson.get("value").getValueType());
		Assert.assertEquals("123", integerConfigurationAsJson.get("value").toString());

		JsonObject longConfigurationAsJson = configurationsAsJson.getJsonObject(1);
		Assert.assertEquals("long.config", longConfigurationAsJson.getString("key"));
		Assert.assertEquals(JsonValue.ValueType.NUMBER, longConfigurationAsJson.get("value").getValueType());
		Assert.assertEquals("1234567890", longConfigurationAsJson.get("value").toString());

		JsonObject doubleConfigurationAsJson = configurationsAsJson.getJsonObject(2);
		Assert.assertEquals("double.config", doubleConfigurationAsJson.getString("key"));
		Assert.assertEquals(JsonValue.ValueType.NUMBER, doubleConfigurationAsJson.get("value").getValueType());
		Assert.assertEquals("1.2", doubleConfigurationAsJson.get("value").toString());
	}

	@Test
	public void testBooleanConfigurationsConversion() throws IOException {
		ConfigurationPropertyDefinitionMock trueDefinition = new ConfigurationPropertyDefinitionMock();
		trueDefinition.setType(Boolean.class);
		Configuration trueConfiguration = getConfiguration(trueDefinition, "boolean.true.config", true,
														   "default.tenant");

		ConfigurationPropertyDefinitionMock falseDefinition = new ConfigurationPropertyDefinitionMock();
		falseDefinition.setType(Boolean.class);
		Configuration falseConfiguration = getConfiguration(falseDefinition, "boolean.false.config", false,
															"default.tenant");

		JsonArray configurationsAsJson = writeConfigurations(configurationWriter, trueConfiguration,
															 falseConfiguration);
		Assert.assertEquals(2, configurationsAsJson.size());

		JsonObject trueConfigurationAsJson = configurationsAsJson.getJsonObject(0);
		Assert.assertEquals("boolean.true.config", trueConfigurationAsJson.getString("key"));
		Assert.assertEquals(JsonValue.ValueType.TRUE, trueConfigurationAsJson.get("value").getValueType());
		Assert.assertEquals("true", trueConfigurationAsJson.get("value").toString());

		JsonObject falseConfigurationAsJson = configurationsAsJson.getJsonObject(1);
		Assert.assertEquals("boolean.false.config", falseConfigurationAsJson.getString("key"));
		Assert.assertEquals(JsonValue.ValueType.FALSE, falseConfigurationAsJson.get("value").getValueType());
		Assert.assertEquals("false", falseConfigurationAsJson.get("value").toString());
	}

	@Test
	public void testCollectionConfigurationsConversion() throws IOException {
		ConfigurationPropertyDefinitionMock setDefinition = new ConfigurationPropertyDefinitionMock();
		setDefinition.setType(Set.class);
		Collection<String> states = Arrays.asList("1", "2", "3");
		Configuration setConfiguration = getConfiguration(setDefinition, "collection.config", states, "default.tenant");

		JsonArray configurationsAsJson = writeConfigurations(configurationWriter, setConfiguration);
		Assert.assertEquals(1, configurationsAsJson.size());

		JsonObject setConfigurationAsJson = configurationsAsJson.getJsonObject(0);
		Assert.assertEquals("collection.config", setConfigurationAsJson.getString("key"));
		Assert.assertEquals(JsonValue.ValueType.ARRAY, setConfigurationAsJson.get("value").getValueType());

		JsonArray statesAsJsonArray = setConfigurationAsJson.getJsonArray("value");
		Assert.assertEquals(3, statesAsJsonArray.size());
		Assert.assertEquals("1", statesAsJsonArray.getString(0));
		Assert.assertEquals("2", statesAsJsonArray.getString(1));
		Assert.assertEquals("3", statesAsJsonArray.getString(2));
	}

	@Test
	public void testDependentPropertiesConversion() throws URISyntaxException, IOException {
		ConfigurationGroupDefinitionMock groupConfigurationDefinition = new ConfigurationGroupDefinitionMock().
				setLabel("Group config label")
				.setSystem(false)
				.setSubSystem("Sub system")
				.setType(URI.class)
				.setProperties(new String[] { "dependant1", "dependant2" });
		URI address = new URI("http://google.com");
		Configuration groupConfiguration = getConfiguration(groupConfigurationDefinition, "group.config", address,
															"default.tenant");

		JsonArray configurationsAsJson = writeConfigurations(configurationWriter, groupConfiguration);
		Assert.assertEquals(1, configurationsAsJson.size());

		JsonObject configurationAsJson = configurationsAsJson.getJsonObject(0);
		assertStringConfiguration(configurationAsJson, "key", "group.config");
		assertStringConfiguration(configurationAsJson, "value", "http://google.com");

		JsonArray dependsOn = configurationAsJson.getJsonArray("dependsOn");
		Assert.assertEquals(2, dependsOn.size());
		Assert.assertEquals("dependant1", dependsOn.getString(0));
		Assert.assertEquals("dependant2", dependsOn.getString(1));
	}

	private static JsonArray writeConfigurations(ConfigurationCollectionBodyWriter configurationWriter,
			Configuration... configuration) throws IOException {
		Collection<Configuration> configurations = Arrays.asList(configuration);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		configurationWriter.writeTo(configurations, null, null, null, null, null, out);
		return Json.createReader(new ByteArrayInputStream(out.toByteArray())).readArray();
	}

	private static void assertStringConfiguration(JsonObject configuration, String key, String value) {
		Assert.assertTrue(configuration.containsKey(key));
		Assert.assertEquals(value, configuration.getString(key));
	}

	private static void assertBooleanConfiguration(JsonObject configuration, String key, boolean value) {
		Assert.assertTrue(configuration.containsKey(key));
		Assert.assertEquals(value, configuration.getBoolean(key));
	}

	private static Configuration getConfiguration(Annotation configurationDefinition, String key, Object value,
			String tenant) {
		ConfigurationInstance configurationInstance = new ConfigurationInstanceMock(configurationDefinition);
		Configuration configuration = new Configuration();
		configuration.setConfigurationKey(key);
		configuration.setValue(value);
		configuration.setTenantId(tenant);
		configuration.setDefinition(configurationInstance);
		return configuration;
	}

	/**
	 * Mocks {@link ConfigurationInstance} to simplify testing.
	 *
	 * @author Mihail Radkov
	 */
	private static class ConfigurationInstanceMock implements ConfigurationInstance {

		private final Annotation annotation;

		/**
		 * Constructs new configuration with the provided annotation.It can be either
		 * {@link ConfigurationPropertyDefinition} or {@link ConfigurationGroupDefinition}. Anything else is not
		 * supported.
		 *
		 * @param annotation the configuration definition annotation
		 */
		ConfigurationInstanceMock(Annotation annotation) {
			this.annotation = annotation;
		}

		@Override
		public String getName() {
			if (annotation instanceof ConfigurationPropertyDefinition) {
				return ((ConfigurationPropertyDefinition) annotation).name();
			} else if (annotation instanceof ConfigurationGroupDefinition) {
				return ((ConfigurationGroupDefinition) annotation).name();
			}
			return null;
		}

		@Override
		public boolean isSystemConfiguration() {
			if (annotation instanceof ConfigurationPropertyDefinition) {
				return ((ConfigurationPropertyDefinition) annotation).system();
			} else if (annotation instanceof ConfigurationGroupDefinition) {
				return ((ConfigurationGroupDefinition) annotation).system();
			}
			return false;
		}

		@Override
		public boolean isSharedConfiguration() {
			if (annotation instanceof ConfigurationPropertyDefinition) {
				return ((ConfigurationPropertyDefinition) annotation).shared();
			}
			return false;
		}

		@Override
		public <T> Class<T> getType() {
			if (annotation instanceof ConfigurationPropertyDefinition) {
				return (Class<T>) ((ConfigurationPropertyDefinition) annotation).type();
			} else if (annotation instanceof ConfigurationGroupDefinition) {
				return (Class<T>) ((ConfigurationGroupDefinition) annotation).type();
			}
			return null;
		}

		@Override
		public String getLabel() {
			if (annotation instanceof ConfigurationPropertyDefinition) {
				return ((ConfigurationPropertyDefinition) annotation).label();
			} else if (annotation instanceof ConfigurationGroupDefinition) {
				return ((ConfigurationGroupDefinition) annotation).label();
			}
			return null;
		}

		@Override
		public Annotation getAnnotation() {
			return annotation;
		}

		@Override
		public String getSubSystem() {
			if (annotation instanceof ConfigurationPropertyDefinition) {
				return ((ConfigurationPropertyDefinition) annotation).subSystem();
			} else if (annotation instanceof ConfigurationGroupDefinition) {
				return ((ConfigurationGroupDefinition) annotation).subSystem();
			}
			return null;
		}

		@Override
		public String getAlias() {
			if (annotation instanceof ConfigurationPropertyDefinition) {
				return ((ConfigurationPropertyDefinition) annotation).alias();
			} else if (annotation instanceof ConfigurationGroupDefinition) {
				return ((ConfigurationGroupDefinition) annotation).alias();
			}
			return null;
		}

		@Override
		public boolean isSensitive() {
			if (annotation instanceof ConfigurationPropertyDefinition) {
				return ((ConfigurationPropertyDefinition) annotation).sensitive();
			}
			return false;
		}

		@Override
		public boolean isPassword() {
			if (annotation instanceof ConfigurationPropertyDefinition) {
				return ((ConfigurationPropertyDefinition) annotation).password();
			}
			return false;
		}

		@Override
		public TypeConverterContext createConverterContext(ConfigurationInstanceProvider configurationInstanceProvider,
				RawConfigurationAccessor rawConfigurationAccessor, ConfigurationProvider provider) {
			return null;
		}
	}

	/**
	 * Mock of {@link ConfigurationPropertyDefinition} designed for easy testing of property configurations.
	 *
	 * @author Mihail Radkov
	 */
	private static class ConfigurationPropertyDefinitionMock extends AnnotationLiteral<ConfigurationPropertyDefinition>
			implements ConfigurationPropertyDefinition {

		private String name;
		private boolean system;
		private boolean shared;
		private String defaultValue;
		private Class<?> type;
		private String label;
		private String subSystem;
		private String alias;
		private String converter;
		private boolean sensitive;
		private boolean password;

		@Override
		public String name() {
			return name;
		}

		@Override
		public boolean system() {
			return system;
		}

		@Override
		public boolean shared() {
			return shared;
		}

		@Override
		public String defaultValue() {
			return defaultValue;
		}

		@Override
		public Class<?> type() {
			return type;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String subSystem() {
			return subSystem;
		}

		@Override
		public String alias() {
			return alias;
		}

		@Override
		public String converter() {
			return converter;
		}

		@Override
		public boolean sensitive() {
			return sensitive;
		}

		public ConfigurationPropertyDefinitionMock setName(String name) {
			this.name = name;
			return this;
		}

		public ConfigurationPropertyDefinitionMock setSystem(boolean system) {
			this.system = system;
			return this;
		}

		public ConfigurationPropertyDefinitionMock setShared(boolean shared) {
			this.shared = shared;
			return this;
		}

		public ConfigurationPropertyDefinitionMock setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public ConfigurationPropertyDefinitionMock setType(Class<?> type) {
			this.type = type;
			return this;
		}

		public ConfigurationPropertyDefinitionMock setLabel(String label) {
			this.label = label;
			return this;
		}

		public ConfigurationPropertyDefinitionMock setSubSystem(String subSystem) {
			this.subSystem = subSystem;
			return this;
		}

		public ConfigurationPropertyDefinitionMock setAlias(String alias) {
			this.alias = alias;
			return this;
		}

		public ConfigurationPropertyDefinitionMock setConverter(String converter) {
			this.converter = converter;
			return this;
		}

		public ConfigurationPropertyDefinitionMock setSensitive(boolean sensitive) {
			this.sensitive = sensitive;
			return this;
		}

		@Override
		public boolean password() {
			return password;
		}

		public ConfigurationPropertyDefinitionMock setPassword(boolean password) {
			this.password = password;
			return this;
		}
	}

	/**
	 * Mock of {@link ConfigurationGroupDefinition} designed for easy testing of group configurations.
	 *
	 * @author Mihail Radkov
	 */
	private static class ConfigurationGroupDefinitionMock extends AnnotationLiteral<ConfigurationGroupDefinition>
			implements ConfigurationGroupDefinition {

		private String name;
		private String[] properties;
		private Class<?> type;
		private String label;
		private boolean system;
		private String subSystem;
		private String alias;

		@Override
		public String name() {
			return name;
		}

		@Override
		public String[] properties() {
			return properties;
		}

		@Override
		public Class<?> type() {
			return type;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public boolean system() {
			return system;
		}

		@Override
		public String subSystem() {
			return subSystem;
		}

		@Override
		public String alias() {
			return alias;
		}

		@Override
		public String converter() {
			return null;
		}

		public ConfigurationGroupDefinitionMock setName(String name) {
			this.name = name;
			return this;
		}

		public ConfigurationGroupDefinitionMock setProperties(String[] properties) {
			this.properties = properties;
			return this;
		}

		public ConfigurationGroupDefinitionMock setType(Class<?> type) {
			this.type = type;
			return this;
		}

		public ConfigurationGroupDefinitionMock setLabel(String label) {
			this.label = label;
			return this;
		}

		public ConfigurationGroupDefinitionMock setSystem(boolean system) {
			this.system = system;
			return this;
		}

		public ConfigurationGroupDefinitionMock setSubSystem(String subSystem) {
			this.subSystem = subSystem;
			return this;
		}

		public ConfigurationGroupDefinitionMock setAlias(String alias) {
			this.alias = alias;
			return this;
		}
	}

}
