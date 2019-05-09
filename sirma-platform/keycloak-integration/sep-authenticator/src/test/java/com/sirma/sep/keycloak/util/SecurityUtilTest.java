package com.sirma.sep.keycloak.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for {@link SecurityUtil}.
 *
 * @author smustafov
 */
public class SecurityUtilTest {

	@Test
	public void getUserAndTenant_Should_CorrectlyExtractUserAndTenant() {
		String[] userAndTenant = SecurityUtil.getUserAndTenant(null);
		assertNull(userAndTenant[0]);
		assertNull(userAndTenant[1]);

		userAndTenant = SecurityUtil.getUserAndTenant("systemadmin");
		assertEquals("systemadmin", userAndTenant[0]);
		assertNull(userAndTenant[1]);

		userAndTenant = SecurityUtil.getUserAndTenant("regularuser@sep.test");
		assertEquals("regularuser", userAndTenant[0]);
		assertEquals("sep.test", userAndTenant[1]);
	}

}
