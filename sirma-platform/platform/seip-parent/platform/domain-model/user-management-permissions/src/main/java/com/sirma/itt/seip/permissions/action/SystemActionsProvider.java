package com.sirma.itt.seip.permissions.action;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.permissions.BaseDefinitionActionProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides system actions, which are used by the system for different requirements, like the create action is used to
 * calculate permissions for instance creation. In case there is defined action (transition) in definitions, it will
 * override the ones from this class.
 *
 * <p>
 * State transitions should be defined in definitions.
 *
 * @author smustafov
 */
@ApplicationScoped
@Extension(target = ActionProvider.TARGET_NAME, order = 4)
public class SystemActionsProvider implements ActionProvider {

	@Inject
	private LabelProvider labelProvider;

	@Override
	public Map<String, Action> provide() {
		Map<String, Action> data = new LinkedHashMap<>(2);
		Action createAction = BaseDefinitionActionProvider.createAction(ActionTypeConstants.CREATE, Action.TRANSITION,
				ActionTypeConstants.CREATE, labelProvider);
		data.put(ActionTypeConstants.CREATE, createAction);
		return data;
	}

}
