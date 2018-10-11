package com.sirma.itt.seip.expressions;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.expressions.ShortUriEvaluator;

/**
 * Tests the logic in {@link ShortUriEvaluator}.
 * 
 * @author Mihail Radkov
 */
@Test
public class ShortUriEvaluatorTest extends BaseEvaluatorTest {

	@Mock
	private TypeConverter typeConverter;

	@InjectMocks
	private ShortUriEvaluator evaluator;

	/**
	 * Initializes mocks.
	 */
	@BeforeMethod
	public void initMocks() {
		evaluator = new ShortUriEvaluator();
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the evaluation when the provided value is null.
	 */
	public void testWithNullValue() {
		Assert.assertNull(evaluator.evaluate("${shortUri(null)}"));
	}

	/**
	 * Tests the evaluation when there is <b>no</b> short uri for the provided value.
	 */
	public void testWithMissingUri() {
		mockTypeConverter(null);
		Assert.assertNull(evaluator.evaluate("${shortUri(emf:some-value)}"));
	}
	
	/**
	 * Tests the evaluation when there <b>is</b> a short uri for the provided value.
	 */
	public void testWithCorrectUri() {
		mockTypeConverter(new ShortUri("test"));
		Assert.assertEquals(evaluator.evaluate("${shortUri(emf:some-value)}"), "test");

	}

	/**
	 * Mocks the injected {@link TypeConverter} in the evaluator.
	 * 
	 * @param value
	 *            - the value to be returned by the mocked converter
	 */
	private void mockTypeConverter(ShortUri value) {
		Mockito.when(typeConverter.convert(Matchers.eq(ShortUri.class), Matchers.anyString())).thenReturn(value);
	}
}
