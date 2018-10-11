package com.sirma.itt.seip.permissions.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorManagerService;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Tests for {@link AuthorityServiceImpl}.
 *
 * @author smustafov
 */
public class AuthorityServiceImplTest {

	@InjectMocks
	private AuthorityServiceImpl authorityService;

	@Mock
	private EventService eventService;

	@Mock
	private ResourceService resourceService;

	@Mock
	private ActionRegistry actionRegistry;

	@Mock
	private RoleEvaluatorManagerService roleEvaluatorManagerService;

	@Mock
	private SecurityConfiguration securityConfiguration;

	@Mock
	private SecurityContext securityContext;

	private Instance instance = createInstance("instance-id-1");
	private RoleEvaluator<Instance> roleEvaluator = mock(RoleEvaluator.class);

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);

		when(roleEvaluatorManagerService.getRootEvaluator(instance)).thenReturn(roleEvaluator);
		when(securityContext.getAuthenticated()).thenReturn(new EmfUser());
	}

	@Test(expected = EmfRuntimeException.class)
	public void getAllowedActionNames_shouldThrowException_whenInstanceIsNull() {
		authorityService.getAllowedActionNames(null, null);
	}

	@Test(expected = EmfRuntimeException.class)
	public void getAllowedActionNames_shouldThrowException_whenRoleEvaluatorIsNull() {
		when(roleEvaluatorManagerService.getRootEvaluator(instance)).thenReturn(null);
		authorityService.getAllowedActionNames(instance, null);
	}

	@Test
	public void getAllowedActionNames_shouldReturnEmptySet_whenUserRoleCannotBeDetermined() {
		Set<String> actionNames = authorityService.getAllowedActionNames(instance, null);
		assertTrue(actionNames.isEmpty());
	}

	@Test
	public void getAllowedActionNames_shouldHaveReadAction_whenItsAllowedForRoleAndNoInstanceActions() {
		Action readAction = createAction(ActionTypeConstants.READ);
		Role role = createRole(SecurityModel.BaseRoles.CONSUMER, Arrays.asList(readAction));
		Pair<Role, RoleEvaluator<Instance>> pair = createRolePair(role, roleEvaluator);

		when(roleEvaluator.evaluate(eq(instance), any(Resource.class), eq(null))).thenReturn(pair);
		when(roleEvaluator.filterActions(eq(instance), any(Resource.class), eq(role))).thenReturn(new HashSet<>());

		Set<String> actionNames = authorityService.getAllowedActionNames(instance, null);
		assertEquals(1, actionNames.size());
		assertEquals(ActionTypeConstants.READ, actionNames.iterator().next());
	}

	@Test
	public void getAllowedActionNames_shouldNotFailOnUnmodifiableActionsCollection() {
		Action readAction = createAction(ActionTypeConstants.READ);
		Role role = createRole(SecurityModel.BaseRoles.CONSUMER, Arrays.asList(readAction));
		Pair<Role, RoleEvaluator<Instance>> pair = createRolePair(role, roleEvaluator);

		when(roleEvaluator.evaluate(eq(instance), any(Resource.class), eq(null))).thenReturn(pair);
		when(roleEvaluator.filterActions(eq(instance), any(Resource.class), eq(role))).thenReturn(Collections.emptySet());

		Set<String> actionNames = authorityService.getAllowedActionNames(instance, null);
		assertEquals(1, actionNames.size());
		assertEquals(ActionTypeConstants.READ, actionNames.iterator().next());
	}

	@Test
	public void getAllowedActionNames_shouldHaveReadAction_whenItsAllowedForRole() {
		Action readAction = createAction(ActionTypeConstants.READ);
		Role role = createRole(SecurityModel.BaseRoles.CONSUMER, Arrays.asList(readAction));
		Pair<Role, RoleEvaluator<Instance>> pair = createRolePair(role, roleEvaluator);
		Set<Action> actions = new HashSet<>();
		actions.add(createAction(ActionTypeConstants.APPROVE));

		when(roleEvaluator.evaluate(eq(instance), any(Resource.class), eq(null))).thenReturn(pair);
		when(roleEvaluator.filterActions(eq(instance), any(Resource.class), eq(role))).thenReturn(actions);

		Set<String> actionNames = authorityService.getAllowedActionNames(instance, null);
		assertEquals(2, actionNames.size());
		assertTrue(actionNames.contains(ActionTypeConstants.READ));
		assertTrue(actionNames.contains(ActionTypeConstants.APPROVE));
	}

	@Test
	public void getAllowedActionNames_shouldNotHaveReadAction_whenItsNotAllowedForRole() {
		Role role = createRole(SecurityModel.BaseRoles.NO_PERMISSION, new ArrayList<>());
		Pair<Role, RoleEvaluator<Instance>> pair = createRolePair(role, roleEvaluator);
		Set<Action> actions = new HashSet<>();
		actions.add(createAction(ActionTypeConstants.APPROVE));

		when(roleEvaluator.evaluate(eq(instance), any(Resource.class), eq(null))).thenReturn(pair);
		when(roleEvaluator.filterActions(eq(instance), any(Resource.class), eq(role))).thenReturn(actions);

		Set<String> actionNames = authorityService.getAllowedActionNames(instance, null);
		assertEquals(1, actionNames.size());
		assertTrue(actionNames.contains(ActionTypeConstants.APPROVE));
	}

	private static Instance createInstance(String id) {
		Instance instance = new EmfInstance();
		instance.setId(id);
		instance.setIdentifier(id);
		instance.setType(new ClassInstance());
		return instance;
	}

	private static Action createAction(String id) {
		EmfAction action = new EmfAction(id);
		action.setFilters(Collections.emptyList());
		action.seal();
		return action;
	}

	private static Role createRole(RoleIdentifier id, List<Action> actions) {
		return new Role(id, new HashSet<>(actions));
	}

	private static Pair<Role, RoleEvaluator<Instance>> createRolePair(Role role, RoleEvaluator<Instance> evaluator) {
		return new Pair<>(role, evaluator);
	}

}
