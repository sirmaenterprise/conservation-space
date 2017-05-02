package com.sirma.itt.cmf.help;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.OperationEvent;

/**
 * Event for requesting help.
 *
 * @author Mihail Radkov
 */
@Documentation("Event fired after requesting help.")
public class HelpRequestEvent implements OperationEvent {

	private static final String OPERATION_ID = "helpRequest";

	@Override
	public String getOperationId() {
		return OPERATION_ID;
	}
}
