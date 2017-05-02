package com.sirma.itt.seip.definition.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.security.util.AbstractSecureEvent;
import com.sirma.itt.seip.security.util.SecureEvent;
import com.sirma.itt.seip.security.util.SecureEventAllTenants;

/**
 * Event object fired to initialize all definition loading. The handler should start template definition loading first
 * before top level definition loading. The event could be marked as forced to ignore the disabled configuration
 * loading.
 *
 * @author BBonev
 */
@Documentation("Event object fired to initialize all definition loading. "
		+ "The handler should start template definition loading first before top level definition loading. "
		+ "The event could be marked as forced to ignore the disabled configuration loading.")
public class LoadAllDefinitions extends AbstractSecureEvent implements SecureEventAllTenants {

	@Override
	public SecureEvent copy() {
		return new LoadAllDefinitions();
	}

	@Override
	public boolean allowParallel() {
		return true;
	}
}
