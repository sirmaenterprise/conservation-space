package com.sirma.sep.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Test for {@link PreAuthUtility}.
 *
 * @author S.Djulgerova
 */
public class PreAuthUtilityTest {

	@Test
	public void test_getPreAuthToken() {
		assertNotNull(PreAuthUtility.getPreAuthToken("test-user@sirmaplatform.com"));
		assertEquals(40, PreAuthUtility.getPreAuthToken("test-user@sirmaplatform.com").length());
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_getPreAuthToken_emptyDomain() {
		PreAuthUtility.getPreAuthToken("");
	}

}
