package com.sirma.itt.seip.permissions.role;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link AuthorityRoleAssignment}.
 *
 * @author A. Kunchev
 */

public class AuthorityRoleAssignmentTest {

	@Test
	@SuppressWarnings("static-method")
	public void pairConstructorTest() {
		AuthorityRoleAssignment assignment = new AuthorityRoleAssignment("authority", "role");
		assertEquals("authority", assignment.getAuthority());
		assertEquals("role", assignment.getRole());
	}

}
