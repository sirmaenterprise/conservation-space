package com.sirma.itt.emf.web.action.event;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.web.event.AbstractWebEvent;

/**
 * Payload passed with allowed action events fired by the ActionsManager when particular action
 * button is clicked by the user through the user interface.
 * 
 * @author svelikov
 */
@Documentation("Payload passed with allowed action events fired by the ActionsManager when particular action button is clicked by the user through the user interface.")
public class EMFActionEvent extends AbstractWebEvent<Instance> {

	/** The action id. */
	private String actionId;

	/** The action. */
	private Action action;

	/**
	 * Instantiates a new cMF action event.
	 * 
	 * @param instance
	 *            the instance
	 * @param navigation
	 *            the navigation
	 * @param actionId
	 *            the action id
	 * @param action
	 *            the action
	 */
	public EMFActionEvent(Instance instance, String navigation, String actionId, Action action) {
		super(instance, navigation);
		this.setActionId(actionId);
		this.action = action;
	}

	/**
	 * Getter method for actionId.
	 * 
	 * @return the actionId
	 */
	public String getActionId() {
		return actionId;
	}

	/**
	 * Setter method for actionId.
	 * 
	 * @param actionId
	 *            the actionId to set
	 */
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	/**
	 * Getter method for action.
	 * 
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Setter method for action.
	 * 
	 * @param action
	 *            the action to set
	 */
	public void setAction(Action action) {
		this.action = action;
	}

}
