/**
 *
 */
package com.sirma.itt.seip.configuration.event;

import com.sirma.itt.seip.security.util.AbstractSecureEvent;
import com.sirma.itt.seip.security.util.SecureEvent;
import com.sirma.itt.seip.security.util.SecureEventAllTenants;

/**
 * Event to trigger full configuration reload.
 *
 * @author BBonev
 */
public class ConfigurationReloadRequest extends AbstractSecureEvent implements SecureEventAllTenants {

	@Override
	public boolean allowParallel() {
		return true;
	}

	@Override
	public SecureEvent copy() {
		return new ConfigurationReloadRequest();
	}
}
