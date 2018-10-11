package com.sirma.itt.seip.definition.event;

import com.sirma.itt.seip.security.util.AbstractSecureEvent;

/**
 * Event fired when all semantic definitions are reloaded.
 *
 * @author S.Djulgerova
 */
public class SemanticDefinitionsReloaded extends AbstractSecureEvent {

	@Override
	public String toString() {
		return "SemanticDefinitionsReloaded";
	}

}
