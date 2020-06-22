package com.sirma.itt.seip.permissions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;

public class AbstractRoleProviderExtensionTest {

	@Test
	public void testMerge() throws Exception {

		Map<RoleIdentifier, Role> data = new HashMap<>();

		Role viewer = new Role(BaseRoles.VIEWER);
		viewer.addActions(Arrays.asList(Mockito.mock(EmfAction.class), Mockito.mock(EmfAction.class)));
		Role admin = new Role(BaseRoles.VIEWER);
		admin.addActions(Arrays.asList(Mockito.mock(EmfAction.class), Mockito.mock(EmfAction.class)));

		data.put(BaseRoles.ADMINISTRATOR, admin);
		data.put(BaseRoles.VIEWER, viewer);
		AbstractRoleProviderExtension.merge(data, BaseRoles.VIEWER, BaseRoles.ADMINISTRATOR);
		Assert.assertEquals(4, data.get(BaseRoles.ADMINISTRATOR).getAllAllowedActions().size());

		data = new HashMap<>();
		data.put(BaseRoles.VIEWER, viewer);
		AbstractRoleProviderExtension.merge(data, BaseRoles.VIEWER, BaseRoles.ADMINISTRATOR);
		Assert.assertEquals(2, data.get(BaseRoles.ADMINISTRATOR).getAllAllowedActions().size());

		data = new HashMap<>();
		AbstractRoleProviderExtension.merge(data, BaseRoles.VIEWER, BaseRoles.ADMINISTRATOR);
		Assert.assertEquals(0, data.size());
	}

}
