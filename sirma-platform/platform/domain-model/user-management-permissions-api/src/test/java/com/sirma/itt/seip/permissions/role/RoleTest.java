package com.sirma.itt.seip.permissions.role;

import java.util.Collections;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.PermissionIdentifier;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.model.PermissionId;

/**
 * The Class RoleImplTest.
 */
public class RoleTest {

	/**
	 * Test init role impl.
	 */
	@Test
	public void testInitRoleImpl() {
		Assert.assertNotNull(createRole("test"));
	}

	/**
	 * Test get allowed actions.
	 */
	@Test
	public void testGetAllowedActions() {
		Role role = createRole("test");
		PermissionIdentifier permissions = new PermissionId("test");
		Action action = provideAction("test");
		role.addActions(Collections.singletonList(action));
		Assert.assertEquals(1, role.getAllowedActions(null).size());

		Action action2 = provideAction("test2");
		role.addActions(Collections.singletonList(action2));
		Assert.assertEquals(2, role.getAllowedActions(null).size());

		permissions = new PermissionId("test");
		action2 = provideAction("test2");
		role.addActions(Collections.singletonList(action2));
		Set<Action> allowedActions = role.getAllowedActions(null);
		Assert.assertEquals("test1 and test2", 2, allowedActions.size());

		Assert.assertTrue(allowedActions.contains(action));
		Assert.assertTrue(allowedActions.contains(action2));

		permissions = new PermissionId("test3");

		Action action3 = provideAction("test3");
		role.addActions(Collections.singletonList(action3));
		Assert.assertEquals("test1 and test2 and test3", 3, role.getAllowedActions(null).size());

	}

	/**
	 * Test get allowed actions by class.
	 */
	@Test
	public void testGetAllowedActionsByClass() {
		Role role = createRole("test");
		PermissionIdentifier permissions = new PermissionId("test");
		Action action = provideAction("test");
		role.addActions(Collections.singletonList(action));
		Assert.assertEquals(1, role.getAllowedActions(null).size());
		Assert.assertEquals(1, role.getAllowedActions(null).size());
		Action action2 = provideAction("test2");
		role.addActions(Collections.singletonList(action2));
		Assert.assertEquals(2, role.getAllowedActions(null).size());

		permissions = new PermissionId("test");
		action2 = provideAction("test2");
		role.addActions(Collections.singletonList(action2));
		Assert.assertEquals(2, role.getAllowedActions(null).size());
	}

	/**
	 * Test get permissions.
	 */
	@Test
	public void testGetPermissions() {
		Role role = createRole("test");
		PermissionIdentifier permissions = Mockito.mock(PermissionIdentifier.class);
		Mockito.when(permissions.getPermissionId()).thenReturn("test");
		Action action = provideAction("test");
		role.addActions(Collections.singletonList(action));
		Assert.assertEquals(1, role.getAllAllowedActions().size());

		Action action2 = Mockito.mock(Action.class);
		role.addActions(Collections.singletonList(action2));
		Assert.assertEquals(2, role.getAllAllowedActions().size());

		permissions = Mockito.mock(PermissionIdentifier.class);
		Mockito.when(permissions.getPermissionId()).thenReturn("test2");
		action2 = Mockito.mock(Action.class);
		role.addActions(Collections.singletonList(action2));
		Assert.assertEquals(3, role.getAllAllowedActions().size());
	}

	/**
	 * Gets the role id.
	 */
	@Test
	public void testGetRoleId() {
		Role role = createRole("test");
		Assert.assertNotNull(role.getRoleId());
		Assert.assertEquals("test", role.getRoleId().getIdentifier());
	}

	/**
	 * Seal.
	 */
	@Test
	public void testSeal() {
		Role role = createRole("test");
		PermissionIdentifier permissions = Mockito.mock(PermissionIdentifier.class);
		Mockito.when(permissions.getPermissionId()).thenReturn("test");
		Action action = provideAction("test");
		role.addActions(Collections.singletonList(action));
		Assert.assertEquals("Should add when not sealed!", 1, role.getAllowedActions(null).size());

		role = createRole("test");
		role.seal();
		Assert.assertTrue(role.isSealed());
		role.addActions(Collections.singletonList(action));
		Assert.assertEquals("Should fail when sealed!", 0, role.getAllowedActions(null).size());

	}

	private static Action provideAction(String actionId) {
		return new EmfAction(actionId);
	}

	private static Role createRole(String role) {
		RoleIdentifier roleIdentifier = Mockito.mock(RoleIdentifier.class);
		Mockito.when(roleIdentifier.getIdentifier()).thenReturn(role);
		return new Role(roleIdentifier);
	}
}
