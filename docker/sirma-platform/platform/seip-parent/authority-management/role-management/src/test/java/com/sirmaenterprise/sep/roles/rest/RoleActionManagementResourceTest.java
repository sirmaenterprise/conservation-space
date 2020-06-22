package com.sirmaenterprise.sep.roles.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirma.itt.seip.permissions.action.RoleActionFilterService;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleActionChanges;
import com.sirmaenterprise.sep.roles.RoleActionChanges.RoleActionChange;
import com.sirmaenterprise.sep.roles.RoleActionModel;
import com.sirmaenterprise.sep.roles.RoleDefinition;
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Test for {@link RoleActionManagementResource}
 *
 * @author BBonev
 */
public class RoleActionManagementResourceTest {

	private static final ActionDefinition ACTION = new ActionDefinition()
			.setId("action")
				.setActionType("serverAction")
				.setEnabled(true)
				.setImmediate(true)
				.setImagePath("/images/action.jpg")
				.setVisible(true);

	private static final ActionDefinition ACTION_WITH_FILTER = new ActionDefinition()
			.setId("actionWithFilter")
				.setActionType("serveractionWithFilter")
				.setEnabled(true)
				.setImmediate(false)
				.setImagePath("/images/actionWithFilter.jpg")
				.setVisible(true);

	private static final ActionDefinition DISABLED_ACTION = new ActionDefinition()
			.setId("disabledAction")
				.setActionType("serverDisabledAction")
				.setEnabled(false)
				.setVisible(false);

	@InjectMocks
	private RoleActionManagementResource managementResource;

	@Mock
	private RoleManagement roleManagement;
	@Mock
	private UserPreferences userPreferences;
	@Mock
	private LabelProvider labelProvider;

	@Mock
	private RoleActionFilterService filterService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(labelProvider.getLabel(anyString())).then(a -> a.getArgumentAt(0, String.class));
		when(userPreferences.getLanguage()).thenReturn("en");

		RoleActionModel mapping = new RoleActionModel()
				.add(ACTION)
					.add(ACTION_WITH_FILTER)
					.add(DISABLED_ACTION)
					.add(new RoleDefinition(BaseRoles.CONSUMER))
					.add(new RoleDefinition(BaseRoles.COLLABORATOR))
					.add(new RoleDefinition(BaseRoles.ADMINISTRATOR))
					.add(BaseRoles.CONSUMER.getIdentifier(), ACTION.getId(), true, null)
					.add(BaseRoles.CONSUMER.getIdentifier(), ACTION_WITH_FILTER.getId(), true, Arrays.asList("CREATOR"))
					.add(BaseRoles.COLLABORATOR.getIdentifier(), ACTION.getId(), true, null)
					.add(BaseRoles.COLLABORATOR.getIdentifier(), DISABLED_ACTION.getId(), true, null)
					.add(BaseRoles.COLLABORATOR.getIdentifier(), ACTION_WITH_FILTER.getId(), false, null)
					.add(BaseRoles.ADMINISTRATOR.getIdentifier(), ACTION.getId(), true, null);

		when(roleManagement.getRoleActionModel()).thenReturn(mapping);
		when(roleManagement.getActions()).then(a -> Stream.of(ACTION, ACTION_WITH_FILTER, DISABLED_ACTION));
		when(roleManagement.getRoles()).then(a -> Stream.of(new RoleDefinition(BaseRoles.CONSUMER),
				new RoleDefinition(BaseRoles.COLLABORATOR), new RoleDefinition(BaseRoles.ADMINISTRATOR)));

		when(filterService.getFilters()).thenReturn(new HashSet<>(Arrays.asList("CREATOR", "CREATED")));
	}

	@Test
	public void should_ReturnFullRoleActions() throws Exception {
		RoleActionsResponse allRoleActions = managementResource.getAllRoleActions();
		assertNotNull(allRoleActions);
		assertEquals(2, allRoleActions.getRoles().size());
		assertEquals(2, allRoleActions.getActions().size());
		assertEquals(5, allRoleActions.getRoleActions().size());

		// roles are sorted by global id so this should be the first role
		RoleResponse role = allRoleActions.getRoles().get(0);
		assertEquals("CONSUMER", role.getId());
		assertEquals("consumer.label", role.getLabel());
		assertEquals(5, role.getOrder());
		assertTrue(role.isCanRead());
		assertFalse(role.isCanWrite());

		ActionResponse action = allRoleActions.getActions().get(0);
		assertEquals("action", action.getId());
		assertEquals("action.label", action.getLabel());
		assertEquals("action.tooltip", action.getTooltip());
		assertTrue(action.isEnabled());
	}

	@Test
	public void should_generateCorrectChangeSet_onSave() throws Exception {
		List<RoleAction> roleActions = new ArrayList<>();
		roleActions.add(new RoleAction().setAction("action1").setRole("role1").setEnabled(true).setFilters(
				new HashSet<>(Arrays.asList("filter1"))));
		roleActions.add(new RoleAction().setAction("action2").setRole("role1").setEnabled(false));
		roleActions.add(new RoleAction().setAction("action1").setRole("role2").setEnabled(false));
		roleActions.add(new RoleAction().setAction("action2").setRole("role2").setEnabled(true));
		managementResource.saveRoleActionAssigments(roleActions);

		verify(roleManagement).updateRoleActionMappings(argThat(new BaseMatcher<RoleActionChanges>() {

			@Override
			public boolean matches(Object item) {
				Collection<RoleActionChange> changes = ((RoleActionChanges) item).getChanges();
				Iterator<RoleActionChange> it = changes.iterator();
				RoleActionChange change = it.next();
				assertEquals("action1", change.getAction());
				assertEquals("role1", change.getRole());
				assertTrue(change.isActive());
				assertFalse(change.getFilters().isEmpty());
				return changes.size() == 4;
			}

			@Override
			public void describeTo(Description description) {
				description.appendDescriptionOf(this);
			}
		}));
	}

	@Test
	public void should_doNothing_onEmptyRequest() throws Exception {
		List<RoleAction> roleActions = new ArrayList<>();
		managementResource.saveRoleActionAssigments(roleActions);

		verify(roleManagement, never()).updateRoleActionMappings(any());
	}

	@Test
	public void should_provideAvailableFiltersSorted() throws Exception {
		List<String> filters = managementResource.getFilers();
		assertEquals(Arrays.asList("CREATED", "CREATOR"), filters);
	}
}
