package com.sirma.itt.seip.instance.lock.action;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Used for unlock action. Can be extended with additional information for the operation execution, if needed.
 *
 * @author A. Kunchev
 */
public class UnlockRequest extends ActionRequest {

	public static final String UNLOCK = "unlock";

	private static final long serialVersionUID = -3511966486391946376L;

	@Override
	public String getOperation() {
		return UNLOCK;
	}

	@Override
	public String getUserOperation() {
		return UNLOCK;
	}
}
