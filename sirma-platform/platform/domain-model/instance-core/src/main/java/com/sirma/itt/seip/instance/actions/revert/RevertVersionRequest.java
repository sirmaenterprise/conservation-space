package com.sirma.itt.seip.instance.actions.revert;

import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.itt.seip.instance.version.InstanceVersionService;

/**
 * Request object for revert operation. Contains required information for successful action execution.
 *
 * @author A. Kunchev
 */
public class RevertVersionRequest extends ActionRequest {

	private static final long serialVersionUID = -5190631414932117730L;

	@Override
	public String getOperation() {
		return InstanceVersionService.REVERT_VERSION_SERVER_OPERATION;
	}

}
