package com.sirma.itt.seip.instance.actions.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.sep.instance.actions.group.ActionMenu;
import com.sirma.sep.instance.actions.group.ActionMenuMember;
import com.sirma.sep.instance.actions.group.VisitableMenu;
import com.sirma.sep.instance.actions.group.Visitor;

/**
 * Tests for {@link InstanceActionsList}.
 *
 * @author A. Kunchev
 */
public class InstanceActionsListTest implements Visitor {

	@InjectMocks
	private InstanceActionsList actions;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private AuthorityService authorityService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private StateTransitionManager stateTransitionManager;

	@Before
	public void setup() {
		actions = new InstanceActionsList();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals(ActionsListRequest.ACTIONS_LIST, actions.getName());
	}

	@Test(expected = BadRequestException.class)
	public void getActions_nullRequest() {
		actions.perform(null);
	}

	@Test(expected = BadRequestException.class)
	public void getActions_nullId() {
		actions.perform(buildRequest(null, "contextId", "placeholder", Arrays.asList("path1", "path2"), true));
	}

	@Test(expected = BadRequestException.class)
	public void getActions_emptyId() {
		actions.perform(buildRequest("", "contextId", "placeholder", Arrays.asList("path1", "path2"), true));
	}

	@Test(expected = BadRequestException.class)
	public void getActions_nullPlaceholder_emptySet() {
		actions.perform(buildRequest("id", "contextId", null, Arrays.asList("path1", "path2"), true));
	}

	@Test(expected = InstanceNotFoundException.class)
	public void getActions_nullInstance_emptySet() {
		String id = "id";
		when(instanceTypeResolver.resolveReference(id)).thenReturn(Optional.empty());
		actions.perform(buildRequest(id, "contextId", "placeholder", Arrays.asList("path1", "path2"), true));
	}

	@Test
	public void getActions_noActions_noPermissionAction() {
		Set<Action> result = getActionsInternal(Collections.emptySet());
		assertEquals(1, result.size());
		assertEquals(ActionTypeConstants.NO_ACTIONS_ALLOWED, result.iterator().next().getActionId());
		verify(labelProvider).getValue("cmf.btn.actions.not_allowed");
	}

	@Test
	public void getActions_withActions_() {
		Set<Action> result = getActionsInternal(Collections.singleton(new EmfAction(ActionTypeConstants.CREATE)));
		assertEquals(1, result.size());
		assertEquals(ActionTypeConstants.CREATE, result.iterator().next().getActionId());
	}

	@SuppressWarnings("boxing")
	@Test
	public void buildMenu_withActionsAndGroups_() {
		List<TransitionGroupDefinition> groups = new ArrayList<>();
		groups.add(buildGroup("g1", null, 3));
		groups.add(buildGroup("g2", "g1", 1));
		groups.add(buildGroup("g3", null, 1));

		Set<Action> actionSet = new HashSet<>();
		actionSet.add(buildAction("a1", null, 2));
		actionSet.add(buildAction("a2", "g1", 1));
		actionSet.add(buildAction("a3", null, 1));

		ActionMenu menu = buildMenu(actionSet, groups);
		visitMenu(menu);
		assertEquals(3, visitMenuMembers(menu).size());

		visitMenu(visitMenuMembers(menu).get(0));
		assertEquals("a3", visitMenuMember(visitMenuMembers(menu).get(0)).getIdentifier());

		visitMenu(visitMenuMembers(menu).get(1));
		assertEquals("a1", visitMenuMember(visitMenuMembers(menu).get(1)).getIdentifier());

		ActionMenu g1 = visitMenuMembers(menu).get(2);
		visitMenu(g1);
		assertEquals("g1", visitMenuMember(g1).getIdentifier());
		assertEquals(1, visitMenuMembers(g1).size());

		visitMenu(visitMenuMembers(g1).get(0));
		assertEquals("a2", visitMenuMember(visitMenuMembers(g1).get(0)).getIdentifier());
	}

	@SuppressWarnings("boxing")
	@Test
	public void buildMenu_withEmptyGroups_() {
		List<TransitionGroupDefinition> groups = new ArrayList<>();
		groups.add(buildGroup("g1", null, 1));
		groups.add(buildGroup("g2", "g1", 1));
		groups.add(buildGroup("g3", "g2", 1));

		Set<Action> actionSet = new HashSet<>();

		ActionMenu menu = buildMenu(actionSet, groups);
		visitMenu(menu);
		assertEquals(1, visitMenuMembers(menu).size());
		visitMenu(visitMenuMembers(menu).get(0));
		assertEquals(ActionTypeConstants.NO_ACTIONS_ALLOWED,
				visitMenuMember(visitMenuMembers(menu).get(0)).getIdentifier());
	}

