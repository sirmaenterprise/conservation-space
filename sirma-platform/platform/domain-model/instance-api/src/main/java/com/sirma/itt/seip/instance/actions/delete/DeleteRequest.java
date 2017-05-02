package com.sirma.itt.seip.instance.actions.delete;

import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Request for delete instance/instances. Used when delete operation is executed.
 *
 * <pre>
 * See com.sirma.itt.seip.instance.actions.delete.DeleteAction
 * </pre>
 * 
 * @see Actions
 * @author A. Kunchev
 */
public class DeleteRequest extends ActionRequest {

	private static final long serialVersionUID = -2293362940571396829L;

	public static final String DELETE_OPERATION = "delete";

	@Override
	public String getOperation() {
		return DELETE_OPERATION;
	}

}
