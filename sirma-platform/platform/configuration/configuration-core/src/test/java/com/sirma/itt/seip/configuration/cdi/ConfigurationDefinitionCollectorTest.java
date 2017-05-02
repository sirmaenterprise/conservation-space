/**
 *
 */
package com.sirma.itt.seip.configuration.cdi;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.fail;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.AfterBeanDiscovery;

import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.PropertyConfigurationInstance;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;

/**
 * @author BBonev
 *
 */
public class ConfigurationDefinitionCollectorTest {

	@Test
	public void test_configurationValidation() {
		AfterBeanDiscovery afterBeanDiscovery = mock(AfterBeanDiscovery.class);
		Map<String, List<Field>> uses = new HashMap<>();
		// 2 uses
		uses.put("config1",
				Arrays.asList(getField(DefineConfig.class, "config1"), getField(UseConfig.class, "config1")));
		// defined one place used other
		uses.put("config2", Arrays.asList(getField(UseConfig.class, "config2")));
		// found in parent and child
		uses.put("config3", Arrays.asList(getField(ParentWithConfig.class, "config"), getField(Child.class, "config")));
		Map<String, ConfigurationInstance> instances = new HashMap<>();

		Field field = getField(DefineConfig.class, "config1");
		instances.put("config1",
				new PropertyConfigurationInstance(field.getAnnotation(ConfigurationPropertyDefinition.class), field));
		field = getField(DefineConfig.class, "config2");
		instances.put("config2",
				new PropertyConfigurationInstance(field.getAnnotation(ConfigurationPropertyDefinition.class), field));
		field = getField(ParentWithConfig.class, "config");
		instances.put("config3",
				new PropertyConfigurationInstance(field.getAnnotation(ConfigurationPropertyDefinition.class), field));

		ConfigurationDefinitionCollector.verifyUsedConfigurationsAreInTheSameTypeAsDefined(instances, uses,
				afterBeanDiscovery);

		verify(afterBeanDiscovery, times(2)).addDefinitionError(any(ConfigurationException.class));
	}

	private static Field getField(Class<?> type, String string) {
		try {
			return type.getField(string);
		} catch (NoSuchFieldException | SecurityException e) {
			fail("Cant resolve " + type.toString() + "." + string, e);
			// never reached
			return null;
		}
	}

	private class DefineConfig {
		@ConfigurationPropertyDefinition(name = "config1")
		public ConfigurationProperty<String> config1;

		@ConfigurationPropertyDefinition(name = "config2")
		public ConfigurationProperty<String> config2;
	}

	private class UseConfig {
		@Configuration("config1")
		public ConfigurationProperty<String> config1;

		@Configuration("config2")
		public ConfigurationProperty<String> config2;
	}

	private class ParentWithConfig {
		@ConfigurationPropertyDefinition(name = "config3")
		public ConfigurationProperty<String> config;
	}

	private class Child extends ParentWithConfig {

	}
}
