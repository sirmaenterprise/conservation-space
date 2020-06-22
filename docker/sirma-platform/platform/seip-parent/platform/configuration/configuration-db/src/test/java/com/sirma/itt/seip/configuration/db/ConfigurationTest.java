package com.sirma.itt.seip.configuration.db;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.Test;

import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.io.ResourceLoadUtil;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link Configuration}
 *
 * @author BBonev
 */
public class ConfigurationTest {

	private static final String TENANT_ID = "tenant.com";
	private static final String CONFIG_KEY = "configKey";

	@Test
	public void testGetValue_from_definition() throws Exception {
		Configuration configuration = new Configuration(createConfig("aValue"));
		assertEquals("aValue", configuration.getDefaultValue());
		assertEquals("aValue", configuration.getValue());
	}

	private static ConfigurationInstance createConfig(String value) {
		ConfigurationInstance definition = mock(ConfigurationInstance.class);
		ConfigurationPropertyDefinition annotation = mock(ConfigurationPropertyDefinition.class);
		when(definition.getType()).then(a -> String.class);
		when(definition.getLabel()).thenReturn("label for " + value);
		when(definition.getAnnotation()).thenReturn(annotation);
		when(definition.getName()).thenReturn(CONFIG_KEY);
		when(annotation.defaultValue()).thenReturn(value);
		return definition;
	}

}
