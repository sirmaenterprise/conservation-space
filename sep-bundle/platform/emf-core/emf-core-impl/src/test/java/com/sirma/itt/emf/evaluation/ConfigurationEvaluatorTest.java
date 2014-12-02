package com.sirma.itt.emf.evaluation;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.configuration.SystemConfiguration;
import com.sirma.itt.emf.util.EmfTest;

/**
 * The Class ConfigurationEvaluatorTest.
 * 
 * @author BBonev
 */
public class ConfigurationEvaluatorTest extends EmfTest {

	/**
	 * Test configuration evaluator.
	 */
	@Test
	public void testConfigurationEvaluator() {
		ConfigurationEvaluator evaluator = new ConfigurationEvaluator();
		Configurations configuration = new Configurations();
		ReflectionUtils.setField(evaluator, "systemConfiguration", configuration);
		configuration.properties.put("test.key", "testValue1");
		configuration.properties.put("test.key-test", "testValue2");

		Assert.assertEquals("testValue1", evaluator.evaluate("${config(test.key)}"));
		Assert.assertEquals("alternative", evaluator.evaluate("${config(test.key2, alternative)}"));
		Assert.assertEquals("testValue2",
				evaluator.evaluate("${config(test.key-test, alternative)}"));
	}

	/**
	 * The Class Configurations.
	 */
	static class Configurations implements SystemConfiguration {

		/** The properties. */
		Properties properties = new Properties();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getConfiguration(String key, String defaultValue) {
			return properties.getProperty(key, defaultValue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getConfiguration(String key) {
			return properties.getProperty(key);
		}

		@Override
		public Set<String> getConfigurationKeys() {
			return Collections.emptySet();
		}

	}

}
