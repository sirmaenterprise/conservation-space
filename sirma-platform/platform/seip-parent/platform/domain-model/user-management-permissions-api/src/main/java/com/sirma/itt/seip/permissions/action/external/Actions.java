package com.sirma.itt.seip.permissions.action.external;

import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.seip.Copyable;


/**
 * Object that represents an actions set for a single object type.
 *
 * @author BBonev
 */
public class Actions implements Copyable<Actions> {

	private List<Action> actionsList;

	/**
	 * Getter method for actions.
	 *
	 * @return the actions
	 */
	public List<Action> getActions() {
		return actionsList;
	}

	/**
	 * Setter method for actions.
	 *
	 * @param actions
	 *            the actions to set
	 */
	public void setActions(List<Action> actions) {
		this.actionsList = actions;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(128);
		builder.append(actionsList);
		return builder.toString();
	}

	@Override
	public Actions createCopy() {
		Actions clone = new Actions();

		if (actionsList != null) {
			List<Action> actionsCopy = new ArrayList<>(actionsList.size());
			for (Action action : actionsList) {
				actionsCopy.add(action.createCopy());
			}
			clone.setActions(actionsCopy);
		}
		return clone;
	}
}
