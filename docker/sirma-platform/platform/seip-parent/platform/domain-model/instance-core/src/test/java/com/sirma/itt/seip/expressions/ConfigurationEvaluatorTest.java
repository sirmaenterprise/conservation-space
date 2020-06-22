package com.sirma.itt.seip.expressions;

import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.expressions.ConfigurationEvaluator;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * The Class ConfigurationEvaluatorTest.
 *
 * @author BBonev
 */
public class ConfigurationEvaluatorTest extends EmfTest {

	@InjectMocks
	ConfigurationEvaluator evaluator = new ConfigurationEvaluator();

	@Mock
	RawConfigurationAccessor configurationAccessor;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
	}

	/**
	 * Test configuration evaluator.
	 */
	@Test
	public void testConfigurationEvaluator() {
		when(configurationAccessor.getRawConfigurationValue("test.key")).thenReturn("testValue1");
		when(configurationAccessor.getRawConfigurationValue("test.key-test")).thenReturn("testValue2");

		Assert.assertEquals("testValue1", evaluator.evaluate("${config(test.key)}"));
		Assert.assertEquals("alternative", evaluator.evaluate("${config(test.key2, alternative)}"));
		Assert.assertEquals("testValue2", evaluator.evaluate("${config(test.key-test, alternative)}"));
	}

}
