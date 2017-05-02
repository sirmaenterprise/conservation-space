package com.sirma.itt.seip.definition.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.security.util.AbstractSecureEvent;

/**
 * Event object fired to initialize all definition loading. The handler should start template definition loading first
 * before top level definition loading. The event could be marked as forced to ignore the disabled configuration
 * loading.
 *
 * @author bbanchev
 */
@Documentation("Event object fired to initialize all definition loading in synchronious manner. "
		+ "Event processing should be the same as LoadAllDefinitions except it is synchronious.")
public class LoadAllDefinitionsSynchronious extends AbstractSecureEvent {
	// nothing to add here
}
