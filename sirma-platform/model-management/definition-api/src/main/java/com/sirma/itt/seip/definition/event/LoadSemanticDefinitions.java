package com.sirma.itt.seip.definition.event;

import com.sirma.itt.seip.security.util.AbstractSecureEvent;
import com.sirma.itt.seip.security.util.SecureEvent;
import com.sirma.itt.seip.security.util.SecureEventAllTenants;

/**
 * Event object fired to initialize all semantic definition loading.
 *
 * @author kirq4e
 */
public class LoadSemanticDefinitions extends AbstractSecureEvent implements SecureEventAllTenants {

	@Override
	public String toString() {
		return "LoadSemanticDefinitions";
	}

	@Override
	public SecureEvent copy() {
		return new LoadSemanticDefinitions();
	}

}
