package com.sirma.itt.seip.resources.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.OperationEvent;

/**
 * Event for changing a user's password.
 *
 * @author Mihail Radkov
 */
@Documentation("Event fired after a user has changed its password.")
public class UserPasswordChangeEvent implements OperationEvent {

	private static final String OPERATION_ID = "changePassword";

	@Override
	public String getOperationId() {
		return OPERATION_ID;
	}

}
