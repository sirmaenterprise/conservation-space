package com.sirma.itt.seip.permissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@link PermissionAssignmentChange}.
 *
 * @author Adrian Mitev
 */
public class PermissionAssignmentChangeTest {

	@Test
	public void isNewAssignmentChangeShouldBeTrueWhenTheOldRoleIsNullAndNewIsNot() {
		PermissionAssignmentChange change = new PermissionAssignmentChange("test", null, "test123");
		assertTrue(change.isNewAssignmentChange());

		change = new PermissionAssignmentChange("test", null, null);
		assertFalse(change.isNewAssignmentChange());

		change = new PermissionAssignmentChange("test", "test123", null);
		assertFalse(change.isNewAssignmentChange());
	}

	@Test
	public void isRemoveAssignmentChangeShouldBeTrueWhenTheOldRoleIsNotNullAndNewIsNull() {
		PermissionAssignmentChange change = new PermissionAssignmentChange("test", "test123", null);
		assertTrue(change.isRemoveAssignmentChange());

		change = new PermissionAssignmentChange("test", null, null);
		assertFalse(change.isRemoveAssignmentChange());

		change = new PermissionAssignmentChange("test", null, "test123");
		assertFalse(change.isRemoveAssignmentChange());
	}

}
