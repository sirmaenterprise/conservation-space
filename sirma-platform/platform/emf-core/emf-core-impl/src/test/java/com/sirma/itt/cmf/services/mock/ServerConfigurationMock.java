package com.sirma.itt.cmf.services.mock;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * The ServerConfigurationMock listens for starup event and fires init phase
 */
@Startup(phase = StartupPhase.BEFORE_APP_START)
public class ServerConfigurationMock {
	// nothing to add
}