package com.sirma.itt.emf.authentication.sso.saml;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.emf.authentication.sso.saml.SAMLServiceLogin.SAMLLoginSecurityExclusion;

/**
 * Tests for {@link SAMLServiceLogin}
 *
 * @author Adrian Mitev
 */
public class SAMLServiceLoginTest {

	/**
	 * Tests {@link SAMLLoginSecurityExclusion}.
	 */
	@Test
	public void verifySecurityExlusionShouldExcludeTheServletPath() {
		SAMLLoginSecurityExclusion exclusion = new SAMLLoginSecurityExclusion();
		Assert.assertTrue(exclusion.isForExclusion(SAMLServiceLogin.SERVICE_LOGIN));
	}

}
