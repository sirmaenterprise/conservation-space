package com.sirmaenterprise.sep.roles.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleActionChanges;
import com.sirmaenterprise.sep.roles.RoleActionChanges.RoleActionChange;
import com.sirmaenterprise.sep.roles.RoleActionModel;
import com.sirmaenterprise.sep.roles.RoleManagement;
import com.sirmaenterprise.sep.roles.events.ActionDefinitionsChangedEvent;

/**
 * Tests for {@link AdministratorPermissionsInitializer}.
 *
 * @author smustafov
 */
public class AdministratorPermissionsInitializerTest {

	@InjectMocks
	private AdministratorPermissionsInitializer initializer;

	@Mock
	private RoleManagement roleManagement;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		List<ActionDefinition> actions = new ArrayList<>();
		actions.add(new ActionDefinition().setId("action1"));
		actions.add(new ActionDefinition().setId("action2"));

		RoleActionModel model = new RoleActionModel();
		actions.forEach(model::add);
		model.add(SecurityModel.BaseRoles.ADMINISTRATOR.getIdentifier(), "action1", true, Arrays.asList("CREATEDBY"));
		model.add(SecurityModel.BaseRoles.ADMINISTRATOR.getIdentifier(), "action2", true, Collections.emptyList());
		model.add(SecurityModel.BaseRoles.MANAGER.getIdentifier(), "action1", true, Arrays.asList("ASSIGNEE"));
		when(roleManagement.getRoleActionModel()).thenReturn(model);
	}

	@Test
	public void should_updateRoleActionsMapping_withAdminRole() throws Exception {
		ArgumentCaptor<RoleActionChanges> argCaptor = ArgumentCaptor.forClass(RoleActionChanges.class);

		initializer.actionsChanged(new ActionDefinitionsChangedEvent());

		verify(roleManagement).updateRoleActionMappings(argCaptor.capture());

		RoleActionChanges roleActionChanges = argCaptor.getValue();
		roleActionChanges.getChanges().forEach(change -> {
			assertEquals(SecurityModel.BaseRoles.ADMINISTRATOR.getIdentifier(), change.getRole());
		});
	}

	@Test
	public void should_updateRoleActionsMapping_withFilters() throws Exception {
		ArgumentCaptor<RoleActionChanges> argCaptor = ArgumentCaptor.forClass(RoleActionChanges.class);

		initializer.actionsChanged(new ActionDefinitionsChangedEvent());

		verify(roleManagement).updateRoleActionMappings(argCaptor.capture());

		RoleActionChanges roleActionChanges = argCaptor.getValue();
		List<RoleActionChange> changes = new ArrayList<>(roleActionChanges.getChanges());

		assertFalse(changes.get(0).getFilters().contains("ASSIGNEE"));
		assertTrue(changes.get(0).getFilters().contains("CREATEDBY"));
		assertTrue(changes.get(1).getFilters().size() == 0);
	}

}
