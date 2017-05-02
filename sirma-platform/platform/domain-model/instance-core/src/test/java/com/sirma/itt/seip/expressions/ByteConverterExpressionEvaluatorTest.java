package com.sirma.itt.seip.expressions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ByteConverterExpressionEvaluator}.
 *
 * @author A. Kunchev
 */
public class ByteConverterExpressionEvaluatorTest {

	private ByteConverterExpressionEvaluator evaluator;

	@Before
	public void setup() {
		evaluator = new ByteConverterExpressionEvaluator();
	}

	@Test
	public void getExpressionId() {
		assertEquals("getReadableFormat", evaluator.getExpressionId());
	}

	@Test
	public void getPattern() {
		assertNotNull(evaluator.getPattern());
	}

	@Test
	public void evaluateInternal_noMatch() {
		Serializable result = evaluator.evaluate("${readableFormat(4096)}");
		assertNull(result);
	}

	@Test
	public void evaluateInternal_withValue() {
		Serializable result = evaluator.evaluate("${getReadableFormat(4)}");
		assertEquals("4 B", result);
	}

	@Test
	public void evaluateInternal_withValue_notANumber() {
		Serializable result = evaluator.evaluate("${getReadableFormat(null)}");
		assertEquals("null", result);
	}

	@Test
	public void evaluateInternal_withValue_numberWithLetters() {
		Serializable result = evaluator.evaluate("${getReadableFormat(2sgfs2354fdas)}");
		assertEquals("2sgfs2354fdas", result);
	}

}
