package com.sirma.itt.seip.permissions.action.external;

import java.util.List;

import com.sirma.itt.seip.exception.EmfRuntimeException;


/**
 * Object that represents an actions set for a single object type.
 *
 * @author BBonev
 */
public class Actions implements Cloneable {

	/** The actions. */
	private List<Action> actions;


	/**
	 * Getter method for actions.
	 *
	 * @return the actions
	 */
	public List<Action> getActions() {
		return actions;
	}

	/**
	 * Setter method for actions.
	 *
	 * @param actions
	 *            the actions to set
	 */
	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(128);
		builder.append(actions);
		return builder.toString();
	}

	@Override
	public Actions clone() {
		try {
			return (Actions) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new EmfRuntimeException(e);
		}
	}
}
