package com.sirma.itt.seip.permissions.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.permissions.PermissionModelType;

public class RoleAssignmentsTest {

	private RoleAssignments roleAssignments;

	private static final String MANAGER = "MANAGER";
	private static final String CONSUMER = "CONSUMER";
	private static final String COLLABORATOR = "COLLABORATOR";

	@Before
	public void init() {
		roleAssignments = new RoleAssignments(MANAGER);
	}

	@Test
	public void addAssignmentShouldSetSpecialPermissions() {
		roleAssignments.addAssignment(CONSUMER, PermissionModelType.SPECIAL);

		assertEquals(CONSUMER, roleAssignments.getSpecial());
	}

	@Test
	public void addAssignmentShouldSetLibraryPermissions() {
		roleAssignments.addAssignment(CONSUMER, PermissionModelType.LIBRARY);

		assertEquals(CONSUMER, roleAssignments.getLibrary());
	}

	@Test
	public void addAssignmentShouldSetInheritedPermissions() {
		roleAssignments.addAssignment(CONSUMER, PermissionModelType.INHERITED);

		assertEquals(CONSUMER, roleAssignments.getInherited());
	}

	@Test
	public void addAssignmentShouldProperlyCalculatePermissionsWhenManagerIsProvided() {
		roleAssignments.addAssignment(CONSUMER, PermissionModelType.SPECIAL);
		roleAssignments.addAssignment(MANAGER, PermissionModelType.LIBRARY);
		roleAssignments.addAssignment(COLLABORATOR, PermissionModelType.INHERITED);

		assertEquals(MANAGER, roleAssignments.getActive());
		assertTrue(roleAssignments.isManager());
	}

	@Test
	public void addAssignmentShouldProperlyCalculatePermissionsWhenNoManagerIsProvided() {
		roleAssignments.addAssignment(CONSUMER, PermissionModelType.SPECIAL);
		roleAssignments.addAssignment(COLLABORATOR, PermissionModelType.INHERITED);

		assertEquals(CONSUMER, roleAssignments.getActive());
		assertFalse(roleAssignments.isManager());
	}

}
