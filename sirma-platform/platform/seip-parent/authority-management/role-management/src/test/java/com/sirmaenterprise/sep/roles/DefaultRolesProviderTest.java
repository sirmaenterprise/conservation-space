package com.sirmaenterprise.sep.roles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.action.ActionRegistry;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;

public class DefaultRolesProviderTest {

	@InjectMocks
	private DefaultRolesProvider provider;

	@Mock
	private ActionRegistry actionRegistry;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_Build_Map_With_Default_Roles() {
		Map<RoleIdentifier, Role> roles = provider.getDefaultRoles();

		assertEquals(6, roles.size());
		assertTrue(roles.containsKey(SecurityModel.BaseRoles.CONSUMER));
		assertTrue(roles.containsKey(SecurityModel.BaseRoles.CONTRIBUTOR));
		assertTrue(roles.containsKey(SecurityModel.BaseRoles.COLLABORATOR));
		assertTrue(roles.containsKey(SecurityModel.BaseRoles.MANAGER));
		assertTrue(roles.containsKey(SecurityModel.BaseRoles.NO_PERMISSION));
		assertTrue(roles.containsKey(SecurityModel.BaseRoles.CREATOR));
	}

}
