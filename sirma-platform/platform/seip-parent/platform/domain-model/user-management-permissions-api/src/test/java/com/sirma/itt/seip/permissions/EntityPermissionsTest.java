package com.sirma.itt.seip.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.sirma.itt.seip.permissions.EntityPermissions.Assignment;
import com.sirma.itt.seip.permissions.model.RoleId;

/**
 * Tests for {@link EntityPermissions}.
 *
 * @author smustafov
 */
public class EntityPermissionsTest {

	@Test
	public void should_CorrectlyAddAssignments() {
		Assignment assignment1 = new Assignment("user", "consumer");
		Assignment assignment2 = new Assignment("admin", "manager");

		EntityPermissions entityPermissions = new EntityPermissions("instance-id");
		entityPermissions.addAssignment(assignment1.getAuthority(), assignment1.getRole());
		entityPermissions.addAssignment(assignment2.getAuthority(), assignment2.getRole());

		List<Assignment> assignments = entityPermissions.getAssignments().collect(Collectors.toList());
		assertEquals(2, assignments.size());
		assertEquals(assignment1, assignments.get(0));
		assertEquals(assignment2, assignments.get(1));
	}

	@Test
	public void should_ReturnEmptyStream_When_NoAssignments() {
		EntityPermissions entityPermissions = new EntityPermissions("instance-id");
		assertEquals(0, entityPermissions.getAssignments().count());
	}

	@Test
	public void equals_Should_ReturnTrue_When_PassedTheSameObject() {
		Assignment assignment = new Assignment("user", "consumer");
		assertTrue(assignment.equals(assignment));
	}

	@Test
	public void equals_Should_ReturnFalse_When_PassedObjectOfAnotherClass() {
		Assignment assignment = new Assignment("user", "consumer");
		assertFalse(assignment.equals(new RoleId("id", 100)));
	}

	@Test
	public void equals_Should_ReturnFalse_When_AuthorityIsDifferent() {
		Assignment assignment1 = new Assignment("user", "consumer");
		Assignment assignment2 = new Assignment("admin", "consumer");
		assertFalse(assignment1.equals(assignment2));
	}

	@Test
	public void equals_Should_ReturnFalse_When_RoleIsDifferent() {
		Assignment assignment1 = new Assignment("user", "consumer");
		Assignment assignment2 = new Assignment("user", "contributor");
		assertFalse(assignment1.equals(assignment2));
	}

	@Test
	public void equals_Should_ReturnTrue_When_AuthorityAndRoleAreSame() {
		Assignment assignment1 = new Assignment("user", "contributor");
		Assignment assignment2 = new Assignment("user", "contributor");
		assertTrue(assignment1.equals(assignment2));
	}

}
