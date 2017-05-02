package com.sirmaenterprise.sep.roles.persistence;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.definition.event.AllDefinitionsLoaded;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.action.ActionProvider;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;
import com.sirmaenterprise.sep.roles.RoleDefinition;
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Test for {@link PermissionsDefinitionsInitializer}
 *
 * @since 2017-03-28
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class PermissionsDefinitionsInitializerTest {
	@InjectMocks
	private PermissionsDefinitionsInitializer initializer;

	@Mock
	private RoleManagement roleManagement;
	@Mock
	private ActionProvider actionProvider;
	@Spy
	private List<ActionProvider> actionProviders = new ArrayList<>();
	@Mock
	private RoleProviderExtension roleProviderExtension;
	@Spy
	private List<RoleProviderExtension> roleProviders = new ArrayList<>();

	@Mock
	private LabelService labelService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		actionProviders.clear();
		actionProviders.add(actionProvider);

		roleProviders.clear();
		roleProviders.add(roleProviderExtension);

		Map<String, Action> actions = new HashMap<>();
		EmfAction emfAction = new EmfAction("action1");
		emfAction.setLabel(emfAction.getActionId());
		emfAction.setTooltip(emfAction.getActionId());
		actions.put("action1", emfAction);
		actions.put("action2", new EmfAction("action2"));
		when(actionProvider.provide()).thenReturn(actions);

		when(roleProviderExtension.getModel(anyMap())).then(a -> {
			Map<RoleIdentifier, Role> roles = a.getArgumentAt(0, Map.class);
			Role role = new Role(SecurityModel.BaseRoles.CONSUMER);
			role.addActions(Arrays.asList(new EmfAction("action1")));
			roles.put(SecurityModel.BaseRoles.CONSUMER, role);
			return roles;
		});
	}

	@Test
	public void should_saveAllPermissionModelOnEvent_ifRoleModelIsEmpty() throws Exception {
		when(roleManagement.getRoles()).thenAnswer(answer -> Stream.empty());
		initializer.onDefinitionChange(new AllDefinitionsLoaded());
		verify(roleManagement).saveActions(anyCollection());
		verify(roleManagement).saveRoles(anyCollection());
		verify(roleManagement).updateRoleActionMappings(any());
	}

	@Test
	public void should_saveOnlyActions_ifRoleModelAlreadyPersisted() throws Exception {
		when(roleManagement.getRoles()).thenAnswer(answer -> Stream.of(Arrays.asList(new RoleDefinition())));
		initializer.onDefinitionChange(new AllDefinitionsLoaded());
		verify(roleManagement).saveActions(anyCollection());
		verify(roleManagement, times(0)).saveRoles(anyCollection());
		verify(roleManagement, times(0)).updateRoleActionMappings(any());
	}

	@Test
	public void should_copyActionLabelAndTooltip() throws Exception {
		LabelDefinition labelDefinition = mock(LabelDefinition.class);
		when(labelService.getLabel("action1")).thenReturn(labelDefinition);
		when(roleManagement.getRoles()).thenAnswer(answer -> Stream.empty());
		ArgumentCaptor<LabelDefinition> argCaptor = ArgumentCaptor.forClass(LabelDefinition.class);

		initializer.onDefinitionChange(new AllDefinitionsLoaded());

		verify(labelService, times(2)).saveLabel(argCaptor.capture());
		List<LabelDefinition> allValues = argCaptor.getAllValues();
		assertEquals("action1.label", allValues.get(0).getIdentifier());
		assertEquals("action1.tooltip", allValues.get(1).getIdentifier());
	}
}
