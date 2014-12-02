package com.sirma.itt.cmf.help;

import com.sirma.itt.emf.event.AuditableOperationEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event for requesting help.
 * 
 * @author Mihail Radkov
 */
@Documentation("Event fired after requesting help.")
public class HelpRequestEvent implements AuditableOperationEvent {

	@Override
	public String getOperationId() {
		return "helpRequest";
	}
}
