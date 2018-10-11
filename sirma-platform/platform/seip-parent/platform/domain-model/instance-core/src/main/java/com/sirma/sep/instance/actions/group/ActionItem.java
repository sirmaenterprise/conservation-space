package com.sirma.sep.instance.actions.group;

import com.sirma.itt.seip.domain.security.Action;

/**
 * Action menu member, that wraps {@link Action} object.
 *
 * @author T. Dossev
 */
public class ActionItem implements ActionMenuMember {

	private Action action;

	/**
	 * Constructs a {@link Action} wrapper.
	 *
	 * @param action
	 *            current action
	 */
	public ActionItem(Action action) {
		this.action = action;
	}

	@Override
	public Object toJsonHelper() {
		return Action.convertAction(action);
	}

	@Override
	public String getIdentifier() {
		return action.getIdentifier();
	}

	@Override
	public Integer getOrder() {
		return action.getOrder();
	}

	@Override
	public String getParent() {
		return null;
	}
}
