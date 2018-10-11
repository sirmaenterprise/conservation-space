package com.sirma.itt.seip.permissions.sync.batch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sirma.itt.seip.permissions.sync.batch.PermissionsDiff.PermissionsDiffEntry;

/**
 * Tests for {@link PermissionsDiff}.
 *
 * @author smustafov
 */
public class PermissionsDiffTest {

	@Test
	public void equals_Should_ReturnTrue_When_CheckingTheSamePermissionsDiff() {
		PermissionsDiffEntry diff = new PermissionsDiffEntry("user", "consumer");
		assertTrue(diff.equals(diff));
	}

	@Test
	public void equals_Should_ReturnFalse_When_PassedNull() {
		PermissionsDiffEntry diff = new PermissionsDiffEntry("user", "consumer");
		assertFalse(diff.equals(null));
	}

	@Test
	public void equals_Should_ReturnFalse_When_PassedObjectFromDifferentClass() {
		PermissionsDiffEntry diff = new PermissionsDiffEntry("user", "consumer");
		assertFalse(diff.equals("consumer"));
	}

	@Test
	public void equals_Should_ReturnTrue_When_AuthorityAndRoleOfAnotherPermissionsDiffIsTheSame() {
		PermissionsDiffEntry diff1 = new PermissionsDiffEntry("user", "consumer");
		PermissionsDiffEntry diff2 = new PermissionsDiffEntry("user", "consumer");
		assertTrue(diff1.equals(diff2));
	}

	@Test
	public void equals_Should_ReturnFalse_When_AuthorityAndRoleOfAnotherPermissionsDiffIsNotTheSame() {
		PermissionsDiffEntry diff1 = new PermissionsDiffEntry("user", "consumer");
		PermissionsDiffEntry diff2 = new PermissionsDiffEntry("regularuser", "manager");
		assertFalse(diff1.equals(diff2));
	}

	@Test
	public void equals_Should_ReturnFalse_When_RoleOfAnotherPermissionsDiffIsNotTheSame() {
		PermissionsDiffEntry diff1 = new PermissionsDiffEntry("user", "consumer");
		PermissionsDiffEntry diff2 = new PermissionsDiffEntry("user", "collaborator");
		assertFalse(diff1.equals(diff2));
	}

	@Test
	public void equals_Should_ReturnFalse_When_AuthorityOfAnotherPermissionsDiffIsNotTheSame() {
		PermissionsDiffEntry diff1 = new PermissionsDiffEntry("user", "consumer");
		PermissionsDiffEntry diff2 = new PermissionsDiffEntry("regularuser", "consumer");
		assertFalse(diff1.equals(diff2));
	}

	@Test
	public void equals_Should_ReturnFalse_When_AuthorityIsNull() {
		PermissionsDiffEntry diff1 = new PermissionsDiffEntry(null, "consumer");
		PermissionsDiffEntry diff2 = new PermissionsDiffEntry("regularuser", "consumer");
		assertFalse(diff1.equals(diff2));
	}

	@Test
	public void equals_Should_ReturnFalse_When_AuthorityIsNullInBothDiffAndRolesAreNotSame() {
		PermissionsDiffEntry diff1 = new PermissionsDiffEntry(null, "consumer");
		PermissionsDiffEntry diff2 = new PermissionsDiffEntry(null, "contributor");
		assertFalse(diff1.equals(diff2));
	}

	@Test
	public void equals_Should_ReturnTrue_When_AuthorityIsNullInBothDiffAndRolesAreSame() {
		PermissionsDiffEntry diff1 = new PermissionsDiffEntry(null, "contributor");
		PermissionsDiffEntry diff2 = new PermissionsDiffEntry(null, "contributor");
		assertTrue(diff1.equals(diff2));
	}

}
