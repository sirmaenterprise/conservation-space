package com.sirma.itt.seip.instance.actions.evaluation;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.Monitored;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.sep.instance.actions.group.ActionItem;
import com.sirma.sep.instance.actions.group.ActionMenu;
import com.sirma.sep.instance.actions.group.GroupItem;

/**
 * Used for instances actions evaluation. Uses {@link AuthorityService} to extract the actions for the user and the
 * instance. Also when the actions are evaluated the context is restored. Supports evaluation of actions in flat
 * structure and complex one, which contains menu with any number of sub menus, check the method documentation for more
 * details.
 *
 * @author A. Kunchev
 */
public class InstanceActionsEvaluatior {

	@Inject
	private AuthorityService authorityService;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private StateTransitionManager stateTransitionManager;

	/**
	 * Produces flat action menu. Uses {@link AuthorityService} to evaluate the allowed actions for instance. If the
	 * service returns empty collection, then {@link ActionTypeConstants#NO_ACTIONS_ALLOWED} action is set and returned.
	 *
	 * @param request should contain all of the information needed to evaluate the instance actions
	 * @return set of evaluated actions for the target instance, if there are no actions due to missing permissions over
	 *         the target instance, to the result set is added action 'Not Allowed', which is used as flag
	 * @throws NullPointerException when the request or the placeholder are null
	 */
	public Set<Action> evaluate(InstanceActionsRequest request) {
		Objects.requireNonNull(request, "The request should not be null.");
		Objects.requireNonNull(request.getPlaceholder(), "Placeholder should not be null.");

		Set<Action> actions = authorityService.getAllowedActions(request.getTargetInstance(), request.getPlaceholder());
		if (isEmpty(actions) || isSystemActionsOnly(actions)) {
			actions = addNoAllowedActions(actions);
		}

		return actions;
	}

	private static boolean isSystemActionsOnly(Set<Action> allowedActions) {
		return allowedActions.size() == 1
				&& ActionTypeConstants.READ.equals(allowedActions.iterator().next().getActionId());
	}

	private Set<Action> addNoAllowedActions(Set<Action> actions) {
		EmfAction noAllowed = new EmfAction(ActionTypeConstants.NO_ACTIONS_ALLOWED);
		noAllowed.setPurpose(ActionTypeConstants.NO_ACTIONS_ALLOWED);
		noAllowed.setLabel(labelProvider.getValue("cmf.btn.actions.not_allowed"));
		noAllowed.setDisabled(false);

		Set<Action> copy = new LinkedHashSet<>(actions);
		copy.add(noAllowed);
		return copy;
	}

	/**
	 * Produces complex action menu structure. This structure represents menu with unlimited number of sub menus, which
	 * also can have sub menus. Uses {@link AuthorityService} to evaluate the allowed actions for instance. If the
	 * service returns empty collection, then {@link ActionTypeConstants#NO_ACTIONS_ALLOWED} action is set and returned.
	 *
	 * @param request should contain all of the information needed to evaluate the instance actions
	 * @return {@link ActionMenu} which represents menu with sub menus for the target instance, if there are no actions
	 *         due to missing permissions over the target instance, to the result is added action 'Not Allowed', which
	 *         is used as flag
	 * @throws NullPointerException when the request or the placeholder are null
	 */
	@Monitored({
		@MetricDefinition(name = "action_eval_duration_seconds", type = Type.TIMER, descr = "Action evaluation duration in seconds."),
		@MetricDefinition(name = "action_eval_hit_count", type = Type.COUNTER, descr = "Hit counter on the action evaluation service method.")
	})
	public ActionMenu evaluateAndBuildMenu(InstanceActionsRequest request) {
		Set<Action> actions = evaluate(request);
		Map<String, TransitionGroupDefinition> groups = getGroups(request.getTargetInstance());
		ActionMenu menu = new ActionMenu();
		ActionMenu currentMenu = menu;

		for (Action action : actions) {
			ActionMenu rootMenu = currentMenu;
			Deque<TransitionGroupDefinition> groupsTree = buildGroupTree(action.getGroup(), groups, new ArrayDeque<>());
			while (!groupsTree.isEmpty()) {
				currentMenu = currentMenu.addMenuMember(new GroupItem(groupsTree.pop()));
			}

			currentMenu.addMenuMember(new ActionItem(action));
			currentMenu = rootMenu;
		}

		return menu;
	}

	private Map<String, TransitionGroupDefinition> getGroups(Instance instance) {
		return stateTransitionManager.getActionGroups(instance).stream().collect(
				toMap(TransitionGroupDefinition::getIdentifier, Function.identity()));
	}

	private Deque<TransitionGroupDefinition> buildGroupTree(String actionGroup,
			Map<String, TransitionGroupDefinition> groups, Deque<TransitionGroupDefinition> parents) {
		String parent = actionGroup;
		if (parent == null) {
			return parents;
		}

		TransitionGroupDefinition group = groups.get(parent);
		parents.addFirst(group);
		parent = group.getParent();
		return buildGroupTree(parent, groups, parents);
	}
}