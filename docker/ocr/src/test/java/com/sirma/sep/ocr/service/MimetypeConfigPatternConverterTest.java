package com.sirma.sep.ocr.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Tests for {@link MimetypeConfigPatternConverter}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 24/10/2017
 */
public class MimetypeConfigPatternConverterTest {

	@Test
	public void convert_null_value() throws Exception {
		MimetypeConfigPatternConverter pattern = new MimetypeConfigPatternConverter();
		Pattern actual = pattern.convert(null);
		assertNull(actual);
	}

	@Test
	public void convert_validRegExp() throws Exception {
		MimetypeConfigPatternConverter pattern = new MimetypeConfigPatternConverter();
		Pattern actual = pattern.convert("[a-zA-Z]+");
		assertNotNull(actual);
	}

}