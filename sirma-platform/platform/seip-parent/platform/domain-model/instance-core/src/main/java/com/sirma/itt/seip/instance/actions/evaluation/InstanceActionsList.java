package com.sirma.itt.seip.instance.actions.evaluation;

import static com.sirma.itt.seip.instance.actions.evaluation.ActionsListRequest.ACTIONS_LIST;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.sep.instance.actions.group.ActionItem;
import com.sirma.sep.instance.actions.group.ActionMenu;
import com.sirma.sep.instance.actions.group.GroupItem;

/**
 * Used for instances actions evaluation. Uses {@link AuthorityService} to extract the actions for the user and the
 * instance. Also when the actions are evaluated the context is restored. Builds two types of response depending of
 * request: set of {@link Action} objects or {@link ActionMenu} object.
 *
 * @author A. Kunchev
 */
@Extension(target = com.sirma.itt.seip.instance.actions.Action.TARGET_NAME, enabled = true, order = 20)
public class InstanceActionsList implements com.sirma.itt.seip.instance.actions.Action<ActionsListRequest> {

	@Inject
	private AuthorityService authorityService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private StateTransitionManager stateTransitionManager;

	@Override
	public String getName() {
		return ACTIONS_LIST;
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(ActionsListRequest request) {
		// read only operation
		return false;
	}

	/**
	 * Based on request produces grouped or flat action menu. Uses {@link AuthorityService} to evaluate the actions for
	 * instance. If the service returns empty collection, then NO_PERMISSIONS action is set and returned. This method
	 * also resolves the context for the current instance.
	 *
	 * @param request
	 * 		the {@link ActionsListRequest} which should contain all of the information needed to evaluate the
	 * 		instance actions
	 * @return set or {@link ActionMenu} of evaluated actions for the instance, if there are any or NO_PERMISSIONS
	 * if the internal service returns empty collection
	 * @throws BadRequestException
	 * 		when: <br />
	 * 		the request object is null <br />
	 * 		the id or the placeholder are null or empty <br />
	 * 		cannot find instance for the passed id <br />
	 */
	@Override
	public Object perform(ActionsListRequest request) {
		if (request == null) {
			throw new BadRequestException("Invalid request. The requst object is null.");
		}

		String targetId = (String) request.getTargetId();
		if (StringUtils.isBlank(targetId) || request.getPlaceholder() == null) {
			throw new BadRequestException("Invalid request. The id of the instance or the placeholder are missing!");
		}

		Instance instance = instanceTypeResolver.resolveReference(targetId)
				.map(InstanceReference::toInstance)
				.orElseThrow(() -> new InstanceNotFoundException(targetId));

		Set<Action> allowedActions = authorityService.getAllowedActions(instance, request.getPlaceholder());

		if (CollectionUtils.isEmpty(allowedActions) || isSystemActionsOnly(allowedActions)) {
			allowedActions = addNoAllowedAction(allowedActions);
		}

		if (request.getFlatMenuType()) {
			return allowedActions;
		}
		return buildMenu(allowedActions, instance);
	}

	private boolean isSystemActionsOnly(Set<Action> allowedActions) {
		return allowedActions.size() == 1 && allowedActions.iterator()
				.next()
				.getActionId()
				.equals(ActionTypeConstants.READ);
	}

	private Set<Action> addNoAllowedAction(Set<Action> allowedActions) {
		EmfAction noAllowed = new EmfAction(ActionTypeConstants.NO_ACTIONS_ALLOWED);
		noAllowed.setPurpose(ActionTypeConstants.NO_ACTIONS_ALLOWED);
		noAllowed.setLabel(labelProvider.getValue("cmf.btn.actions.not_allowed"));
		noAllowed.setDisabled(false);

		Set<Action> copy = new LinkedHashSet<>(allowedActions);
		copy.add(noAllowed);
		return copy;
	}

	private ActionMenu buildMenu(Collection<Action> actions, Instance instance) {
		Map<String, TransitionGroupDefinition> groups = stateTransitionManager.getActionGroups(instance)
				.stream()
				.collect(Collectors.toMap(TransitionGroupDefinition::getIdentifier, Function.identity()));

		return buildMenuInternal(actions, groups);
	}

	private static ActionMenu buildMenuInternal(Collection<Action> actions,
			Map<String, TransitionGroupDefinition> groups) {
		ActionMenu menu = new ActionMenu();
		ActionMenu currentMenu = menu;

		for (Action action : actions) {
			ActionMenu rootMenu = currentMenu;

			Deque<TransitionGroupDefinition> groupsTree = buildGroupTree(action.getGroup(), groups,
				new ArrayDeque<TransitionGroupDefinition>());

			while (!groupsTree.isEmpty()) {
				currentMenu = currentMenu.addMenuMember(new GroupItem(groupsTree.pop()));
			}

			currentMenu.addMenuMember(new ActionItem(action));
			currentMenu = rootMenu;
		}

		return menu;
	}

	private static Deque<TransitionGroupDefinition> buildGroupTree(String actionGroup,
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
