package com.sirma.itt.seip.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.function.Function;

import org.junit.Test;

public class RegExGeneratorTest {
	private final String NAME_REGEX_MESSAGE = "Invalid format. Use letters and digits only up to {0} signs.";

	/**
	 * Tests {@link RegExGenerator#getNameRegEx(type,rnc)} with a proper rnc
	 * name field.
	 */
	@Test
	public void nameRegexTest() {
		RegExGenerator generator = new RegExGenerator(new DummyLabelProvider(NAME_REGEX_MESSAGE));
		String actual = generator.getNameRegEx("an..180", "[^|\\/*:<>\"?]{1,180}$").getFirst();
		assertEquals("[^|\\/*:<>\"?]{1,180}$", actual);
	}

	/**
	 * Tests {@link RegExGenerator#getNameRegEx(type,rnc)} proper error message
	 * is set to the returned value .
	 */
	@Test
	public void nameMessageText() {
		RegExGenerator generator = new RegExGenerator(new DummyLabelProvider(NAME_REGEX_MESSAGE));
		String actual = generator.getNameRegEx("an..180", "[^|\\/*:<>\"?]+$").getSecond();
		assertEquals("Invalid format. Use letters and digits only up to 180 signs.", actual);
	}

	/**
	 * Tests {@link RegExGenerator#getNameRegEx(type,rnc)} without set character
	 * limit. The limit must be extracted from the type and built into the
	 * regex.
	 */
	@Test
	public void rncWithoutLimitTest() {
		RegExGenerator generator = new RegExGenerator(new DummyLabelProvider(NAME_REGEX_MESSAGE));
		String actual = generator.getNameRegEx("an..20", "[^|\\/*:<>\"?]+$").getFirst();
		assertEquals("[^|\\/*:<>\"?]{1,20}$", actual);
	}

	/**
	 * Tests if the generator creates a non null regEx when given a type with
	 * two digits after the floating point.
	 */
	@Test
	public void twoDigitsAfterTheFloatingPointTest() {
		RegExGenerator generator = new RegExGenerator(new DummyLabelProvider(NAME_REGEX_MESSAGE));
		String result = generator.getPattern("n..14,12", null).getFirst();
		assertNotNull(result);
	}
}

/**
 * Provides a dummy message for the regex generator.
 *
 * @author georgi.tsankov
 *
 */
class DummyLabelProvider implements Function<String, String> {
	private String message;

	DummyLabelProvider(String message) {
		this.message = message;
	}

	@Override
	public String apply(String t) {
		return message;
	}
}
