package com.sirma.itt.seip.instance.actions.evaluation;

import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.sep.instance.actions.group.ActionMenu;

/**
 * Test for {@link InstanceActionsEvaluatior}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class InstanceActionsEvaluatiorTest {

	@InjectMocks
	private InstanceActionsEvaluatior evaluatior;

	@Mock
	private AuthorityService authorityService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private StateTransitionManager stateTransitionManager;

	@Test(expected = NullPointerException.class)
	public void evaluate_nullRequest() {
		evaluatior.evaluate(null);
	}

	@Test(expected = NullPointerException.class)
	public void evaluate_nullPlaceholder() {
		evaluatior.evaluate(new InstanceActionsRequest(new EmfInstance()));
	}

	@Test
	public void evaluate_noPermissions() {
		when(authorityService.getAllowedActions(any(), anyString())).thenReturn(emptySet());
		Set<Action> actions = evaluatior.evaluate(new InstanceActionsRequest(new EmfInstance()).setPlaceholder(""));
		assertEquals(1, actions.size());
		assertEquals(ActionTypeConstants.NO_ACTIONS_ALLOWED, actions.iterator().next().getActionId());
	}

	@Test
	public void evaluate_onlySystemActions() {
		when(authorityService.getAllowedActions(any(), anyString()))
				.thenReturn(Collections.singleton(new EmfAction(ActionTypeConstants.READ)));
		Set<Action> actions = evaluatior.evaluate(new InstanceActionsRequest(new EmfInstance()).setPlaceholder(""));
		assertEquals(2, actions.size());
		Iterator<Action> actionsIterator = actions.iterator();
		assertEquals(ActionTypeConstants.READ, actionsIterator.next().getActionId());
		assertEquals(ActionTypeConstants.NO_ACTIONS_ALLOWED, actionsIterator.next().getActionId());
	}

	@Test
	public void evaluate_withPermissions() {
		when(authorityService.getAllowedActions(any(), anyString()))
				.thenReturn(Collections.singleton(new EmfAction(ActionTypeConstants.EDIT_DETAILS)));
		Set<Action> actions = evaluatior.evaluate(new InstanceActionsRequest(new EmfInstance()).setPlaceholder(""));
		assertEquals(1, actions.size());
		assertEquals(ActionTypeConstants.EDIT_DETAILS, actions.iterator().next().getActionId());
	}

	@Test
	public void evaluateAndBuildMenu() {
		List<TransitionGroupDefinition> groups = new ArrayList<>(3);
		groups.add(buildGroup("g1", null, 3));
		groups.add(buildGroup("g2", "g1", 1));
		groups.add(buildGroup("g3", null, 1));

		Set<Action> actions = new HashSet<>(3);
		actions.add(buildAction("a1", null, 2));
		actions.add(buildAction("a2", "g1", 1));
		actions.add(buildAction("a3", null, 1));

		when(authorityService.getAllowedActions(any(), anyString())).thenReturn(actions);
		when(stateTransitionManager.getActionGroups(any())).thenReturn(groups);
		InstanceActionsRequest request = new InstanceActionsRequest(new EmfInstance()).setPlaceholder("");
		ActionMenu menu = evaluatior.evaluateAndBuildMenu(request);
		assertNotNull(menu);
	}

	private static TransitionGroupDefinition buildGroup(String identifier, String parent, Integer order) {
		TransitionGroupDefinition group = mock(TransitionGroupDefinition.class);
		when(group.getIdentifier()).thenReturn(identifier);
		when(group.getLabel()).thenReturn("label_" + identifier);
		when(group.getParent()).thenReturn(parent);
		when(group.getType()).thenReturn("menu");
		when(group.getOrder()).thenReturn(order);
		return group;
	}

	private static EmfAction buildAction(String identifier, String group, Integer order) {
		EmfAction emfAction = spy(new EmfAction(identifier));
		emfAction.setGroup(group);
		when(emfAction.getOrder()).thenReturn(order);
		return emfAction;
	}

}
