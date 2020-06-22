/**
 * Copyright (c) 2015 Nov 12, 2015 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.authentication.sso.saml;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.emf.authentication.sso.saml.AuthenticationServlet.AuthenticationServletSecurityExclusion;

/**
 * Tests for {@link AuthenticationServlet}
 *
 * @author Adrian Mitev
 */
public class AuthenticationServletTest {

	/**
	 * Tests {@link AuthenticationServletSecurityExclusion}.
	 */
	@Test
	public void verifySecurityExlusionShouldExcludeTheServletPath() {
		AuthenticationServletSecurityExclusion exclusion = new AuthenticationServletSecurityExclusion();
		Assert.assertTrue(exclusion.isForExclusion(AuthenticationServlet.SERLVET_PATH));
	}

}
