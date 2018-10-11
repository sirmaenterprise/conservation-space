package com.sirma.itt.seip.instance.actions.evaluation;

import java.io.Serializable;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Used as request object for the actions listing for the instances. Instances of this class are build, when there are
 * requests for the instance actions.
 *
 * @author A. Kunchev
 */
public class ActionsListRequest extends ActionRequest {

	public static final String ACTIONS_LIST = "actions-list";

	private static final long serialVersionUID = -4296567726530260533L;

	private Serializable contextId;

	private boolean flatMenu;

	@Override
	public String getOperation() {
		return ACTIONS_LIST;
	}

	/**
	 * Getter for the path. Used to resolve the current instance context.
	 *
	 * @return the path
	 */
	public Serializable getContextId() {
		return contextId;
	}

	/**
	 * Setter for the instance id of the context of the current instance.
	 *
	 * @param contextId
	 *            the contextId to set
	 */
	public void setContextId(Serializable contextId) {
		this.contextId = contextId;
	}

	/**
	 * Getter for menu type.
	 * 
	 * @return true if flat menu, otherwise false
	 */
	public boolean getFlatMenuType() {
		return this.flatMenu;
	}

	/**
	 * Setter for returned the menu type.
	 *
	 * @param flatMenu
	 *            true if flat menu, otherwise false
	 */
	public void setFlatMenuType(boolean flatMenu) {
		this.flatMenu = flatMenu;
	}

}
