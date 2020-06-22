package com.sirmaenterprise.sep.roles.rest;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Represent a response object return when requesting information about the role actions mapping. The object contains
 * information about the participating roles, actions and the mapping between them.
 *
 * @author BBonev
 */
public class RoleActionsResponse implements Serializable {

	private static final long serialVersionUID = 2181313052477202368L;

	private final List<ActionResponse> actions = new LinkedList<>();
	private final List<RoleResponse> roles = new LinkedList<>();
	private final List<RoleAction> roleActions = new LinkedList<>();

	public List<ActionResponse> getActions() {
		return actions;
	}

	public List<RoleResponse> getRoles() {
		return roles;
	}

	public List<RoleAction> getRoleActions() {
		return roleActions;
	}

	/**
	 * Add action response instance to the response object
	 * 
	 * @param actionResponse
	 *            the action to add
	 */
	public void add(ActionResponse actionResponse) {
		addNonNullValue(actions, actionResponse);
	}

	/**
	 * Add role response instance to the response object
	 * 
	 * @param roleResponse
	 *            the role to add
	 */
	public void add(RoleResponse roleResponse) {
		addNonNullValue(roles, roleResponse);
	}

	/**
	 * Add role action mapping entry to the response object
	 * 
	 * @param roleAction
	 *            the role action to add
	 */
	public void add(RoleAction roleAction) {
		addNonNullValue(roleActions, roleAction);
	}
}
