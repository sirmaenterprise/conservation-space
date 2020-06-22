package com.sirma.itt.seip.permissions.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.permissions.PermissionIdentifier;
import com.sirma.itt.seip.permissions.model.PermissionId;

/**
 * The Class EmfPermissionTest.
 */
public class PermissionIdTest {

	/**
	 * Test init.
	 */
	@Test
	public void testInit() {
		try {
			new PermissionId(null);
			Assert.fail("Should fail when id is null");
		} catch (Exception e) {
		}
	}

	/**
	 * Test equals.
	 */
	@Test
	public void testEquals() {
		PermissionIdentifier permissions1 = new PermissionId("test");
		Assert.assertEquals(permissions1.getPermissionId(), "test");
		PermissionId permissions2 = new PermissionId("test2");
		Assert.assertNotEquals(permissions2, permissions1);

		permissions2 = new PermissionId("test");
		Assert.assertEquals(permissions2, permissions1);

	}

	/**
	 * Test hash code.
	 */
	@Test
	public void testHashCode() {
		PermissionIdentifier permissions1 = new PermissionId("test");
		PermissionIdentifier permissions2 = new PermissionId("test2");
		Assert.assertNotEquals(permissions2.hashCode(), permissions1.hashCode());

		permissions2 = new PermissionId("test");
		Assert.assertEquals(permissions2.hashCode(), permissions1.hashCode());
	}
}
