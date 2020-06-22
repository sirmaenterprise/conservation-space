package com.sirma.sep.email;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link EmailIntegrationHelper}.
 *
 * @author S.Djulgerova
 */
public class EmailIntegrationHelperTest {

	@Test
	public void test_generateEmailAddress_without_suffix() {
		String expected = "john-tenant.id@sirma.bg";
		String actual = EmailIntegrationHelper.generateEmailAddress("john@tenant.id", "sirma.bg", null);
		assertEquals(expected, actual);
	}

	@Test
	public void test_generateEmailAddress_with_suffix() {
		String expected = "john-tenant.id-test@sirma.bg";
		String actual = EmailIntegrationHelper.generateEmailAddress("john@tenant.id", "sirma.bg", "test");
		assertEquals(expected, actual);
	}
}