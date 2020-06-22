package com.sirma.itt.seip.rest.handlers.writers;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * Test for {@link PropertiesFilterBuilder}
 *
 * @author BBonev
 */
public class PropertiesFilterBuilderTest {

	@Test
	public void onlyPropertiesShouldRejectAllIfInitWithEmptyCollection() throws Exception {
		assertFalse(PropertiesFilterBuilder.onlyProperties(null).test(null));
		assertFalse(PropertiesFilterBuilder.onlyProperties(null).test(""));
		assertFalse(PropertiesFilterBuilder.onlyProperties(null).test("Test"));

		assertFalse(PropertiesFilterBuilder.onlyProperties(Collections.emptyList()).test(null));
		assertFalse(PropertiesFilterBuilder.onlyProperties(Collections.emptyList()).test(""));
		assertFalse(PropertiesFilterBuilder.onlyProperties(Collections.emptyList()).test("Test"));
	}

	@Test
	public void onlyPropertiesShouldMatchTheGivenProperties() throws Exception {
		assertFalse(PropertiesFilterBuilder.onlyProperties(Arrays.asList("property")).test("unkownProperty"));
	}

	@Test
	public void onlyPropertiesShouldNotMatchUnkownProperties() throws Exception {
		assertFalse(PropertiesFilterBuilder.onlyProperties(Arrays.asList("property")).test("unkownProperty"));
	}
}
