package com.sirma.itt.seip.permissions.action;

import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.provider.ProviderRegistry;

/**
 * Register class for the security {@link Action}s.The implementation should provide access to a single instance of a
 * action identified by a pair of target class and action id.
 *
 * @author BBonev
 */
public interface ActionRegistry extends ProviderRegistry<String, Action> {
	// nothing specific to do here
}
