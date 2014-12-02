package com.sirma.itt.emf.security.event;

import com.sirma.itt.emf.event.AuditableOperationEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event for changing an user's password.
 * 
 * @author Mihail Radkov
 */
@Documentation("Event fired after an user has changed its password.")
public class UserPasswordChangeEvent implements AuditableOperationEvent {

	@Override
	public String getOperationId() {
		return "changePassword";
	}

}