	@Test
	public void buildMenu_getMenuMembers_visits() {
		List<TransitionGroupDefinition> groups = new ArrayList<>();
		Set<Action> actionSet = new HashSet<>();

		ActionMenu visitedMenu = buildMenu(actionSet, groups);
		visitMenu(visitedMenu);
		assertEquals(1, visitMenuMembers(visitedMenu).size());

		ActionMenu notVisitedMenu = buildMenu(actionSet, groups);
		assertEquals(0, visitMenuMembers(notVisitedMenu).size());
	}

	@Test
	public void buildMenu_getMenuMember_visits() {
		List<TransitionGroupDefinition> groups = new ArrayList<>();
		Set<Action> actionSet = new HashSet<>();
		actionSet.add(buildAction("a1", null, null));

		ActionMenu menu = buildMenu(actionSet, groups);
		visitMenu(menu);
		assertNull(visitMenuMembers(menu).get(0).getMenuMember(this));

		visitMenu(visitMenuMembers(menu).get(0));
		assertNotNull(visitMenuMembers(menu).get(0).getMenuMember(this));
	}

	@SuppressWarnings("unchecked")
	private Set<Action> getActionsInternal(Set<Action> actionsToReturn) {
		String placeholder = "placeholder";
		String id = "id";
		Instance instance = new EmfInstance();
		instance.setId(id);
		when(instanceTypeResolver.resolveReference(id))
		.thenReturn(Optional.of(new InstanceReferenceMock(id, new DataTypeDefinitionMock(instance), instance)));
		when(instanceTypeResolver.resolveReference("contextId")).thenReturn(Optional.of(new InstanceReferenceMock()));
		when(authorityService.getAllowedActions(instance, placeholder)).thenReturn(actionsToReturn);

		return (Set<Action>) actions
				.perform(buildRequest(id, "contextId", placeholder, Arrays.asList("path1", "path2"), true));
	}

	@SuppressWarnings("unchecked")
	private ActionMenu buildMenu(Set<Action> actionsToReturn, List<TransitionGroupDefinition> groups) {
		String placeholder = "placeholder";
		String id = "id";
		Instance instance = new EmfInstance();
		instance.setId(id);
		when(instanceTypeResolver.resolveReference(id))
		.thenReturn(Optional.of(new InstanceReferenceMock(id, new DataTypeDefinitionMock(instance), instance)));
		when(instanceTypeResolver.resolveReference("contextId")).thenReturn(Optional.of(new InstanceReferenceMock()));
		when(authorityService.getAllowedActions(instance, placeholder)).thenReturn(actionsToReturn);
		when(stateTransitionManager.getActionGroups(instance)).thenReturn(groups);

		return (ActionMenu) actions
				.perform(buildRequest(id, "contextId", placeholder, Arrays.asList("path1", "path2"), false));
	}

	private static ActionsListRequest buildRequest(String id, String contextId, String placeholder,
			List<Serializable> path, boolean flatMenu) {
		ActionsListRequest request = new ActionsListRequest();
		request.setTargetId(id);
		request.setContextId(contextId);
		request.setPlaceholder(placeholder);
		request.setContextPath(path);
		request.setFlatMenuType(flatMenu);
		return request;
	}

	private static ActionsListRequest buildRequestMenu(String id, String contextId, String placeholder,
			List<Serializable> path) {
		ActionsListRequest request = new ActionsListRequest();
		request.setTargetId(id);
		request.setContextId(contextId);
		request.setPlaceholder(placeholder);
		request.setContextPath(path);
		request.setFlatMenuType(false);
		return request;
	}

	private static TransitionGroupDefinition buildGroup(String identifier, String parent, Integer order) {
		TransitionGroupDefinition group = Mockito.mock(TransitionGroupDefinition.class);
		Mockito.when(group.getIdentifier()).thenReturn(identifier);
		Mockito.when(group.getLabel()).thenReturn("label_" + identifier);
		Mockito.when(group.getParent()).thenReturn(parent);
		Mockito.when(group.getType()).thenReturn("menu");
		Mockito.when(group.getOrder()).thenReturn(order);
		return group;
	}

	private static EmfAction buildAction(String identifier, String group, Integer order) {
		EmfAction emfAction = Mockito.spy(new EmfAction(identifier));
		emfAction.setConfirmationMessage("message");
		emfAction.setDisabled(false);
		emfAction.setImmediate(true);
		emfAction.setLabel("label");
		emfAction.setGroup(group);
		Mockito.when(emfAction.getOrder()).thenReturn(order);
		return emfAction;
	}

	@Override
	public void visitMenu(VisitableMenu menu) {
		menu.acceptVisitor(this);

	}

	@Override
	public List<ActionMenu> visitMenuMembers(VisitableMenu menu) {
		return menu.getMenuMembers(this);
	}

	@Override
	public ActionMenuMember visitMenuMember(VisitableMenu menu) {
		return menu.getMenuMember(this);
	}

}
