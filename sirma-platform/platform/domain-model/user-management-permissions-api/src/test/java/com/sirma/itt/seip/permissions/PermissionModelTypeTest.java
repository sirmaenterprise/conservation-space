package com.sirma.itt.seip.permissions;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link PermissionModelTypeTest}.
 *
 * @author Adrian Mitev
 */
public class PermissionModelTypeTest {

	@Test
	public void shouldBeDefinedWhenSomethingIsSet() {
		Assert.assertTrue(new PermissionModelType(true, false, false).isDefined());
		Assert.assertTrue(new PermissionModelType(false, true, false).isDefined());
		Assert.assertTrue(new PermissionModelType(false, false, true).isDefined());

	}

	@Test
	public void shouldBeUnDefinedfWhenNothingIsSet() {
		Assert.assertFalse(new PermissionModelType(false, false, false).isDefined());
	}

}
